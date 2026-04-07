package com.example.weedventory.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.weedventory.data.local.db.AppDatabase
import com.example.weedventory.data.local.db.entity.EstadoConsignacion
import com.example.weedventory.notification.NotificationHelper
import com.example.weedventory.utils.DateFormatter

/**
 * Worker que revisa las consignaciones pendientes y muestra notificaciones
 * se ejecuta diariamente para verificar si hay consignaciones que necesiten recordatorio
 */
class ConsignacionReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val consignacionDao = db.consignacionDao()
            val notificationHelper = NotificationHelper(applicationContext)
            
            val ahora = System.currentTimeMillis()
            
            // Obtener consignaciones pendientes que deben ser revisadas hoy
            val consignacionesAhora = consignacionDao.getConsignacionesProximas(
                ahora,
                ahora + 86400000 // Próximas 24 horas
            ).let { flow ->
                val lista = mutableListOf<com.example.weedventory.data.local.db.entity.Consignacion>()
                flow.collect { lista.addAll(it) }
                lista
            }
            
            // Mostrar notificación por cada consignación a revisar
            consignacionesAhora.forEach { consignacion ->
                if (consignacion.estado == EstadoConsignacion.PENDIENTE) {
                    notificationHelper.mostrarNotificacionConsignacion(
                        titulo = "Recordatorio: Consignación de ${consignacion.comprador}",
                        mensaje = "Saldo pendiente: $${consignacion.saldoPendiente}",
                        notificationId = consignacion.id.toInt()
                    )
                }
            }
            
            // Marcar consignaciones vencidas
            val consignacionesVencidas = consignacionDao.getConsignacionesVencidas(ahora)
                .let { flow ->
                    val lista = mutableListOf<com.example.weedventory.data.local.db.entity.Consignacion>()
                    flow.collect { lista.addAll(it) }
                    lista
                }
            
            consignacionesVencidas.forEach { consignacion ->
                consignacionDao.actualizarEstadoYSaldo(
                    consignacion.id,
                    EstadoConsignacion.VENCIDA,
                    consignacion.saldoPendiente
                )
                
                notificationHelper.mostrarNotificacionConsignacion(
                    titulo = "⚠ Consignación Vencida",
                    mensaje = "${consignacion.comprador} - Saldo: $${consignacion.saldoPendiente}",
                    notificationId = consignacion.id.toInt() + 10000
                )
            }
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
