package com.example.weedventory.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.weedventory.utils.Constants

class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICACION_CONSIGNACION_CHANNEL_ID,
                Constants.NOTIFICACION_CONSIGNACION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de recordatorio de consignaciones"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    fun mostrarNotificacionConsignacion(
        titulo: String,
        mensaje: String,
        notificationId: Int = 1
    ) {
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICACION_CONSIGNACION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(titulo)
            .setContentText(mensaje)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(notificationId, notification)
    }
}
