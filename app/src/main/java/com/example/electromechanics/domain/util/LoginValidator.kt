package com.example.electromechanics.domain.util

sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

object LoginValidator {

    fun validarRut(rut: String): ValidationResult {
        val rutLimpio = rut.replace(".", "").replace("-", "").trim().uppercase()

        if (rutLimpio.isEmpty()) {
            return ValidationResult.Error("El RUT no puede estar vacío")
        }
        if (rutLimpio.length < 8) {
            return ValidationResult.Error("Formato de RUT incompleto")
        }

        val cuerpo = rutLimpio.dropLast(1)
        val dvIngresado = rutLimpio.last()

        if (cuerpo.any { !it.isDigit() }) {
            return ValidationResult.Error("El cuerpo del RUT solo debe contener números")
        }

        // Algoritmo Módulo 11 para Chile
        var suma = 0
        var multiplicador = 2
        for (i in cuerpo.length - 1 downTo 0) {
            suma += (cuerpo[i].toString().toInt()) * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        val resto = suma % 11
        val dvEsperadoCalculado = 11 - resto

        val dvEsperado = when (dvEsperadoCalculado) {
            11 -> '0'
            10 -> 'K'
            else -> dvEsperadoCalculado.toString()[0]
        }

        return if (dvIngresado == dvEsperado) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("RUT inválido (dígito verificador incorrecto)")
        }
    }

    fun validarPassword(password: String): ValidationResult {
        return when {
            password.isEmpty() -> ValidationResult.Error("La contraseña no puede estar vacía")
            password.length < 4 -> ValidationResult.Error("La contraseña debe tener al menos 4 caracteres")
            else -> ValidationResult.Success
        }
    }
}