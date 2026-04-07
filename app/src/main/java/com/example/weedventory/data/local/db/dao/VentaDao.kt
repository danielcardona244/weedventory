package com.example.weedventory.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.weedventory.data.local.db.entity.DetalleVenta
import com.example.weedventory.data.local.db.entity.TipoVenta
import com.example.weedventory.data.local.db.entity.Venta
import kotlinx.coroutines.flow.Flow

@Dao
interface VentaDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertVenta(venta: Venta): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDetalleVenta(detalle: DetalleVenta): Long
    
    @Delete
    suspend fun deleteVenta(venta: Venta)
    
    @Query("SELECT * FROM ventas WHERE id = :id")
    suspend fun getVentaById(id: Long): Venta?
    
    @Query("SELECT * FROM ventas ORDER BY fecha DESC")
    fun getTodosVentas(): Flow<List<Venta>>
    
    @Query("""
        SELECT * FROM ventas 
        WHERE tipoVenta = :tipo 
        ORDER BY fecha DESC
    """)
    fun getVentasPorTipo(tipo: TipoVenta): Flow<List<Venta>>
    
    @Query("""
        SELECT * FROM ventas 
        WHERE fecha >= :fechaMin AND fecha <= :fechaMax
        ORDER BY fecha DESC
    """)
    fun getVentasEnRango(fechaMin: Long, fechaMax: Long): Flow<List<Venta>>
    
    @Query("SELECT * FROM detalles_venta WHERE ventaId = :ventaId")
    suspend fun getDetallesVenta(ventaId: Long): List<DetalleVenta>
    
    @Query("""
        SELECT COUNT(*) FROM ventas
    """)
    suspend fun getTotalVentas(): Int
    
    @Query("""
        SELECT COALESCE(SUM(total), 0.0) FROM ventas 
        WHERE fecha >= :fechaMin AND fecha <= :fechaMax
    """)
    suspend fun getTotalVentasEnRango(fechaMin: Long, fechaMax: Long): Double
}
