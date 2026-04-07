package com.example.weedventory.data.local.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.weedventory.data.local.db.entity.Consignacion
import com.example.weedventory.data.local.db.entity.ConsignacionDetalle
import com.example.weedventory.data.local.db.entity.EstadoConsignacion
import kotlinx.coroutines.flow.Flow

@Dao
interface ConsignacionDao {
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(consignacion: Consignacion): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertDetalle(detalle: ConsignacionDetalle): Long
    
    @Update
    suspend fun update(consignacion: Consignacion)
    
    @Delete
    suspend fun delete(consignacion: Consignacion)
    
    @Query("SELECT * FROM consignaciones WHERE id = :id")
    suspend fun getById(id: Long): Consignacion?
    
    @Query("SELECT * FROM consignaciones ORDER BY fechaEntrega DESC")
    fun getAll(): Flow<List<Consignacion>>
    
    @Query("""
        SELECT * FROM consignaciones 
        WHERE estado = :estado 
        ORDER BY fechaEntrega DESC
    """)
    fun getConsignacionesPorEstado(estado: EstadoConsignacion): Flow<List<Consignacion>>
    
    @Query("""
        SELECT * FROM consignaciones 
        WHERE fechaRecordatorio >= :fechaMin AND fechaRecordatorio <= :fechaMax
        ORDER BY fechaRecordatorio ASC
    """)
    fun getConsignacionesProximas(fechaMin: Long, fechaMax: Long): Flow<List<Consignacion>>
    
    @Query("""
        SELECT * FROM consignaciones 
        WHERE estado = 'PENDIENTE' AND fechaRecordatorio < :ahora
        ORDER BY fechaRecordatorio ASC
    """)
    fun getConsignacionesVencidas(ahora: Long): Flow<List<Consignacion>>
    
    @Query("SELECT * FROM consignacion_detalles WHERE consignacionId = :consignacionId")
    suspend fun getDetalles(consignacionId: Long): List<ConsignacionDetalle>
    
    @Query("""
        UPDATE consignaciones 
        SET estado = :nuevoEstado, saldoPendiente = :nuevoSaldo 
        WHERE id = :consignacionId
    """)
    suspend fun actualizarEstadoYSaldo(
        consignacionId: Long,
        nuevoEstado: EstadoConsignacion,
        nuevoSaldo: Double
    )
    
    @Query("""
        SELECT COUNT(*) FROM consignaciones 
        WHERE estado = 'PENDIENTE'
    """)
    suspend fun contarPendientes(): Int
    
    @Query("""
        SELECT COUNT(*) FROM consignaciones 
        WHERE estado = 'PENDIENTE' AND fechaRecordatorio < :ahora
    """)
    suspend fun contarVencidas(ahora: Long): Int
}
