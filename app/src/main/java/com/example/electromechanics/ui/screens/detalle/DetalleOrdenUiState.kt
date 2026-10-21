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
    val isGuardandoActividad: Boolean = false,

    // Errores de validación por campo (null = sin error)
    val tareaError: String? = null,
    val repuestosError: String? = null,
    val comentariosError: String? = null,

    // Se pone en true al guardar una actividad (dispara vibración y animación)
    val actividadGuardada: Boolean = false
)