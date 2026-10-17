package com.example.electromechanics.ui.screens.detalle

import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity

data class DetalleOrdenUiState(
    val orden: OrdenTrabajoEntity? = null,
    val actividades: List<ActividadEntity> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Campos del formulario para registrar nueva actividad
    val nuevaTarea: String = "",
    val nuevosRepuestos: String = "",
    val nuevosComentarios: String = "",
    val isGuardandoActividad: Boolean = false
)