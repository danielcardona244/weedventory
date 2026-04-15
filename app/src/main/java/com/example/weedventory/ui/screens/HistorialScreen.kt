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
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun HistorialScreen(
    ventaViewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel
) {
    val todasVentas by ventaViewModel.todasVentas.collectAsState()
    val consignacionesPendientes by consignacionViewModel.consignacionesPendientes.collectAsState()

    val ahora = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate()
    val ventasMes = todasVentas.filter { venta ->
        val fecha = Instant.ofEpochMilli(venta.fecha).atZone(ZoneId.systemDefault()).toLocalDate()
        fecha.month == ahora.month && fecha.year == ahora.year
    }
    val totalMes = ventasMes.sumOf { it.total }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Historial del mes", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Ventas este mes", style = MaterialTheme.typography.titleMedium)
                Text("Total de ventas: $${"%.2f".format(totalMes)}")
                Text("Número de ventas: ${ventasMes.size}")
                Text("Consignaciones pendientes: ${consignacionesPendientes.size}")
            }
        }

        Text("Ventas recientes", style = MaterialTheme.typography.titleMedium)
        if (ventasMes.isEmpty()) {
            Text("No hay ventas registradas en este mes")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ventasMes) { venta ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("${venta.tipoVenta.name} - $${"%.2f".format(venta.total)}", fontWeight = FontWeight.Bold)
                            Text("Fecha: ${venta.fecha}")
                            if (!venta.cliente.isNullOrBlank()) {
                                Text("Cliente: ${venta.cliente}")
                            }
                            if (venta.observacion.isNotBlank()) {
                                Text("Detalle: ${venta.observacion}")
                            }
                        }
                    }
                }
            }
        }

        Text("Consignaciones pendientes", style = MaterialTheme.typography.titleMedium)
        if (consignacionesPendientes.isEmpty()) {
            Text("No hay consignaciones pendientes")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(consignacionesPendientes) { consignacion ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Comprador: ${consignacion.comprador}", fontWeight = FontWeight.Bold)
                            Text("Total: $${"%.2f".format(consignacion.montoTotal)}")
                            Text("Saldo pendiente: $${"%.2f".format(consignacion.saldoPendiente)}")
                            Text("Estado: ${consignacion.estado.name}")
                        }
                    }
                }
            }
        }
    }
}
