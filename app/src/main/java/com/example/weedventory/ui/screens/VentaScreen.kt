@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weedventory.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.weedventory.utils.DateFormatter
import com.example.weedventory.utils.UnitType
import java.time.Instant
import java.util.Calendar
import kotlinx.coroutines.delay

@Composable
fun VentaScreen(
    viewModel: VentaViewModel,
    productoViewModel: ProductoViewModel,
    consignacionViewModel: ConsignacionViewModel
) {
    val mensajeVentaError by viewModel.mensajeError.collectAsState()
    val mensajeVentaExito by viewModel.mensajeExito.collectAsState()
    val productos by productoViewModel.todosProductos.collectAsState()
    var mostrarFormulario by remember { mutableStateOf(false) }

    LaunchedEffect(mensajeVentaExito, mensajeVentaError) {
        if (mensajeVentaExito != null || mensajeVentaError != null) {
            delay(3000)
            viewModel.limpiarMensajes()
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(text = "Ventas", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

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

        if (mostrarFormulario) {
            VentaForm(
                productos = productos,
                onGuardar = { isConsignacion, comprador, fechaLimite, item ->
                    if (isConsignacion) {
                        if (!comprador.isNullOrBlank() && fechaLimite != null) {
                            val itemConsignacion = ItemConsignacion(
                                productoId = item.productoId,
                                cantidad = item.cantidad,
                                unidad = item.unidad,
                                precioUnitario = item.precioUnitario
                            )
                            consignacionViewModel.registrarConsignacion(
                                comprador,
                                listOf(itemConsignacion),
                                Instant.now().toEpochMilli(),
                                fechaLimite
                            )
                            mostrarFormulario = false
                        }
                    } else {
                        viewModel.registrarVentaNormal(
                            cliente = if (comprador.isNullOrBlank()) null else comprador,
                            items = listOf(item)
                        )
                        mostrarFormulario = false
                    }
                },
                onCancelar = { mostrarFormulario = false }
            )
        } else {
            Button(onClick = { mostrarFormulario = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Registrar venta")
            }
        }
    }
}

@Composable
fun VentaForm(
    productos: List<Producto>,
    onGuardar: (isConsignacion: Boolean, comprador: String?, fechaLimite: Long?, item: ItemVenta) -> Unit,
    onCancelar: () -> Unit
) {
    val context = LocalContext.current
    var cliente by remember { mutableStateOf("") }
    var isConsignacion by remember { mutableStateOf(false) }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var expanded by remember { mutableStateOf(false) }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var observacion by remember { mutableStateOf("") }
    var fechaLimite by remember { mutableStateOf<Long?>(null) }
    var mostrarFechaPicker by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = cliente,
            onValueChange = { cliente = it },
            label = { Text("Cliente / Comprador") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Es consignacion?")
            Spacer(modifier = Modifier.width(8.dp))
            Switch(checked = isConsignacion, onCheckedChange = { isConsignacion = it })
        }

        if (isConsignacion) {
            Button(onClick = { mostrarFechaPicker = true }, modifier = Modifier.fillMaxWidth()) {
                Text(fechaLimite?.let { "Fecha limite: ${DateFormatter.formatarFecha(it)}" } ?: "Seleccionar fecha limite")
            }
            if (mostrarFechaPicker) {
                val cal = Calendar.getInstance()
                DatePickerDialog(context, { _, y, m, d ->
                    val c = Calendar.getInstance()
                    c.set(y, m, d, 23, 59, 59)
                    fechaLimite = c.timeInMillis
                    mostrarFechaPicker = false
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
            }
        }

        Text("Producto", style = MaterialTheme.typography.titleMedium)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = productoSeleccionado?.nombre.orEmpty(),
                onValueChange = {},
                label = { Text("Producto") },
                placeholder = { Text("Seleccionar producto") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                singleLine = true
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                productos.forEach { producto ->
                    DropdownMenuItem(
                        text = { Text("${producto.nombre} - Stock: ${producto.stockActual}") },
                        onClick = {
                            productoSeleccionado = producto
                            expanded = false
                        }
                    )
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

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onCancelar, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
            Button(
                onClick = {
                    val producto = productoSeleccionado
                    val cantidadVenta = cantidad.toDoubleOrNull()
                    val precioVenta = precio.toDoubleOrNull()

                    if (producto != null && cantidadVenta != null && precioVenta != null) {
                        val unidad = UnitType.fromCode(producto.unidad)
                        onGuardar(
                            isConsignacion,
                            if (cliente.isBlank()) null else cliente,
                            fechaLimite,
                            ItemVenta(
                                productoId = producto.id,
                                cantidad = cantidadVenta,
                                unidad = unidad.name,
                                precioUnitario = precioVenta
                            )
                        )
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar venta")
            }
        }
    }
}
