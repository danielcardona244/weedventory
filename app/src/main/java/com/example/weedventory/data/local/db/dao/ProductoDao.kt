package com.example.weedventory.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.weedventory.data.local.db.entity.Producto
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(producto: Producto): Long
    
    @Update
    suspend fun update(producto: Producto)
    
    @Delete
    suspend fun delete(producto: Producto)
    
    @Query("SELECT * FROM productos WHERE id = :id")
    suspend fun getById(id: Long): Producto?

    @Query("SELECT * FROM productos WHERE activo = 1 AND LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) LIMIT 1")
    suspend fun getByNombre(nombre: String): Producto?

    @Query("SELECT * FROM productos WHERE activo = 1 AND LOWER(TRIM(nombre)) = LOWER(TRIM(:nombre)) ORDER BY id ASC")
    suspend fun getActivosByNombre(nombre: String): List<Producto>

    @Query("SELECT nombre FROM productos WHERE activo = 1 GROUP BY LOWER(TRIM(nombre)) HAVING COUNT(*) > 1")
    suspend fun getNombresActivosDuplicados(): List<String>
    
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllActivos(): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getAll(): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos WHERE stockActual < stockMinimo AND activo = 1")
    fun getProductosConStockBajo(): Flow<List<Producto>>
    
    @Query("UPDATE productos SET stockActual = :nuevoStock WHERE id = :productoId")
    suspend fun actualizarStock(productoId: Long, nuevoStock: Int)

    @Query("UPDATE movimientos_inventario SET productoId = :productoDestinoId WHERE productoId = :productoOrigenId")
    suspend fun moverMovimientos(productoOrigenId: Long, productoDestinoId: Long)

    @Query("UPDATE detalles_venta SET productoId = :productoDestinoId WHERE productoId = :productoOrigenId")
    suspend fun moverDetallesVenta(productoOrigenId: Long, productoDestinoId: Long)

    @Query("UPDATE consignacion_detalles SET productoId = :productoDestinoId WHERE productoId = :productoOrigenId")
    suspend fun moverDetallesConsignacion(productoOrigenId: Long, productoDestinoId: Long)

    @Query("DELETE FROM productos WHERE id = :productoId")
    suspend fun deleteById(productoId: Long)

    @Transaction
    suspend fun fusionarActivosConMismoNombre(nombre: String): Producto? {
        val productos = getActivosByNombre(nombre)
        val productoPrincipal = productos.firstOrNull() ?: return null
        val duplicados = productos.drop(1)

        if (duplicados.isEmpty()) {
            return productoPrincipal
        }

        val productoFusionado = productoPrincipal.copy(
            nombre = productoPrincipal.nombre.trim(),
            descripcion = productoPrincipal.descripcion.ifBlank {
                duplicados.firstOrNull { it.descripcion.isNotBlank() }?.descripcion.orEmpty()
            },
            stockActual = productos.sumOf { it.stockActual },
            stockMinimo = productos.maxOf { it.stockMinimo }
        )

        update(productoFusionado)
        duplicados.forEach { duplicado ->
            moverMovimientos(duplicado.id, productoPrincipal.id)
            moverDetallesVenta(duplicado.id, productoPrincipal.id)
            moverDetallesConsignacion(duplicado.id, productoPrincipal.id)
            deleteById(duplicado.id)
        }

        return productoFusionado
    }
}
