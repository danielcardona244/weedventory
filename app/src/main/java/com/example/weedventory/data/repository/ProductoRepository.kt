package com.example.weedventory.data.repository

import com.example.weedventory.data.local.db.dao.ProductoDao
import com.example.weedventory.data.local.db.entity.Producto
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio para operaciones de Productos
 */
class ProductoRepository(private val productoDao: ProductoDao) {
    
    fun getAllActivos(): Flow<List<Producto>> = productoDao.getAllActivos()
    
    fun getAll(): Flow<List<Producto>> = productoDao.getAll()
    
    fun getProductosConStockBajo(): Flow<List<Producto>> = productoDao.getProductosConStockBajo()
    
    suspend fun getById(id: Long): Producto? = productoDao.getById(id)
    
    suspend fun crearProducto(
        nombre: String,
        descripcion: String,
        costo: Double,
        precioVenta: Double,
        stockMinimo: Int = 0
    ): Long {
        val producto = Producto(
            nombre = nombre,
            descripcion = descripcion,
            costo = costo,
            precioVenta = precioVenta,
            stockMinimo = stockMinimo,
            stockActual = 0
        )
        return productoDao.insert(producto)
    }
    
    suspend fun actualizarProducto(producto: Producto) {
        productoDao.update(producto)
    }
    
    suspend fun desactivarProducto(id: Long) {
        val producto = productoDao.getById(id) ?: return
        productoDao.update(producto.copy(activo = false))
    }
    
    suspend fun actualizarStock(productoId: Long, nuevoStock: Int) {
        // Validar que el stock no sea negativo
        val stock = if (nuevoStock < 0) 0 else nuevoStock
        productoDao.actualizarStock(productoId, stock)
    }
}
