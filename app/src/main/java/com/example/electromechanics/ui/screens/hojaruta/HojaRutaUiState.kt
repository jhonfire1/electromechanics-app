package com.example.electromechanics.ui.screens.hojaruta

import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity

enum class FiltroEstado {
    TODAS,
    PENDIENTE,
    EN_PROCESO,
    COMPLETADA
}

data class HojaRutaUiState(
    val ordenes: List<OrdenTrabajoEntity> = emptyList(),
    val filtroSeleccionado: FiltroEstado = FiltroEstado.TODAS,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
) {
    val ordenesFiltradas: List<OrdenTrabajoEntity>
        get() = when (filtroSeleccionado) {
            FiltroEstado.TODAS -> ordenes
            FiltroEstado.PENDIENTE -> ordenes.filter { it.estado == "PENDIENTE" }
            FiltroEstado.EN_PROCESO -> ordenes.filter { it.estado == "EN_PROCESO" }
            FiltroEstado.COMPLETADA -> ordenes.filter { it.estado == "COMPLETADA" }
        }
}