package com.example.weedventory.data.repository

import com.example.weedventory.data.local.db.dao.ConsignacionDao
import com.example.weedventory.data.local.db.dao.ProductoDao
import com.example.weedventory.data.local.db.entity.Consignacion
import com.example.weedventory.data.local.db.entity.ConsignacionDetalle
import com.example.weedventory.data.local.db.entity.EstadoConsignacion
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import kotlin.math.roundToInt

data class ItemConsignacion(
    val productoId: Long,
    val cantidad: Double,
    val unidad: String = "KILO",
    val precioUnitario: Double
)

/**
 * Repositorio para operaciones de Consignaciones
 * Maneja el registro y seguimiento de consignaciones con lógica específica:
 * - Seguimiento de saldo pendiente
 * - Recordatorios en fechas específicas
 * - Estados (PENDIENTE, SALDADA, VENCIDA)
 */
class ConsignacionRepository(
    private val consignacionDao: ConsignacionDao,
    private val productoDao: ProductoDao,
    private val inventarioRepository: InventarioRepository
) {
    
    fun getAll(): Flow<List<Consignacion>> = consignacionDao.getAll()
    
    fun getConsignacionesPorEstado(estado: EstadoConsignacion): Flow<List<Consignacion>> =
        consignacionDao.getConsignacionesPorEstado(estado)
    
    fun getConsignacionesProximas(fechaMin: Long, fechaMax: Long): Flow<List<Consignacion>> =
        consignacionDao.getConsignacionesProximas(fechaMin, fechaMax)
    
    fun getConsignacionesVencidas(ahora: Long): Flow<List<Consignacion>> =
        consignacionDao.getConsignacionesVencidas(ahora)
    
    suspend fun contarPendientes(): Int = consignacionDao.contarPendientes()
    
    suspend fun contarVencidas(ahora: Long): Int = consignacionDao.contarVencidas(ahora)
    
    suspend fun registrarConsignacion(
        comprador: String,
        items: List<ItemConsignacion>,
        fechaEntrega: Long = Instant.now().toEpochMilli(),
        fechaRecordatorio: Long,
        observaciones: String = ""
    ): Result<Long> {
        return try {
            require(comprador.isNotBlank()) { "El nombre del comprador es obligatorio" }
            require(items.isNotEmpty()) { "La consignación debe contener al menos un producto" }
            require(fechaRecordatorio > fechaEntrega) { "Fecha de recordatorio debe ser posterior a entrega" }
            
            var montoTotal = 0.0
            
            // Validar stock de todos los productos
            for (item in items) {
                val producto = productoDao.getById(item.productoId)
                    ?: return Result.failure(Exception("Producto ${item.productoId} no encontrado"))

                val cantidadStock = item.cantidad.toStockQuantity()
                if (producto.stockActual < cantidadStock) {
                    return Result.failure(
                        Exception("Stock insuficiente para ${producto.nombre}. Disponible: ${producto.stockActual}, solicitado: $cantidadStock")
                    )
                }
                montoTotal += item.cantidad * item.precioUnitario
            }
            
            // Crear consignación
            val consignacion = Consignacion(
                comprador = comprador,
                fechaEntrega = fechaEntrega,
                fechaRecordatorio = fechaRecordatorio,
                montoTotal = montoTotal,
                saldoPendiente = montoTotal,
                estado = EstadoConsignacion.PENDIENTE,
                observaciones = observaciones
            )
            val consignacionId = consignacionDao.insert(consignacion)
            
            // Insertar detalles y descontar del inventario
            for (item in items) {
                val cantidadStock = item.cantidad.toStockQuantity()
                consignacionDao.insertDetalle(
                    ConsignacionDetalle(
                        consignacionId = consignacionId,
                        productoId = item.productoId,
                        cantidad = cantidadStock,
                        unidad = item.unidad,
                        precioUnitario = item.precioUnitario,
                        subtotal = item.cantidad * item.precioUnitario
                    )
                )

                // Descontar del inventario
                inventarioRepository.registrarSalida(
                    productoId = item.productoId,
                    cantidad = cantidadStock,
                    tipoMovimiento = TipoMovimiento.CONSIGNACION,
                    nota = "Consignación a: $comprador",
                    referenciaId = consignacionId
                )
            }
            
            Result.success(consignacionId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun marcarSaldada(consignacionId: Long): Result<Unit> {
        return try {
            val consignacion = consignacionDao.getById(consignacionId)
                ?: return Result.failure(Exception("Consignación no encontrada"))
            
            consignacionDao.actualizarEstadoYSaldo(
                consignacionId,
                EstadoConsignacion.SALDADA,
                0.0
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registrarPagoPartial(
        consignacionId: Long,
        montoPagado: Double
    ): Result<Unit> {
        return try {
            val consignacion = consignacionDao.getById(consignacionId)
                ?: return Result.failure(Exception("Consignación no encontrada"))
            
            require(montoPagado > 0) { "Monto debe ser mayor a 0" }
            require(montoPagado <= consignacion.saldoPendiente) {
                "El pago no puede exceder el saldo pendiente"
            }
            
            val nuevoSaldo = consignacion.saldoPendiente - montoPagado
            val nuevoEstado = if (nuevoSaldo <= 0) EstadoConsignacion.SALDADA 
                             else EstadoConsignacion.PENDIENTE
            
            consignacionDao.actualizarEstadoYSaldo(
                consignacionId,
                nuevoEstado,
                if (nuevoSaldo <= 0) 0.0 else nuevoSaldo
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun marcarVencida(consignacionId: Long): Result<Unit> {
        return try {
            consignacionDao.actualizarEstadoYSaldo(
                consignacionId,
                EstadoConsignacion.VENCIDA,
                // Mantener el saldo pendiente actual
                consignacionDao.getById(consignacionId)?.saldoPendiente ?: 0.0
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDetalles(consignacionId: Long): List<ConsignacionDetalle> {
        return consignacionDao.getDetalles(consignacionId)
    }
    
    suspend fun eliminarConsignacion(id: Long): Result<Unit> {
        return try {
            val consignacion = consignacionDao.getById(id) ?: return Result.failure(Exception("Consignación no encontrada"))
            val detalles = consignacionDao.getDetalles(id)
            
            // Revertir inventario
            for (detalle in detalles) {
                inventarioRepository.registrarEntrada(
                    productoId = detalle.productoId,
                    cantidad = detalle.cantidad,
                    nota = "Reversión de consignación eliminada"
                )
            }
            
            consignacionDao.delete(consignacion)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Double.toStockQuantity(): Int {
        val quantity = roundToInt()
        require(quantity > 0) { "Cantidad debe ser mayor a 0" }
        return quantity
    }
}
