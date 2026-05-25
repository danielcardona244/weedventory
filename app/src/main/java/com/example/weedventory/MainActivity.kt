package com.example.weedventory

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.weedventory.data.local.db.AppDatabase
import com.example.weedventory.data.repository.ConsignacionRepository
import com.example.weedventory.data.repository.InventarioRepository
import com.example.weedventory.data.repository.ProductoRepository
import com.example.weedventory.data.repository.VentaRepository
import com.example.weedventory.ui.navigation.WeedventoryNavGraph
import com.example.weedventory.ui.theme.WeedventoryTheme
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import com.example.weedventory.utils.Constants
import com.example.weedventory.worker.ConsignacionReminderWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_OPEN_HISTORIAL = "open_historial"
    }
    
    private lateinit var productoViewModel: ProductoViewModel
    private lateinit var ventaViewModel: VentaViewModel
    private lateinit var consignacionViewModel: ConsignacionViewModel
    private var historialRequestId by mutableIntStateOf(0)
    
    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                scheduleConsignacionReminders()
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra(EXTRA_OPEN_HISTORIAL, false)) {
            historialRequestId++
        }
        
        // Inicializar base de datos y repositorios
        val db = AppDatabase.getDatabase(this)
        val productoRepository = ProductoRepository(db.productoDao())
        val inventarioRepository = InventarioRepository(db.productoDao(), db.movimientoInventarioDao())
        val ventaRepository = VentaRepository(
            db.ventaDao(),
            db.productoDao(),
            db.movimientoInventarioDao(),
            inventarioRepository
        )
        val consignacionRepository = ConsignacionRepository(
            db.consignacionDao(),
            db.productoDao(),
            inventarioRepository
        )
        
        // Crear ViewModels
        productoViewModel = ProductoViewModel(productoRepository)
        ventaViewModel = VentaViewModel(ventaRepository)
        consignacionViewModel = ConsignacionViewModel(consignacionRepository)
        
        // Solicitar permiso de notificaciones (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                scheduleConsignacionReminders()
            }
        } else {
            scheduleConsignacionReminders()
        }
        
        setContent {
            WeedventoryTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    WeedventoryNavGraph(
                        navController = navController,
                        productoViewModel = productoViewModel,
                        ventaViewModel = ventaViewModel,
                        consignacionViewModel = consignacionViewModel,
                        historialRequestId = historialRequestId
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(EXTRA_OPEN_HISTORIAL, false)) {
            historialRequestId++
        }
    }
    
    private fun scheduleConsignacionReminders() {
        // Programar Worker para revisar consignaciones diariamente
        val consignacionReminderWork = PeriodicWorkRequestBuilder<ConsignacionReminderWorker>(
            1, TimeUnit.DAYS
        ).build()
        
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            Constants.WORK_CONSIGNACION_REMINDER_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            consignacionReminderWork
        )
    }
}
