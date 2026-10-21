package com.example.electromechanics.domain.util

object ActividadValidator {

    fun validarTarea(tarea: String): ValidationResult {
        val texto = tarea.trim()
        return when {
            texto.isEmpty() -> ValidationResult.Error("La tarea ejecutada es obligatoria")
            texto.length < 5 -> ValidationResult.Error("Describe la tarea con al menos 5 caracteres")
            texto.length > 200 -> ValidationResult.Error("La tarea no puede superar los 200 caracteres")
            else -> ValidationResult.Success
        }
    }

    // Campo opcional: solo se valida el largo máximo
    fun validarRepuestos(repuestos: String): ValidationResult {
        return if (repuestos.trim().length > 150) {
            ValidationResult.Error("Los repuestos no pueden superar los 150 caracteres")
        } else {
            ValidationResult.Success
        }
    }

    // Campo opcional: solo se valida el largo máximo
    fun validarComentarios(comentarios: String): ValidationResult {
        return if (comentarios.trim().length > 300) {
            ValidationResult.Error("Los comentarios no pueden superar los 300 caracteres")
        } else {
            ValidationResult.Success
        }
    }
}
