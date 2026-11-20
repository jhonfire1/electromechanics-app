package com.example.electromechanics.ui.screens.hojaruta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import com.example.electromechanics.data.remote.mensajeSolicitud
import com.example.electromechanics.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class HojaRutaViewModel(
    private val repository: OrdenTrabajoRepository,
    private val idEmpleadoTecnico: String = SessionManager.idEmpleadoActual ?: ""
) : ViewModel() {

    private val _uiState = MutableStateFlow(HojaRutaUiState(isLoading = true))
    val uiState: StateFlow<HojaRutaUiState> = _uiState.asStateFlow()
    private var carga: Job? = null

    init {
        viewModelScope.launch {
            repository.obtenerOrdenesPorTecnico(idEmpleadoTecnico)
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Error al cargar órdenes")
                    }
                }
                .collect { listaOrdenes ->
                    _uiState.update {
                        it.copy(ordenes = listaOrdenes)
                    }
                }
        }
        cargarOrdenes()
    }

    fun cargarOrdenes() {
        if (carga?.isActive == true) return
        carga = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.actualizarAsignadas(idEmpleadoTecnico)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(errorMessage = mensajeSolicitud(e, "No se pudieron actualizar las órdenes."))
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun seleccionarFiltro(filtro: FiltroEstado) {
        _uiState.update { it.copy(filtroSeleccionado = filtro) }
    }
}
