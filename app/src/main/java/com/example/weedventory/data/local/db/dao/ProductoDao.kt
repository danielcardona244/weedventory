package com.example.weedventory.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
    
    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun getAllActivos(): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos ORDER BY nombre ASC")
    fun getAll(): Flow<List<Producto>>
    
    @Query("SELECT * FROM productos WHERE stockActual < stockMinimo AND activo = 1")
    fun getProductosConStockBajo(): Flow<List<Producto>>
    
    @Query("UPDATE productos SET stockActual = :nuevoStock WHERE id = :productoId")
    suspend fun actualizarStock(productoId: Long, nuevoStock: Int)
}
