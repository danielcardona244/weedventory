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
    
    suspend fun getByNombre(nombre: String): Producto? = productoDao.getByNombre(nombre)

    suspend fun consolidarProductosDuplicadosActivos() {
        productoDao.getNombresActivosDuplicados().forEach { nombre ->
            productoDao.fusionarActivosConMismoNombre(nombre)
        }
    }

    suspend fun crearProducto(
        nombre: String,
        descripcion: String,
        costo: Double,
        precioVenta: Double,
        stockActual: Int = 0,
        stockMinimo: Int = 0,
        unidad: String = "KILO"
    ): Long {
        val nombreTrimmed = nombre.trim()
        val productoExistente = productoDao.fusionarActivosConMismoNombre(nombreTrimmed)
        if (productoExistente != null) {
            val productoActualizado = productoExistente.copy(
                descripcion = if (descripcion.isBlank()) productoExistente.descripcion else descripcion,
                costo = if (costo > 0.0) costo else productoExistente.costo,
                precioVenta = if (precioVenta > 0.0) precioVenta else productoExistente.precioVenta,
                stockActual = productoExistente.stockActual + stockActual,
                stockMinimo = maxOf(productoExistente.stockMinimo, stockMinimo),
                unidad = unidad
            )
            productoDao.update(productoActualizado)
            return productoExistente.id
        }

        val producto = Producto(
            nombre = nombreTrimmed,
            descripcion = descripcion,
            costo = costo,
            precioVenta = precioVenta,
            stockActual = stockActual,
            stockMinimo = stockMinimo,
            unidad = unidad
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
