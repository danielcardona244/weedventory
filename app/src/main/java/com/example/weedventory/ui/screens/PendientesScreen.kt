package com.example.weedventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weedventory.data.local.db.entity.Consignacion
import com.example.weedventory.data.local.db.entity.EstadoConsignacion
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.InventarioViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import com.example.weedventory.utils.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PendientesScreen(
    navController: NavHostController,
    viewModel: ConsignacionViewModel
) {
    val consignacionesPendientes by viewModel.consignacionesPendientes.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Consignaciones Pendientes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (consignacionesPendientes.isEmpty()) {
                Text("No hay consignaciones pendientes")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(consignacionesPendientes) { consignacion ->
                        ConsignacionCard(consignacion, viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ConsignacionCard(
    consignacion: Consignacion,
    viewModel: ConsignacionViewModel
) {
    val ahora = System.currentTimeMillis()
    val estado = when {
        consignacion.estado == EstadoConsignacion.SALDADA -> "SALDADA"
        consignacion.fechaRecordatorio < ahora && consignacion.estado == EstadoConsignacion.PENDIENTE -> "VENCIDA ⚠"
        else -> "PENDIENTE"
    }
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        consignacion.comprador,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Total: $${consignacion.montoTotal}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    estado,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (estado == "VENCIDA ⚠") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
            
            Text(
                "Recordatorio: ${DateFormatter.formatarFecha(consignacion.fechaRecordatorio)}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Text(
                "Saldo Pendiente: $${consignacion.saldoPendiente}",
                style = MaterialTheme.typography.bodySmall
            )
            
            Button(
                onClick = { viewModel.marcarSaldada(consignacion.id) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Marcar como Saldada")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistorialScreen(
    navController: NavHostController,
    ventaViewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel,
    inventarioViewModel: InventarioViewModel
) {
    val ventas by ventaViewModel.todasVentas.collectAsState()
    val consignaciones by consignacionViewModel.todasConsignaciones.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text("Últimas Ventas: ${ventas.size}", style = MaterialTheme.typography.labelMedium)
            Text("Total Consignaciones: ${consignaciones.size}", style = MaterialTheme.typography.labelMedium)
            
            Text(
                "Consignaciones Saldadas: ${consignaciones.count { it.estado == EstadoConsignacion.SALDADA }}",
                style = MaterialTheme.typography.labelMedium
            )
            
            Text(
                "Consignaciones Pendientes: ${consignaciones.count { it.estado == EstadoConsignacion.PENDIENTE }}",
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}
