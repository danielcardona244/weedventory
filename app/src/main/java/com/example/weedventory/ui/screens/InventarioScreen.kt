@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.weedventory.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.weedventory.data.local.db.entity.Producto
import com.example.weedventory.ui.viewmodel.ProductoViewModel

@Composable
fun InventarioScreen(
    productoViewModel: ProductoViewModel
) {
    val productos by productoViewModel.todosProductos.collectAsState()
    val productosConStockBajo by productoViewModel.productosConStockBajo.collectAsState()

    var mostrarFormulario by remember { mutableStateOf(false) }
    var productoEditando by remember { mutableStateOf<Producto?>(null) }
    var nombre by remember { mutableStateOf("") }
    var costo by remember { mutableStateOf("") }
    var precioVenta by remember { mutableStateOf("") }
    var stockActual by remember { mutableStateOf("") }
    var stockMinimo by remember { mutableStateOf("") }
    var mensaje by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Inventario",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (mensaje != null) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    mensaje!!,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (productosConStockBajo.isNotEmpty()) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "⚠ ${productosConStockBajo.size} productos con stock bajo",
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (mostrarFormulario) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = if (productoEditando == null) "Agregar producto" else "Editar producto",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = costo,
                        onValueChange = { costo = it },
                        label = { Text("Costo") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = precioVenta,
                        onValueChange = { precioVenta = it },
                        label = { Text("Precio de venta") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stockActual,
                        onValueChange = { stockActual = it },
                        label = { Text("Stock actual") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = stockMinimo,
                        onValueChange = { stockMinimo = it },
                        label = { Text("Stock mínimo") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                mostrarFormulario = false
                                productoEditando = null
                                nombre = ""
                                costo = ""
                                precioVenta = ""
                                stockActual = ""
                                stockMinimo = ""
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = {
                                if (nombre.isNotBlank() && precioVenta.isNotBlank()) {
                                    val costoDouble = costo.toDoubleOrNull() ?: 0.0
                                    val precioDouble = precioVenta.toDoubleOrNull() ?: 0.0
                                    val stockActualInt = stockActual.toIntOrNull() ?: 0
                                    val stockMinimoInt = stockMinimo.toIntOrNull() ?: 0
                                    if (productoEditando == null) {
                                        productoViewModel.crearProducto(
                                            nombre = nombre,
                                            descripcion = "",
                                            costo = costoDouble,
                                            precioVenta = precioDouble,
                                            stockActual = stockActualInt,
                                            stockMinimo = stockMinimoInt
                                        )
                                    } else {
                                        productoViewModel.actualizarProducto(
                                            productoEditando!!.copy(
                                                nombre = nombre,
                                                costo = costoDouble,
                                                precioVenta = precioDouble,
                                                stockActual = stockActualInt,
                                                stockMinimo = stockMinimoInt
                                            )
                                        )
                                    }
                                    mostrarFormulario = false
                                    productoEditando = null
                                    nombre = ""
                                    costo = ""
                                    precioVenta = ""
                                    stockActual = ""
                                    stockMinimo = ""
                                    mensaje = "Producto guardado correctamente"
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(if (productoEditando == null) "Guardar" else "Actualizar")
                        }
                    }
                }
            }

        } else {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Productos activos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Button(onClick = { mostrarFormulario = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Agregar")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (productos.isEmpty()) {
                Text("No hay productos. Presiona Agregar para comenzar.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(productos) { producto ->
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(producto.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                        Text("Precio: $${producto.precioVenta}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    Text("Stock: ${producto.stockActual}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Mínimo: ${producto.stockMinimo}", style = MaterialTheme.typography.bodySmall)
                                    Row {
                                        IconButton(onClick = { productoViewModel.agregarUnidad(producto.id) }) {
                                            Icon(Icons.Default.Add, contentDescription = "Agregar unidad")
                                        }
                                        IconButton(onClick = {
                                            productoEditando = producto
                                            mostrarFormulario = true
                                            nombre = producto.nombre
                                            costo = producto.costo.toString()
                                            precioVenta = producto.precioVenta.toString()
                                            stockActual = producto.stockActual.toString()
                                            stockMinimo = producto.stockMinimo.toString()
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar")
                                        }
                                        IconButton(onClick = {
                                            productoViewModel.eliminarProducto(producto.id)
                                            mensaje = "Producto eliminado"
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
