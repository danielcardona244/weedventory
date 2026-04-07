package com.example.weedventory.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Entidad que registra cada movimiento de inventario
 * Proporciona auditoría completa de cambios en el stock
 */
@Entity(
    tableName = "movimientos_inventario",
    foreignKeys = [
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("productoId"),
        Index("fecha")
    ]
)
data class MovimientoInventario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val productoId: Long,
    val tipoMovimiento: TipoMovimiento,
    val cantidad: Int,
    val fecha: Long = Instant.now().toEpochMilli(),
    val referenciaId: Long? = null,  // ID de venta/consignación relacionada
    val nota: String = ""
)
