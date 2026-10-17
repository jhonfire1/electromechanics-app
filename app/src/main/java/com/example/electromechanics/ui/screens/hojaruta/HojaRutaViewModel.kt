package com.example.electromechanics.ui.screens.hojaruta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HojaRutaViewModel(
    private val repository: OrdenTrabajoRepository,
    private val idEmpleadoTecnico: String = "emp-001"
) : ViewModel() {

    private val _uiState = MutableStateFlow(HojaRutaUiState(isLoading = true))
    val uiState: StateFlow<HojaRutaUiState> = _uiState.asStateFlow()

    init {
        cargarOrdenes()
    }

    fun cargarOrdenes() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            repository.obtenerOrdenesPorTecnico(idEmpleadoTecnico)
                .catch { exception ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = exception.message ?: "Error al cargar órdenes")
                    }
                }
                .collect { listaOrdenes ->
                    _uiState.update {
                        it.copy(ordenes = listaOrdenes, isLoading = false)
                    }
                }
        }
    }

    fun seleccionarFiltro(filtro: FiltroEstado) {
        _uiState.update { it.copy(filtroSeleccionado = filtro) }
    }
}