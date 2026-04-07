package com.example.weedventory.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object DateFormatter {
    
    private val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es", "ES"))
    private val formatterWithTime = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale("es", "ES"))
    
    fun formatarFecha(millis: Long): String {
        return try {
            val instant = Instant.ofEpochMilli(millis)
            val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
            date.format(formatter)
        } catch (e: Exception) {
            "Fecha inválida"
        }
    }
    
    fun formatarFechaHora(millis: Long): String {
        return try {
            val instant = Instant.ofEpochMilli(millis)
            val dateTime = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()
            dateTime.format(formatterWithTime)
        } catch (e: Exception) {
            "Fecha/Hora inválida"
        }
    }
    
    fun hoysDateInMillis(): Long = System.currentTimeMillis()
    
    fun obtenerMismaFechaAlAnoDespues(millis: Long): Long {
        val instant = Instant.ofEpochMilli(millis)
        val date = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        val nextYear = date.plusYears(1)
        return nextYear.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

object Constants {
    const val NOTIFICACION_CONSIGNACION_CHANNEL_ID = "consignacion_reminders"
    const val NOTIFICACION_CONSIGNACION_CHANNEL_NAME = "Recordatorios de Consignación"
    
    const val WORK_CONSIGNACION_REMINDER_TAG = "consignacion_reminder"
    const val WORK_CONSIGNACION_REMINDER_NAME = "ConsignacionReminderWorker"
}
