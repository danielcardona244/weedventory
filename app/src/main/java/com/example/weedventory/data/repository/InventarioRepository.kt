package com.example.weedventory.data.repository

import com.example.weedventory.data.local.db.dao.MovimientoInventarioDao
import com.example.weedventory.data.local.db.dao.ProductoDao
import com.example.weedventory.data.local.db.entity.MovimientoInventario
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones de Inventario
 * Maneja entradas, salidas, consumo y ajustes de stock
 */
class InventarioRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoInventarioDao
) {
    
    fun getMovimientosDelProducto(productoId: Long): Flow<List<MovimientoInventario>> =
        movimientoDao.getMovimientosDelProducto(productoId)
    
    fun getTodosMovimientos(): Flow<List<MovimientoInventario>> = movimientoDao.getTodos()
    
    fun getMovimientosPorTipo(tipo: TipoMovimiento): Flow<List<MovimientoInventario>> =
        movimientoDao.getMovimientosPorTipo(tipo)
    
    suspend fun registrarEntrada(
        productoId: Long,
        cantidad: Int,
        nota: String = ""
    ): Result<Long> {
        return try {
            require(cantidad > 0) { "Cantidad debe ser mayor a 0" }
            
            val producto = productoDao.getById(productoId) 
                ?: return Result.failure(Exception("Producto no encontrado"))
            
            val nuevoStock = producto.stockActual + cantidad
            productoDao.actualizarStock(productoId, nuevoStock)
            
            val movimiento = MovimientoInventario(
                productoId = productoId,
                tipoMovimiento = TipoMovimiento.ENTRADA,
                cantidad = cantidad,
                nota = nota
            )
            val id = movimientoDao.insert(movimiento)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registrarSalida(
        productoId: Long,
        cantidad: Int,
        tipoMovimiento: TipoMovimiento,
        nota: String = "",
        referenciaId: Long? = null
    ): Result<Long> {
        return try {
            require(cantidad > 0) { "Cantidad debe ser mayor a 0" }
            require(
                tipoMovimiento in listOf(
                    TipoMovimiento.VENTA,
                    TipoMovimiento.CONSUMO,
                    TipoMovimiento.CONSIGNACION
                )
            ) { "Tipo de movimiento inválido" }
            
            val producto = productoDao.getById(productoId)
                ?: return Result.failure(Exception("Producto no encontrado"))
            
            // Validar que haya stock disponible
            if (producto.stockActual < cantidad) {
                return Result.failure(
                    Exception("Stock insuficiente. Disponible: ${producto.stockActual}, Solicitado: $cantidad")
                )
            }
            
            val nuevoStock = producto.stockActual - cantidad
            productoDao.actualizarStock(productoId, nuevoStock)
            
            val movimiento = MovimientoInventario(
                productoId = productoId,
                tipoMovimiento = tipoMovimiento,
                cantidad = cantidad,
                nota = nota,
                referenciaId = referenciaId
            )
            val id = movimientoDao.insert(movimiento)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun registrarAjuste(
        productoId: Long,
        cantidadActual: Int,
        razon: String
    ): Result<Long> {
        return try {
            val producto = productoDao.getById(productoId)
                ?: return Result.failure(Exception("Producto no encontrado"))
            
            val diferencia = cantidadActual - producto.stockActual
            val tipo = if (diferencia >= 0) TipoMovimiento.ENTRADA else TipoMovimiento.AJUSTE
            
            productoDao.actualizarStock(productoId, cantidadActual)
            
            val movimiento = MovimientoInventario(
                productoId = productoId,
                tipoMovimiento = tipo,
                cantidad = kotlin.math.abs(diferencia),
                nota = "Ajuste: $razon"
            )
            val id = movimientoDao.insert(movimiento)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
