package com.example.weedventory.data.repository

import com.example.weedventory.data.local.db.dao.MovimientoInventarioDao
import com.example.weedventory.data.local.db.dao.ProductoDao
import com.example.weedventory.data.local.db.dao.VentaDao
import com.example.weedventory.data.local.db.entity.DetalleVenta
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import com.example.weedventory.data.local.db.entity.TipoVenta
import com.example.weedventory.data.local.db.entity.Venta
import kotlinx.coroutines.flow.Flow
import kotlin.math.roundToInt

data class ItemVenta(
    val productoId: Long,
    val cantidad: Double,
    val unidad: String = "KILO",
    val precioUnitario: Double
)

/**
 * Repositorio para operaciones de Ventas
 * Maneja tanto ventas normales como consumo propio
 */
class VentaRepository(
    private val ventaDao: VentaDao,
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoInventarioDao,
    private val inventarioRepository: InventarioRepository
) {
    
    fun getTodosVentas(): Flow<List<Venta>> = ventaDao.getTodosVentas()
    
    fun getVentasPorTipo(tipo: TipoVenta): Flow<List<Venta>> = ventaDao.getVentasPorTipo(tipo)
    
    fun getVentasEnRango(fechaMin: Long, fechaMax: Long): Flow<List<Venta>> =
        ventaDao.getVentasEnRango(fechaMin, fechaMax)
    
    suspend fun getTotalVentasEnRango(fechaMin: Long, fechaMax: Long): Double =
        ventaDao.getTotalVentasEnRango(fechaMin, fechaMax)
    
    suspend fun registrarVentaNormal(
        cliente: String?,
        items: List<ItemVenta>,
        observacion: String = ""
    ): Result<Long> {
        return try {
            require(items.isNotEmpty()) { "La venta debe contener al menos un producto" }
            
            var total = 0.0
            
            // Validar stock de todos los productos antes de proceder
            for (item in items) {
                val producto = productoDao.getById(item.productoId)
                    ?: return Result.failure(Exception("Producto ${item.productoId} no encontrado"))
                
                val cantidadStock = item.cantidad.toStockQuantity()
                if (producto.stockActual < cantidadStock) {
                    return Result.failure(
                        Exception("Stock insuficiente para ${producto.nombre}. Disponible: ${producto.stockActual}, solicitado: $cantidadStock")
                    )
                }
                total += item.cantidad * item.precioUnitario
            }
            
            // Crear venta
            val venta = Venta(
                tipoVenta = TipoVenta.NORMAL,
                cliente = cliente,
                total = total,
                observacion = observacion
            )
            val ventaId = ventaDao.insertVenta(venta)
            
            // Insertar detalles y actualizar inventario
            for (item in items) {
                val cantidadStock = item.cantidad.toStockQuantity()
                ventaDao.insertDetalleVenta(
                    DetalleVenta(
                        ventaId = ventaId,
                        productoId = item.productoId,
                        cantidad = cantidadStock,
                        unidad = item.unidad,
                        precioUnitario = item.precioUnitario,
                        subtotal = item.cantidad * item.precioUnitario
                    )
                )

                // Registrar movimiento de inventario
                inventarioRepository.registrarSalida(
                    productoId = item.productoId,
                    cantidad = cantidadStock,
                    tipoMovimiento = TipoMovimiento.VENTA,
                    referenciaId = ventaId
                )
            }
            
            Result.success(ventaId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registrarConsumoPropio(
        items: List<ItemVenta>,
        observacion: String = ""
    ): Result<Long> {
        return try {
            require(items.isNotEmpty()) { "El consumo debe contener al menos un producto" }
            
            var total = 0.0
            
            // Validar stock
            for (item in items) {
                val producto = productoDao.getById(item.productoId)
                    ?: return Result.failure(Exception("Producto ${item.productoId} no encontrado"))
                
                val cantidadStock = item.cantidad.toStockQuantity()
                if (producto.stockActual < cantidadStock) {
                    return Result.failure(
                        Exception("Stock insuficiente para ${producto.nombre}. Disponible: ${producto.stockActual}, solicitado: $cantidadStock")
                    )
                }
                total += item.cantidad * item.precioUnitario
            }
            
            // Crear registro de consumo propio
            val venta = Venta(
                tipoVenta = TipoVenta.CONSUMO_PROPIO,
                cliente = null,
                total = total,
                observacion = observacion
            )
            val ventaId = ventaDao.insertVenta(venta)
            
            // Insertar detalles y actualizar inventario
            for (item in items) {
                val cantidadStock = item.cantidad.toStockQuantity()
                ventaDao.insertDetalleVenta(
                    DetalleVenta(
                        ventaId = ventaId,
                        productoId = item.productoId,
                        cantidad = cantidadStock,
                        unidad = item.unidad,
                        precioUnitario = item.precioUnitario,
                        subtotal = item.cantidad * item.precioUnitario
                    )
                )

                // Registrar movimiento de inventario
                inventarioRepository.registrarSalida(
                    productoId = item.productoId,
                    cantidad = cantidadStock,
                    tipoMovimiento = TipoMovimiento.CONSUMO,
                    referenciaId = ventaId
                )
            }
            
            Result.success(ventaId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun getDetallesVenta(ventaId: Long): List<DetalleVenta> {
        return ventaDao.getDetallesVenta(ventaId)
    }
    
    suspend fun eliminarVenta(id: Long): Result<Unit> {
        return try {
            val venta = ventaDao.getVentaById(id) ?: return Result.failure(Exception("Venta no encontrada"))
            val detalles = ventaDao.getDetallesVenta(id)
            
            // Revertir inventario
            for (detalle in detalles) {
                inventarioRepository.registrarEntrada(
                    productoId = detalle.productoId,
                    cantidad = detalle.cantidad,
                    nota = "Reversión de venta eliminada"
                )
            }
            
            ventaDao.deleteVenta(venta)
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
