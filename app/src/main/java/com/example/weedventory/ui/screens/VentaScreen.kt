@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weedventory.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.data.repository.ItemConsignacion
import com.example.weedventory.data.repository.ItemVenta
import com.example.weedventory.ui.viewmodel.ConsignacionViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import java.time.Instant
import java.util.Calendar

@Composable
fun VentaScreen(
    viewModel: VentaViewModel,
    consignacionViewModel: ConsignacionViewModel,
    productoViewModel: ProductoViewModel
) {
    val mensajeVentaError by viewModel.mensajeError.collectAsState()
    val mensajeVentaExito by viewModel.mensajeExito.collectAsState()
    val mensajeConsignacionError by consignacionViewModel.mensajeError.collectAsState()
    val mensajeConsignacionExito by consignacionViewModel.mensajeExito.collectAsState()
    val productos by productoViewModel.todosProductos.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Ventas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        if (mensajeVentaError != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeVentaError!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (mensajeVentaExito != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeVentaExito!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (mensajeConsignacionError != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeConsignacionError!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (mensajeConsignacionExito != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(mensajeConsignacionExito!!, modifier = Modifier.padding(12.dp), color = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (mostrarFormulario) {
            VentaForm(
                productos = productos,
                onGuardarNormal = { cliente, items ->
                    viewModel.registrarVentaNormal(cliente, items)
                },
                onGuardarConsignacion = { comprador, items, fechaPago, observacion ->
                    consignacionViewModel.registrarConsignacion(comprador, items, Instant.now().toEpochMilli(), fechaPago, observacion)
                },
                onCancelar = { mostrarFormulario = false }
            )
        } else {
            Button(onClick = { mostrarFormulario = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar venta / consignación")
            }
        }
    }
}

@Composable
fun VentaForm(
    productos: List<Producto>,
    onGuardarNormal: (cliente: String?, items: List<ItemVenta>) -> Unit,
    onGuardarConsignacion: (comprador: String, items: List<ItemConsignacion>, fechaPago: Long, observacion: String) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var tipoVenta by remember { mutableStateOf("normal") }
    var cliente by remember { mutableStateOf("") }
    var comprador by remember { mutableStateOf("") }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ItemVenta>>(emptyList()) }
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

    Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxSize()) {
        Text("Tipo de operación", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(onClick = { tipoVenta = "normal" }, modifier = Modifier.weight(1f)) {
                Text(if (tipoVenta == "normal") "✓ Venta normal" else "Venta normal")
            }
            Button(onClick = { tipoVenta = "consignacion" }, modifier = Modifier.weight(1f)) {
                Text(if (tipoVenta == "consignacion") "✓ Consignación" else "Consignación")
            }
        }

        if (tipoVenta == "normal") {
            OutlinedTextField(
                value = cliente,
                onValueChange = { cliente = it },
                label = { Text("Cliente (opcional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        } else {
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
        }

        Text("Agregar producto", style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = !expanded }) {
            TextField(
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                value = productoSeleccionado?.nombre ?: "Seleccionar producto",
                onValueChange = {},
                label = { Text("Producto") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                items = items + ItemVenta(
                    productoId = productoSeleccionado!!.id,
                    cantidad = cantidad.toInt(),
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
            if (items.isNotEmpty()) {
                if (tipoVenta == "normal") {
                    onGuardarNormal(if (cliente.isBlank()) null else cliente, items)
                } else if (comprador.isNotBlank() && fechaPago != null) {
                    onGuardarConsignacion(
                        comprador,
                        items.map { item ->
                            ItemConsignacion(
                                productoId = item.productoId,
                                cantidad = item.cantidad,
                                precioUnitario = item.precioUnitario
                            )
                        },
                        fechaPago!!,
                        observacion
                    )
                }
            }
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Guardar operación")
        }

        Button(onClick = onCancelar, modifier = Modifier.fillMaxWidth()) {
            Text("Cancelar")
        }
    }
}
