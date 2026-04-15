package com.example.weedventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedventory.data.repository.ProductoRepository
import com.example.weedventory.data.local.db.entity.Producto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {
    
    val todosProductos: StateFlow<List<Producto>> = productoRepository
        .getAllActivos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val productosConStockBajo: StateFlow<List<Producto>> = productoRepository
        .getProductosConStockBajo()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    fun crearProducto(
        nombre: String,
        descripcion: String,
        costo: Double,
        precioVenta: Double,
        stockActual: Int = 0,
        stockMinimo: Int = 0
    ) {
        viewModelScope.launch {
            productoRepository.crearProducto(
                nombre = nombre,
                descripcion = descripcion,
                costo = costo,
                precioVenta = precioVenta,
                stockMinimo = stockMinimo
            ).also { productoId ->
                if (stockActual > 0 && productoId > 0) {
                    productoRepository.actualizarStock(productoId, stockActual)
                }
            }
        }
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            productoRepository.actualizarProducto(producto)
        }
    }

    fun eliminarProducto(id: Long) {
        viewModelScope.launch {
            productoRepository.desactivarProducto(id)
        }
    }

    fun agregarUnidad(productoId: Long) {
        viewModelScope.launch {
            val producto = productoRepository.getById(productoId) ?: return@launch
            productoRepository.actualizarStock(productoId, producto.stockActual + 1)
        }
    }
}
