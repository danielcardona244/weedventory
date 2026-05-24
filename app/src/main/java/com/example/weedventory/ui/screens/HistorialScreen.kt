@file:OptIn(ExperimentalMaterial3Api::class)

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun HistorialScreen(
    ventaViewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel
) {
    val todasVentas by ventaViewModel.todasVentas.collectAsState()
    val todasConsignaciones by consignacionViewModel.todasConsignaciones.collectAsState()
    val consignacionesPendientes by consignacionViewModel.consignacionesPendientes.collectAsState()
    val mensajeVentaError by ventaViewModel.mensajeError.collectAsState()
    val mensajeVentaExito by ventaViewModel.mensajeExito.collectAsState()
    val mensajeConsignacionError by consignacionViewModel.mensajeError.collectAsState()
    val mensajeConsignacionExito by consignacionViewModel.mensajeExito.collectAsState()

    LaunchedEffect(mensajeVentaExito, mensajeVentaError, mensajeConsignacionExito, mensajeConsignacionError) {
        if (mensajeVentaExito != null || mensajeVentaError != null || mensajeConsignacionExito != null || mensajeConsignacionError != null) {
            delay(3000)
            ventaViewModel.limpiarMensajes()
            consignacionViewModel.limpiarMensajes()
        }
    }

    val ahora = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    val ventasMes = todasVentas.filter { venta ->
        val fecha = Instant.ofEpochMilli(venta.fecha).atZone(ZoneId.systemDefault()).toLocalDate()
        fecha.month == ahora.month && fecha.year == ahora.year
    }
    val consignacionesMes = consignacionesPendientes.filter { consignacion ->
        val fecha = Instant.ofEpochMilli(consignacion.fechaEntrega).atZone(ZoneId.systemDefault()).toLocalDate()
        fecha.month == ahora.month && fecha.year == ahora.year
    }
    val totalVentasMes = ventasMes.sumOf { it.total }
    val totalConsignacionesMes = consignacionesMes.sumOf { it.montoTotal }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Historial del mes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        if (mensajeVentaError != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeVentaError!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
        }
        if (mensajeVentaExito != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeVentaExito!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.primary)
            }
        }
        if (mensajeConsignacionError != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeConsignacionError!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
        }
        if (mensajeConsignacionExito != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeConsignacionExito!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.primary)
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ventas este mes", style = MaterialTheme.typography.titleMedium)
                Text("Total: $${"%.2f".format(totalVentasMes)}")
                Text("${ventasMes.size} ventas")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(ventasMes) { venta ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Cliente: ${venta.cliente ?: "Sin cliente"}")
                            Text("Total: $${"%.2f".format(venta.total)}")
                            Text("Fecha: ${Instant.ofEpochMilli(venta.fecha).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        }
                        IconButton(onClick = { ventaViewModel.eliminarVenta(venta.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar venta")
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Consignaciones este mes", style = MaterialTheme.typography.titleMedium)
                Text("Total: $${"%.2f".format(totalConsignacionesMes)}")
                Text("${consignacionesMes.size} consignaciones")
            }
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
            items(consignacionesMes) { consignacion ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text("Comprador: ${consignacion.comprador}")
                            Text("Total: $${"%.2f".format(consignacion.montoTotal)}")
                            Text("Fecha entrega: ${Instant.ofEpochMilli(consignacion.fechaEntrega).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))}")
                        }
                        IconButton(onClick = { consignacionViewModel.eliminarConsignacion(consignacion.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar consignación")
                        }
                    }
                }
            }
        }
    }
}
