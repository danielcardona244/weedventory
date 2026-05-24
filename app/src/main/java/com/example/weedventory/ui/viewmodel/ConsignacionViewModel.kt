package com.example.weedventory.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.weedventory.data.repository.ConsignacionRepository
import com.example.weedventory.data.repository.ItemConsignacion
import com.example.weedventory.data.local.db.entity.Consignacion
import com.example.weedventory.data.local.db.entity.EstadoConsignacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ConsignacionViewModel(
    private val consignacionRepository: ConsignacionRepository
) : ViewModel() {
    
    val todasConsignaciones: StateFlow<List<Consignacion>> = consignacionRepository
        .getAll()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    val consignacionesPendientes: StateFlow<List<Consignacion>> = consignacionRepository
        .getConsignacionesPorEstado(EstadoConsignacion.PENDIENTE)
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    
    private val _mensajeError = MutableStateFlow<String?>(null)
    val mensajeError: StateFlow<String?> = _mensajeError.asStateFlow()
    
    private val _mensajeExito = MutableStateFlow<String?>(null)
    val mensajeExito: StateFlow<String?> = _mensajeExito.asStateFlow()
    
    private val _consignacionId = MutableStateFlow<Long?>(null)
    val consignacionId: StateFlow<Long?> = _consignacionId.asStateFlow()
    
    fun registrarConsignacion(
        comprador: String,
        items: List<ItemConsignacion>,
        fechaEntrega: Long,
        fechaRecordatorio: Long,
        observaciones: String = ""
    ) {
        viewModelScope.launch {
            val resultado = consignacionRepository.registrarConsignacion(
                comprador = comprador,
                items = items,
                fechaEntrega = fechaEntrega,
                fechaRecordatorio = fechaRecordatorio,
                observaciones = observaciones
            )
            
            resultado.onSuccess { id ->
                _consignacionId.value = id
                _mensajeExito.value = "Consignación registrada correctamente"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al registrar consignación"
                _mensajeExito.value = null
            }
        }
    }
    
    fun marcarSaldada(consignacionId: Long) {
        viewModelScope.launch {
            val resultado = consignacionRepository.marcarSaldada(consignacionId)
            
            resultado.onSuccess {
                _mensajeExito.value = "Consignación marcada como saldada"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al actualizar consignación"
            }
        }
    }
    
    fun registrarPagoPartial(consignacionId: Long, montoPagado: Double) {
        viewModelScope.launch {
            val resultado = consignacionRepository.registrarPagoPartial(
                consignacionId = consignacionId,
                montoPagado = montoPagado
            )
            
            resultado.onSuccess {
                _mensajeExito.value = "Pago registrado correctamente"
                _mensajeError.value = null
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al registrar pago"
            }
        }
    }
    
    fun limpiarMensajes() {
        _mensajeError.value = null
        _mensajeExito.value = null
        _consignacionId.value = null
    }
    
    fun eliminarConsignacion(id: Long) {
        viewModelScope.launch {
            val resultado = consignacionRepository.eliminarConsignacion(id)
            resultado.onSuccess {
                _mensajeExito.value = "Consignación eliminada correctamente"
            }
            resultado.onFailure { error ->
                _mensajeError.value = error.message ?: "Error al eliminar consignación"
            }
        }
    }
}
