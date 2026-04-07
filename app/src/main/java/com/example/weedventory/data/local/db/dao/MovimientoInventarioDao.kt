package com.example.weedventory.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.weedventory.data.local.db.entity.MovimientoInventario
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoInventarioDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(movimiento: MovimientoInventario): Long
    
    @Query("SELECT * FROM movimientos_inventario WHERE productoId = :productoId ORDER BY fecha DESC")
    fun getMovimientosDelProducto(productoId: Long): Flow<List<MovimientoInventario>>
    
    @Query("SELECT * FROM movimientos_inventario ORDER BY fecha DESC")
    fun getTodos(): Flow<List<MovimientoInventario>>
    
    @Query("""
        SELECT * FROM movimientos_inventario 
        WHERE tipoMovimiento = :tipo 
        ORDER BY fecha DESC
    """)
    fun getMovimientosPorTipo(tipo: TipoMovimiento): Flow<List<MovimientoInventario>>
    
    @Query("""
        SELECT * FROM movimientos_inventario 
        WHERE fecha >= :fechaMin AND fecha <= :fechaMax
        ORDER BY fecha DESC
    """)
    fun getMovimientosEnRango(fechaMin: Long, fechaMax: Long): Flow<List<MovimientoInventario>>
}
