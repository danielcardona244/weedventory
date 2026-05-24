package com.example.weedventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedventory.data.repository.VentaRepository
import com.example.weedventory.data.repository.ItemVenta
import com.example.weedventory.data.local.db.entity.Venta
import com.example.weedventory.data.local.db.entity.TipoVenta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VentaViewModel(
    private val ventaRepository: VentaRepository
) : ViewModel() {
    
    val todasVentas: StateFlow<List<Venta>> = ventaRepository
        .getTodosVentas()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    
    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()
    
    private val _ventaId = MutableStateFlow<Long?>(null)
    val ventaId: StateFlow<Long?> = _ventaId.asStateFlow()
    
    fun registrarVentaNormal(
        cliente: String?,
        items: List<ItemVenta>,
        observacion: String = ""
    ) {
        viewModelScope.launch {
            val resultado = ventaRepository.registrarVentaNormal(
                cliente = cliente,
                items = items,
                observacion = observacion
            )
            
            resultado.onSuccess { id ->
                _ventaId.value = id
                _mensajeExito.value = "Venta registrada correctamente"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al registrar venta"
                _mensajeExito.value = null
            }
        }
    }
    
    fun registrarConsumoPropio(
        items: List<ItemVenta>,
        observacion: String = ""
    ) {
        viewModelScope.launch {
            val resultado = ventaRepository.registrarConsumoPropio(
                items = items,
                observacion = observacion
            )
            
            resultado.onSuccess { id ->
                _ventaId.value = id
                _mensajeExito.value = "Consumo propio registrado correctamente"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al registrar consumo"
                _mensajeExito.value = null
            }
        }
    }
    
    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
        _ventaId.value = null
    }
    
    fun eliminarVenta(id: Long) {
        viewModelScope.launch {
            val resultado = ventaRepository.eliminarVenta(id)
            resultado.onSuccess {
                _mensajeExito.value = "Venta eliminada correctamente"
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al eliminar venta"
            }
        }
    }
}
