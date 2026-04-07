package com.example.weedventory.data.repository

import com.example.weedventory.data.local.db.dao.MovimientoInventarioDao
import com.example.weedventory.data.local.db.dao.ProductoDao
import com.example.weedventory.data.local.db.dao.VentaDao
import com.example.weedventory.data.local.db.entity.DetalleVenta
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import com.example.weedventory.data.local.db.entity.TipoVenta
import com.example.weedventory.data.local.db.entity.Venta
import kotlinx.coroutines.flow.Flow

data class ItemVenta(
    val productoId: Long,
    val cantidad: Int,
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
                
                if (producto.stockActual < item.cantidad) {
                    return Result.failure(
                        Exception("Stock insuficiente para ${producto.nombre}")
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
                ventaDao.insertDetalleVenta(
                    DetalleVenta(
                        ventaId = ventaId,
                        productoId = item.productoId,
                        cantidad = item.cantidad,
                        precioUnitario = item.precioUnitario
                    )
                )
                
                // Registrar movimiento de inventario
                inventarioRepository.registrarSalida(
                    productoId = item.productoId,
                    cantidad = item.cantidad,
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
                
                if (producto.stockActual < item.cantidad) {
                    return Result.failure(
                        Exception("Stock insuficiente para ${producto.nombre}")
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
                ventaDao.insertDetalleVenta(
                    DetalleVenta(
                        ventaId = ventaId,
                        productoId = item.productoId,
                        cantidad = item.cantidad,
                        precioUnitario = item.precioUnitario
                    )
                )
                
                // Registrar movimiento de inventario
                inventarioRepository.registrarSalida(
                    productoId = item.productoId,
                    cantidad = item.cantidad,
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
}
