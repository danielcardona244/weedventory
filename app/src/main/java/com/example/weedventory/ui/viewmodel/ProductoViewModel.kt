package com.example.weedventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedventory.data.repository.ProductoRepository
import com.example.weedventory.data.local.db.entity.Producto
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val productoRepository: ProductoRepository
) : ViewModel() {
    
    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito
    
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError
    
    val todosProductos: StateFlow<List<Producto>> = productoRepository
        .getAllActivos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val productosConStockBajo: StateFlow<List<Producto>> = productoRepository
        .getProductosConStockBajo()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    init {
        viewModelScope.launch {
            productoRepository.consolidarProductosDuplicadosActivos()
        }
    }
    
    fun crearProducto(
        nombre: String,
        descripcion: String,
        costo: Double,
        precioVenta: Double,
        stockActual: Int = 0,
        stockMinimo: Int = 0,
        unidad: String = "KILO"
    ) {
        viewModelScope.launch {
            try {
                productoRepository.crearProducto(
                    nombre = nombre,
                    descripcion = descripcion,
                    costo = costo,
                    precioVenta = precioVenta,
                    stockActual = stockActual,
                    stockMinimo = stockMinimo,
                    unidad = unidad
                ).also {
                    _mensajeExito.value = "Producto creado exitosamente"
                }
            } catch (e: Exception) {
                _mensajeError.value = "Error al crear producto: ${e.message}"
            }
        }
    }

    fun actualizarProducto(producto: Producto) {
        viewModelScope.launch {
            try {
                productoRepository.actualizarProducto(producto)
                _mensajeExito.value = "Producto actualizado exitosamente"
            } catch (e: Exception) {
                _mensajeError.value = "Error al actualizar producto: ${e.message}"
            }
        }
    }

    fun desactivarProducto(id: Long) {
        viewModelScope.launch {
            try {
                productoRepository.desactivarProducto(id)
                _mensajeExito.value = "Producto desactivado exitosamente"
            } catch (e: Exception) {
                _mensajeError.value = "Error al desactivar producto: ${e.message}"
            }
        }
    }

    fun limpiarMensajes() {
        _mensajeExito.value = null
        _mensajeError.value = null
    }

    fun agregarUnidad(productoId: Long) {
        viewModelScope.launch {
            val producto = productoRepository.getById(productoId) ?: return@launch
            productoRepository.actualizarStock(productoId, producto.stockActual + 1)
        }
    }
}
