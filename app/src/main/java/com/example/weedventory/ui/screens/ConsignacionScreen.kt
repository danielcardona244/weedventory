@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weedventory.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.data.repository.ItemConsignacion
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.utils.DateFormatter
import com.example.weedventory.utils.UnitType
import java.time.Instant
import java.util.Calendar

@Composable
fun ConsignacionScreen(
    viewModel: ConsignacionViewModel,
    productoViewModel: ProductoViewModel
) {
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()
    val productos by productoViewModel.todosProductos.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }
    
    LaunchedEffect(mensajeExito, mensajeError) {
        if (mensajeExito != null || mensajeError != null) {
            delay(3000)
            viewModel.limpiarMensajes()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Consignaciones", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (mensajeError != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeError!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (mensajeExito != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeExito!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (mostrarFormulario) {
            ConsignacionForm(
                productos = productos,
                onGuardar = { comprador, items, fechaPago, observacion ->
                    viewModel.registrarConsignacion(comprador, items, Instant.now().toEpochMilli(), fechaPago, observacion)
                },
                onCancelar = { mostrarFormulario = false }
            )
        } else {
            Button(onClick = { mostrarFormulario = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar consignación")
            }
        }
    }
}

@Composable
fun ConsignacionForm(
    productos: List<Producto>,
    onGuardar: (comprador: String, items: List<ItemConsignacion>, fechaPago: Long, observacion: String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var comprador by remember { mutableStateOf("") }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ItemConsignacion>>(emptyList()) }
    var fechaPago by remember { mutableStateOf<Long?>(null) }
    var fechaPagoTexto by remember { mutableStateOf("Seleccionar fecha de pago") }
    var observacion by remember { mutableStateOf("") }

    val fechaPicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            fechaPago = calendar.timeInMillis
            fechaPagoTexto = "Pago: ${dayOfMonth}/${month + 1}/$year"
        },
        Calendar.getInstance().get(Calendar.YEAR),
        Calendar.getInstance().get(Calendar.MONTH),
        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        OutlinedTextField(
            value = comprador,
            onValueChange = { comprador = it },
            label = { Text("Comprador") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(onClick = { fechaPicker.show() }, modifier = Modifier.fillMaxWidth()) {
            Text(fechaPagoTexto)
        }

        Text("Agregar producto", style = MaterialTheme.typography.titleMedium)
        Box(modifier = Modifier.fillMaxWidth()) {
            TextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = true },
                readOnly = true,
                value = productoSeleccionado?.nombre ?: "Seleccionar producto",
                onValueChange = {},
                label = { Text("Producto") }
            )
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                productos.forEach { producto ->
                    DropdownMenuItem(text = { Text(producto.nombre) }, onClick = {
                        productoSeleccionado = producto
                        expanded = false
                    })
                }
            }
        }

        OutlinedTextField(
            value = cantidad,
            onValueChange = { cantidad = it },
            label = { Text("Cantidad") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        OutlinedTextField(
            value = precio,
            onValueChange = { precio = it },
            label = { Text("Precio unitario") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )
        OutlinedTextField(
            value = observacion,
            onValueChange = { observacion = it },
            label = { Text("Observaciones") },
            modifier = Modifier.fillMaxWidth()
        )

        Button(onClick = {
            if (productoSeleccionado != null && cantidad.isNotBlank() && precio.isNotBlank()) {
                val unidad = UnitType.fromCode(productoSeleccionado?.unidad)
                items = items + ItemConsignacion(
                    productoId = productoSeleccionado!!.id,
                    cantidad = cantidad.toDouble(),
                    unidad = unidad.name,
                    precioUnitario = precio.toDouble()
                )
                productoSeleccionado = null
                cantidad = ""
                precio = ""
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Agregar item")
        }

        if (items.isNotEmpty()) {
            Text("Items (${items.size})", style = MaterialTheme.typography.titleSmall)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.height(180.dp)) {
                items(items) { item ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("${item.cantidad} × ${productos.firstOrNull { it.id == item.productoId }?.nombre.orEmpty()}")
                            Text("$${item.precioUnitario}")
                        }
                    }
                }
            }
        }

        Button(onClick = {
            if (comprador.isNotBlank() && items.isNotEmpty() && fechaPago != null) {
                onGuardar(comprador, items, fechaPago!!, observacion)
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar consignación")
        }

        Button(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
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
                        val unidad = UnitType.fromCode(productoSeleccionado?.unidad)
                        items = items + ItemConsignacion(
                            productoId = productoSeleccionado!!.id,
                            cantidad = cantidad.toDouble(),
                            unidad = unidad.name,
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
