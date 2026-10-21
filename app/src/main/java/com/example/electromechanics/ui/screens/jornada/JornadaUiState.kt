package com.example.electromechanics.ui.screens.jornada

import com.example.electromechanics.data.local.entity.RegistroJornadaEntity

data class JornadaUiState(
    val registros: List<RegistroJornadaEntity> = emptyList(),
    val isObteniendoUbicacion: Boolean = false,
    val error: String? = null,

    // Confirmación animada tras marcar (el texto se conserva mientras la animación de salida corre)
    val mensaje: String = "",
    val mensajeVisible: Boolean = false
) {
    // Alterna: si la última marcación fue ENTRADA, la siguiente es SALIDA
    val proximoTipo: String
        get() = if (registros.firstOrNull()?.tipoMarcacion == "ENTRADA") "SALIDA" else "ENTRADA"
}
