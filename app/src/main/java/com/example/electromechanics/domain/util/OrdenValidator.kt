package com.example.electromechanics.domain.util

import java.text.SimpleDateFormat
import java.util.Locale

object OrdenValidator {
    fun texto(valor: String, etiqueta: String, maximo: Int): String? = when {
        valor.isBlank() -> "Ingresa $etiqueta."
        valor.trim().length > maximo -> "Máximo $maximo caracteres."
        else -> null
    }

    fun fecha(valor: String): String? {
        val fecha = valor.trim()
        val formato = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).apply { isLenient = false }
        val valida = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}").matches(fecha) &&
            runCatching { formato.parse(fecha) != null }.getOrDefault(false)
        return if (valida) null else "Ingresa una fecha válida (AAAA-MM-DD)."
    }
}
