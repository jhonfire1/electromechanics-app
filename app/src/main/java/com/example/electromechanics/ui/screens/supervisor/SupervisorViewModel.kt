package com.example.electromechanics.ui.screens.supervisor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.remote.CrearOrdenDto
import com.example.electromechanics.data.remote.OrdenDto
import com.example.electromechanics.data.remote.TecnicoDto
import com.example.electromechanics.data.remote.mensajeSolicitud
import com.example.electromechanics.data.repository.AuthRepository
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import com.example.electromechanics.domain.util.OrdenValidator
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

enum class CampoOrden(val etiqueta: String) {
    CLIENTE("Cliente"), DIRECCION("Dirección"), DESCRIPCION("Descripción"), FECHA("Fecha (AAAA-MM-DD)")
}

data class SupervisorUiState(
    val tecnicos: List<TecnicoDto> = emptyList(),
    val tecnico: TecnicoDto? = null,
    val campos: Map<CampoOrden, String> = emptyMap(),
    val errores: Map<CampoOrden, String> = emptyMap(),
    val errorTecnico: String? = null,
    val error: String? = null,
    val cargandoTecnicos: Boolean = false,
    val guardando: Boolean = false,
    val ordenCreada: OrdenDto? = null
)

class SupervisorViewModel(
    private val auth: AuthRepository,
    private val ordenes: OrdenTrabajoRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SupervisorUiState())
    val uiState = _uiState.asStateFlow()
    private var solicitudPendiente: CrearOrdenDto? = null

    init {
        cargarTecnicos()
    }

    fun cargarTecnicos() {
        if (_uiState.value.cargandoTecnicos || _uiState.value.guardando) return
        _uiState.update { it.copy(cargandoTecnicos = true, error = null) }
        viewModelScope.launch {
            try {
                val tecnicos = auth.listarTecnicos()
                _uiState.update { state ->
                    state.copy(
                        tecnicos = tecnicos,
                        tecnico = tecnicos.firstOrNull { it.id == state.tecnico?.id }
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = mensajeSolicitud(e, "No se pudieron cargar los técnicos.")) }
            } finally {
                _uiState.update { it.copy(cargandoTecnicos = false) }
            }
        }
    }

    fun seleccionarTecnico(tecnico: TecnicoDto) {
        _uiState.update { it.copy(tecnico = tecnico, errorTecnico = null) }
    }

    fun editar(campo: CampoOrden, valor: String) {
        _uiState.update { it.copy(campos = it.campos + (campo to valor), errores = it.errores - campo) }
    }

    fun asignarOrden() {
        val estado = _uiState.value
        if (estado.guardando || estado.cargandoTecnicos) return
        fun valor(campo: CampoOrden) = estado.campos[campo].orEmpty().trim()
        val cliente = valor(CampoOrden.CLIENTE)
        val direccion = valor(CampoOrden.DIRECCION)
        val descripcion = valor(CampoOrden.DESCRIPCION)
        val fecha = valor(CampoOrden.FECHA)
        val tecnico = estado.tecnico
        val errores = listOfNotNull(
            OrdenValidator.texto(cliente, "el cliente", 120)?.let { CampoOrden.CLIENTE to it },
            OrdenValidator.texto(direccion, "la dirección", 200)?.let { CampoOrden.DIRECCION to it },
            OrdenValidator.texto(descripcion, "la descripción", 500)?.let { CampoOrden.DESCRIPCION to it },
            OrdenValidator.fecha(fecha)?.let { CampoOrden.FECHA to it }
        ).toMap()

        if (tecnico == null || errores.isNotEmpty()) {
            _uiState.update {
                it.copy(errores = errores, errorTecnico = if (tecnico == null) "Selecciona un técnico." else null)
            }
            return
        }

        val anterior = solicitudPendiente
        val datos = CrearOrdenDto(
            id = anterior?.id ?: UUID.randomUUID().toString(),
            tecnicoId = tecnico.id,
            cliente = cliente,
            direccion = direccion,
            descripcion = descripcion,
            fecha = fecha
        )
        // Solo reutilizamos el identificador cuando se vuelve a enviar el mismo formulario.
        val solicitud = if (anterior == null || anterior == datos) datos else datos.copy(id = UUID.randomUUID().toString())
        solicitudPendiente = solicitud
        _uiState.update { it.copy(guardando = true, error = null, ordenCreada = null) }
        viewModelScope.launch {
            try {
                val orden = ordenes.crear(solicitud)
                solicitudPendiente = null
                _uiState.update { it.copy(campos = emptyMap(), tecnico = null, ordenCreada = orden) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(error = mensajeSolicitud(e, "No se pudo asignar la orden.")) }
            } finally {
                _uiState.update { it.copy(guardando = false) }
            }
        }
    }
}
