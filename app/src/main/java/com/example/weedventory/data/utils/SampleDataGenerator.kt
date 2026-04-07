package com.example.weedventory.data.utils

import com.example.weedventory.data.local.db.AppDatabase
import com.example.weedventory.data.local.db.entity.Producto
import java.time.Instant

/**
 * Utilitario para generar datos de ejemplo en la base de datos
 * Únicamente para desarrollo y testing
 */
object SampleDataGenerator {
    
    suspend fun generarDatosDeEjemplo(db: AppDatabase) {
        // Limpiar datos anteriores (opcional)
        val productoDao = db.productoDao()
        
        // Crear productos de ejemplo
        val productos = listOf(
            Producto(
                nombre = "Cannabis Sativa",
                descripcion = "Flor de cannabis sativa premium",
                costo = 50.0,
                precioVenta = 100.0,
                stockActual = 50,
                stockMinimo = 10
            ),
            Producto(
                nombre = "Cannabis Indica",
                descripcion = "Flor de cannabis indica",
                costo = 45.0,
                precioVenta = 90.0,
                stockActual = 30,
                stockMinimo = 8
            ),
            Producto(
                nombre = "Aceite CBD",
                descripcion = "Aceite con 500mg de CBD",
                costo = 20.0,
                precioVenta = 45.0,
                stockActual = 15,
                stockMinimo = 5
            ),
            Producto(
                nombre = "Tinctura",
                descripcion = "Tinctura de cannabis concentrada",
                costo = 30.0,
                precioVenta = 65.0,
                stockActual = 20,
                stockMinimo = 5
            ),
            Producto(
                nombre = "Edibles",
                descripcion = "Caramelos con cannabis",
                costo = 10.0,
                precioVenta = 25.0,
                stockActual = 100,
                stockMinimo = 30
            )
        )
        
        // Insertar productos
        productos.forEach { producto ->
            try {
                productoDao.insert(producto)
            } catch (e: Exception) {
                // Ignorar si ya existen
            }
        }
    }
}
