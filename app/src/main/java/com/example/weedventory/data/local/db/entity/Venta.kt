package com.example.weedventory.data.local.db.entity

import androidx.room.Entity
import androidx.room.ColumnInfo
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Entidad que representa una venta (normal o consumo propio)
 */
@Entity(tableName = "ventas")
data class Venta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val fecha: Long = Instant.now().toEpochMilli(),
    val tipoVenta: TipoVenta = TipoVenta.NORMAL,
    val cliente: String? = null,  // Solo para ventas normales, opcional
    val total: Double = 0.0,
    val observacion: String = ""
)

/**
 * Entidad que representa los detalles (items) de una venta
 */
@Entity(
    tableName = "detalles_venta",
    foreignKeys = [
        androidx.room.ForeignKey(
            entity = Venta::class,
            parentColumns = ["id"],
            childColumns = ["ventaId"],
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
        androidx.room.Index("ventaId"),
        androidx.room.Index("productoId")
    ]
)
data class DetalleVenta(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val ventaId: Long,
    val productoId: Long,
    val cantidad: Int,
    @ColumnInfo(defaultValue = "'KILO'")
    val unidad: String = "KILO",
    val precioUnitario: Double,
    val subtotal: Double
)
