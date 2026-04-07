package com.example.weedventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.data.repository.ItemVenta
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.ui.viewmodel.VentaViewModel
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VentaScreen(
    navController: NavHostController,
    viewModel: VentaViewModel,
    productoViewModel: ProductoViewModel
) {
    var mostrarFormulario by remember { mutableStateOf(false) }
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()
    val productos by productoViewModel.todosProductos.collectAsState()
    
    if (mostrarFormulario) {
        FormularioVenta(
            productos = productos,
            onGuardarNormal = { cliente, items ->
                viewModel.registrarVentaNormal(cliente, items)
                mostrarFormulario = false
            },
            onGuardarConsumo = { items ->
                viewModel.registrarConsumoPropio(items)
                mostrarFormulario = false
            },
            onCancelar = { mostrarFormulario = false }
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Ventas") },
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
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                if (mensajeExito != null) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            mensajeExito!!,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                
                Button(
                    onClick = { mostrarFormulario = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nueva Venta")
                }
            }
        }
    }
}

@Composable
fun FormularioVenta(
    productos: List<Producto>,
    onGuardarNormal: (cliente: String?, items: List<ItemVenta>) -> Unit,
    onGuardarConsumo: (items: List<ItemVenta>) -> Unit,
    onCancelar: () -> Unit
) {
    var tipoVenta by remember { mutableStateOf("normal") } // normal o consumo
    var cliente by remember { mutableStateOf("") }
    var productoSeleccionado by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var precio by remember { mutableStateOf("") }
    var items by remember { mutableStateOf<List<ItemVenta>>(emptyList()) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Venta") }
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
            // Tipo de venta
            Text("Tipo de Venta:", style = MaterialTheme.typography.labelMedium)
            Button(
                onClick = { tipoVenta = "normal" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (tipoVenta == "normal") "✓ Venta Normal" else "Venta Normal")
            }
            Button(
                onClick = { tipoVenta = "consumo" },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (tipoVenta == "consumo") "✓ Consumo Propio" else "Consumo Propio")
            }
            
            if (tipoVenta == "normal") {
                OutlinedTextField(
                    value = cliente,
                    onValueChange = { cliente = it },
                    label = { Text("Cliente (Opcional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
            
            Text("Agregar Productos:", style = MaterialTheme.typography.labelMedium)
            
            OutlinedTextField(
                value = productoSeleccionado?.nombre ?: "",
                onValueChange = {},
                label = { Text("Producto") },
                modifier = Modifier.fillMaxWidth(),
                readOnly = true
            )
            
            OutlinedTextField(
                value = cantidad,
                onValueChange = { cantidad = it },
                label = { Text("Cantidad") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
            
            OutlinedTextField(
                value = precio,
                onValueChange = { precio = it },
                label = { Text("Precio Unitario") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Button(
                onClick = {
                    if (productoSeleccionado != null && cantidad.isNotBlank() && precio.isNotBlank()) {
                        items = items + ItemVenta(
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
                Text("Agregar Item")
            }
            
            Text("Items: ${items.size}", style = MaterialTheme.typography.labelSmall)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = {
                    if (items.isNotEmpty()) {
                        if (tipoVenta == "normal") {
                            onGuardarNormal(if (cliente.isBlank()) null else cliente, items)
                        } else {
                            onGuardarConsumo(items)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar Venta")
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
