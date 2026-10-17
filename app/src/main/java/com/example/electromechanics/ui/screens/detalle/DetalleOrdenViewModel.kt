package com.example.electromechanics.ui.screens.detalle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DetalleOrdenViewModel(
    private val repository: OrdenTrabajoRepository,
    private val idOrden: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetalleOrdenUiState(isLoading = true))
    val uiState: StateFlow<DetalleOrdenUiState> = _uiState.asStateFlow()

    init {
        cargarDetalleOrden()
    }

    private fun cargarDetalleOrden() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            combine(
                repository.obtenerOrdenPorId(idOrden),
                repository.obtenerActividadesPorOrden(idOrden)
            ) { orden, actividades ->
                Pair(orden, actividades)
            }
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Error al cargar la orden")
                    }
                }
                .collect { (orden, actividades) ->
                    _uiState.update {
                        it.copy(
                            orden = orden,
                            actividades = actividades,
                            isLoading = false
                        )
                    }
                }
        }
    }

    fun cambiarEstadoOrden(nuevoEstado: String) {
        viewModelScope.launch {
            repository.actualizarEstado(idOrden, nuevoEstado)
        }
    }

    fun onTareaChanged(tarea: String) {
        _uiState.update { it.copy(nuevaTarea = tarea) }
    }

    fun onRepuestosChanged(repuestos: String) {
        _uiState.update { it.copy(nuevosRepuestos = repuestos) }
    }

    fun onComentariosChanged(comentarios: String) {
        _uiState.update { it.copy(nuevosComentarios = comentarios) }
    }

    fun registrarActividad() {
        val currentState = _uiState.value
        if (currentState.nuevaTarea.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isGuardandoActividad = true) }

            val nuevaActividad = ActividadEntity(
                idOrden = idOrden,
                detalleTareaEjecutada = currentState.nuevaTarea.trim(),
                repuestosUtilizados = currentState.nuevosRepuestos.trim(),
                comentariosTecnicos = currentState.nuevosComentarios.trim()
            )

            repository.registrarActividad(nuevaActividad)

            _uiState.update {
                it.copy(
                    nuevaTarea = "",
                    nuevosRepuestos = "",
                    nuevosComentarios = "",
                    isGuardandoActividad = false
                )
            }
        }
    }
}