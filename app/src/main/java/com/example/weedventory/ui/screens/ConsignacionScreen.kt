@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weedventory.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.data.repository.ItemConsignacion
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.utils.DateFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsignacionScreen(
    navController: NavHostController,
    viewModel: ConsignacionViewModel,
    productoViewModel: ProductoViewModel
) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()
    val productos by productoViewModel.todosProductos.collectAsState()
    
    if (mostrarFormulario) {
        FormularioConsignacion(
            productos = productos,
            onGuardar = { comprador, items, fechaEntrega, fechaRecordatorio, observaciones ->
                viewModel.registrarConsignacion(comprador, items, fechaEntrega, fechaRecordatorio, observaciones)
                mostrarFormulario = false
            },
            onCancelar = { mostrarFormulario = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Consignaciones") },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, "Atrás")
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
                if (mensajeError != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            mensajeError!!,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                
                if (mensajeExito != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            mensajeExito!!,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                
                Button(
                    onClick = { mostrarFormulario = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nueva Consignación")
                }
            }
        }
    }
}

@Composable
fun FormularioConsignacion(
    productos: List<Producto>,
    onGuardar: (comprador: String, items: List<ItemConsignacion>, fechaEntrega: Long, fechaRecordatorio: Long, observaciones: String) -> Unit,
    onCancelar: () -> Unit
) {
    var comprador by remember { mutableStateOf("") }
    var observaciones by remember { mutableStateOf("") }
    var fechaEntrega by remember { mutableStateOf(System.currentTimeMillis()) }
    var fechaRecordatorio by remember { mutableStateOf(System.currentTimeMillis() + 86400000L) }
    var items by remember { mutableStateOf<List<ItemConsignacion>>(emptyList()) }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    
    val context = LocalContext.current
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Consignación") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = comprador,
                onValueChange = { comprador = it },
                label = { Text("Nombre del Comprador") },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Botón para seleccionar fecha de entrega
            OutlinedButton(
                onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            fechaEntrega = calendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fecha Entrega: ${DateFormatter.formatarFecha(fechaEntrega)}")
            }
            
            OutlinedButton(
                onClick = {
                    val calendar = Calendar.getInstance()
                    DatePickerDialog(
                        context,
                        { _, year, month, dayOfMonth ->
                            calendar.set(year, month, dayOfMonth)
                            fechaRecordatorio = calendar.timeInMillis
                        },
                        calendar.get(Calendar.YEAR),
                        calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fecha Recordatorio: ${DateFormatter.formatarFecha(fechaRecordatorio)}")
            }
            
            OutlinedTextField(
                value = observaciones,
                onValueChange = { observaciones = it },
                label = { Text("Observaciones") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Text("Productos (${items.size})", style = MaterialTheme.typography.labelMedium)
            
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio Unitario") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Button(
                onClick = {
                    if (cantidad.isNotBlank() && precio.isNotBlank() && productoSeleccionado != null) {
                        items = items + ItemConsignacion(
                            productoId = productoSeleccionado!!.id,
                            cantidad = cantidad.toInt(),
                            precioUnitario = precio.toDouble()
                        )
                        cantidad = ""
                        precio = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Agregar Producto")
            }
            
            Button(
                onClick = {
                    if (comprador.isNotBlank() && items.isNotEmpty()) {
                        onGuardar(comprador, items, fechaEntrega, fechaRecordatorio, observaciones)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Consignación")
            }
            
            Button(
                onClick = onCancelar,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}
