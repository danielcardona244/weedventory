package com.example.weedventory.data.local.db.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Entidad que representa una consignación con lógica específica
 * Separada de Venta porque tiene tratamiento diferente:
 * - Seguimiento de saldo pendiente
 * - Fecha de recordatorio
 * - Estado específico (PENDIENTE/SALDADA/VENCIDA)
 */
@Entity(tableName = "consignaciones")
data class Consignacion(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val comprador: String,
    val fechaEntrega: Long = Instant.now().toEpochMilli(),
    val fechaRecordatorio: Long, // Fecha elegida para ser recordado
    val montoTotal: Double,
    val saldoPendiente: Double = montoTotal, // Saldo que aún debe pagar
    val estado: EstadoConsignacion = EstadoConsignacion.PENDIENTE,
    val observaciones: String = "",
    val fechaCreacion: Long = Instant.now().toEpochMilli(),
    
    // Productos entregados (JSON serializado o relación separada)
    // Usaremos una tabla separada ConsignacionDetalle
)

/**
 * Entidad para los detalles de productos en una consignación
 */
@Entity(
    tableName = "consignacion_detalles",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Consignacion::class,
            parentColumns = ["id"],
            childColumns = ["consignacionId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        ),
        androidx.room.ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = androidx.room.ForeignKey.CASCADE
        )
    ],
    indices = [
        androidx.room.Index("consignacionId"),
        androidx.room.Index("productoId")
    ]
)
data class ConsignacionDetalle(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val consignacionId: Long,
    val productoId: Long,
    val cantidad: Int,
    @ColumnInfo(defaultValue = "'KILO'")
    val unidad: String = "KILO",
    val precioUnitario: Double,
    val subtotal: Double
)
