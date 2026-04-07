package com.example.weedventory.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

/**
 * Entidad que representa un producto en el inventario
 */
@Entity(tableName = "productos")
data class Producto(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val nombre: String,
    val descripcion: String = "",
    val costo: Double,
    val precioVenta: Double,
    val stockActual: Int = 0,
    val stockMinimo: Int = 0,
    val activo: Boolean = true,
    val fechaCreacion: Long = Instant.now().toEpochMilli()
)
