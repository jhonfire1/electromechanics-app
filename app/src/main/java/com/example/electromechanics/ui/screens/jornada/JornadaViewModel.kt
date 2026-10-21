package com.example.electromechanics.ui.screens.jornada

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.local.entity.RegistroJornadaEntity
import com.example.electromechanics.data.repository.JornadaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class JornadaViewModel(
    private val repository: JornadaRepository,
    private val idEmpleado: String
) : ViewModel() {

    private val _uiState = MutableStateFlow(JornadaUiState())
    val uiState: StateFlow<JornadaUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observarPorEmpleado(idEmpleado).collect { lista ->
                _uiState.update { it.copy(registros = lista) }
            }
        }
    }

    fun onObteniendoUbicacion() {
        _uiState.update { it.copy(isObteniendoUbicacion = true, error = null) }
    }

    fun onUbicacionError(mensaje: String) {
        _uiState.update { it.copy(isObteniendoUbicacion = false, error = mensaje) }
    }

    // La pantalla entrega las coordenadas; el ViewModel decide el tipo y guarda en Room
    fun registrarMarcacion(latitud: Double, longitud: Double) {
        val tipo = _uiState.value.proximoTipo
        viewModelScope.launch {
            val ahora = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            repository.registrar(
                RegistroJornadaEntity(
                    idRegistro = UUID.randomUUID().toString(),
                    idEmpleado = idEmpleado,
                    tipoMarcacion = tipo,
                    fechaHoraMarcacion = ahora,
                    latitudMarcacion = latitud,
                    longitudMarcacion = longitud
                )
            )
            _uiState.update {
                it.copy(
                    isObteniendoUbicacion = false,
                    error = null,
                    mensaje = if (tipo == "ENTRADA") "Entrada registrada correctamente" else "Salida registrada correctamente",
                    mensajeVisible = true
                )
            }
        }
    }

    fun onMensajeConsumido() {
        _uiState.update { it.copy(mensajeVisible = false) }
    }
}
