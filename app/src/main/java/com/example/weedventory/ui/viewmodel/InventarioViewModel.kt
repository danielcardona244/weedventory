package com.example.weedventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedventory.data.repository.InventarioRepository
import com.example.weedventory.data.local.db.entity.MovimientoInventario
import com.example.weedventory.data.local.db.entity.TipoMovimiento
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class InventarioViewModel(
    private val inventarioRepository: InventarioRepository
) : ViewModel() {
    
    val todosMovimientos: StateFlow<List<MovimientoInventario>> = inventarioRepository
        .getTodosMovimientos()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    
    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()
    
    fun registrarEntrada(productoId: Long, cantidad: Int, nota: String = "") {
        viewModelScope.launch {
            val resultado = inventarioRepository.registrarEntrada(
                productoId = productoId,
                cantidad = cantidad,
                nota = nota
            )
            
            resultado.onSuccess {
                _mensajeExito.value = "Entrada registrada correctamente"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al registrar entrada"
                _mensajeExito.value = null
            }
        }
    }
    
    fun registrarSalida(
        productoId: Long,
        cantidad: Int,
        tipoMovimiento: TipoMovimiento,
        nota: String = ""
    ) {
        viewModelScope.launch {
            val resultado = inventarioRepository.registrarSalida(
                productoId = productoId,
                cantidad = cantidad,
                tipoMovimiento = tipoMovimiento,
                nota = nota
            )
            
            resultado.onSuccess {
                _mensajeExito.value = "Salida registrada correctamente"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al registrar salida"
                _mensajeExito.value = null
            }
        }
    }
    
    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
    }
}
