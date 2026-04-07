package com.example.weedventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.weedventory.data.local.db.entity.MovimientoInventario
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import com.example.weedventory.ui.viewmodel.InventarioViewModel
import com.example.weedventory.ui.viewmodel.ProductoViewModel
import com.example.weedventory.utils.DateFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventarioScreen(
    navController: NavHostController,
    viewModel: InventarioViewModel,
    productoViewModel: ProductoViewModel
) {
    var selectedTab by remember { mutableStateOf(0) }
    val movimientos by viewModel.todosMovimientos.collectAsState()
    val mensajeError by viewModel.mensajeError.collectAsState()
    val mensajeExito by viewModel.mensajeExito.collectAsState()
    val productos by productoViewModel.todosProductos.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventario") },
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
            // Mostrar mensajes
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
            
            if (selectedTab == 0) {
                FormularioEntrada(
                    productos = productos,
                    onRegistrar = { productoId, cantidad, nota ->
                        viewModel.registrarEntrada(productoId, cantidad, nota)
                    }
                )
            } else {
                HistorialMovimientos(movimientos)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormularioEntrada(
    productos: List<Producto>,
    onRegistrar: (productoId: Long, cantidad: Int, nota: String) -> Unit
) {
    var selectedProducto by remember { mutableStateOf<Producto?>(null) }
    var cantidad by remember { mutableStateOf("") }
    var nota by remember { mutableStateOf("") }
    var expandedProductos by remember { mutableStateOf(false) }
    
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Registrar Entrada de Inventario", style = MaterialTheme.typography.titleMedium)
        
        ExposedDropdownMenuBox(
            expanded = expandedProductos,
            onExpandedChange = { expandedProductos = !expandedProductos }
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = selectedProducto?.nombre ?: "Seleccionar producto",
                onValueChange = {},
                label = { Text("Producto") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProductos) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expandedProductos,
                onDismissRequest = { expandedProductos = false },
            ) {
                productos.forEach { producto ->
                    DropdownMenuItem(
                        text = { Text(producto.nombre) },
                        onClick = {
                            selectedProducto = producto
                            expandedProductos = false
                        },
                    )
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
            value = nota,
            onValueChange = { nota = it },
            label = { Text("Nota") },
            modifier = Modifier.fillMaxWidth()
        )
        
        Button(
            onClick = {
                if (selectedProducto != null && cantidad.isNotBlank()) {
                    onRegistrar(selectedProducto!!.id, cantidad.toInt(), nota)
                    cantidad = ""
                    nota = ""
                    selectedProducto = null
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Registrar Entrada")
        }
    }
}

@Composable
fun HistorialMovimientos(movimientos: List<MovimientoInventario>) {
    if (movimientos.isEmpty()) {
        Text("No hay movimientos registrados")
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(movimientos) { movimiento ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                movimiento.tipoMovimiento.name,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Cantidad: ${movimiento.cantidad}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = androidx.compose.ui.Alignment.End) {
                            Text(
                                DateFormatter.formatarFechaHora(movimiento.fecha),
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (movimiento.nota.isNotBlank()) {
                                Text(
                                    movimiento.nota,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
