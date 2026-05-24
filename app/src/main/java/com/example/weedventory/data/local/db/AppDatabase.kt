package com.example.weedventory.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.weedventory.data.local.db.dao.ConsignacionDao
import com.example.weedventory.data.local.db.dao.MovimientoInventarioDao
import com.example.weedventory.data.local.db.dao.ProductoDao
import com.example.weedventory.data.local.db.dao.VentaDao
import com.example.weedventory.data.local.db.entity.Consignacion
import com.example.weedventory.data.local.db.entity.ConsignacionDetalle
import com.example.weedventory.data.local.db.entity.DetalleVenta
import com.example.weedventory.data.local.db.entity.MovimientoInventario
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.data.local.db.entity.Venta

import androidx.room.TypeConverter
import com.example.weedventory.data.local.db.entity.EstadoConsignacion
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import com.example.weedventory.data.local.db.entity.TipoVenta
import java.time.Instant

/**
 * TypeConverter para tipos complejos que Room no maneja automáticamente
 */
class Converters {
    @TypeConverter
    fun fromInstant(value: Instant?): Long? {
        return value?.toEpochMilli()
    }

    @TypeConverter
    fun toInstant(value: Long?): Instant? {
        return value?.let { Instant.ofEpochMilli(it) }
    }

    @TypeConverter
    fun fromTipoMovimiento(value: TipoMovimiento?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTipoMovimiento(value: String?): TipoMovimiento? {
        return value?.let { TipoMovimiento.valueOf(it) }
    }

    @TypeConverter
    fun fromEstadoConsignacion(value: EstadoConsignacion?): String? {
        return value?.name
    }

    @TypeConverter
    fun toEstadoConsignacion(value: String?): EstadoConsignacion? {
        return value?.let { EstadoConsignacion.valueOf(it) }
    }

    @TypeConverter
    fun fromTipoVenta(value: TipoVenta?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTipoVenta(value: String?): TipoVenta? {
        return value?.let { TipoVenta.valueOf(it) }
    }
}

/**
 * Base de datos principal de la aplicación usando Room
 * 
 * Contiene:
 * - Productos: Inventario de artículos
 * - MovimientoInventario: Auditoría de cambios en stock
 * - Ventas: Registro de ventas (normales y consumo propio)
 * - DetallesVenta: Items dentro de cada venta
 * - Consignaciones: Registro de consignaciones
 * - DetallesConsignacion: Items dentro de cada consignación
 */
@Database(
    entities = [
        Producto::class,
        MovimientoInventario::class,
        Venta::class,
        DetalleVenta::class,
        Consignacion::class,
        ConsignacionDetalle::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun productoDao(): ProductoDao
    abstract fun movimientoInventarioDao(): MovimientoInventarioDao
    abstract fun ventaDao(): VentaDao
    abstract fun consignacionDao(): ConsignacionDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE productos ADD COLUMN unidad TEXT NOT NULL DEFAULT 'KILO'")
                db.execSQL("ALTER TABLE detalles_venta ADD COLUMN unidad TEXT NOT NULL DEFAULT 'KILO'")
                db.execSQL("ALTER TABLE consignacion_detalles ADD COLUMN unidad TEXT NOT NULL DEFAULT 'KILO'")
            }
        }
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "weedventory_database"
                ).addMigrations(MIGRATION_1_2).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
