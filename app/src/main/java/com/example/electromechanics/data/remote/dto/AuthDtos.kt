package com.example.electromechanics.data.remote.dto

/** Cuerpo de POST /api/auth/login. "username" lleva el RUT normalizado. */
data class LoginRequestDto(
    val username: String,
    val password: String
)

/**
 * Respuesta de login (AuthResponse del backend).
 * Gson ignora la nulabilidad de Kotlin y puede dejar un campo en null si falta en el JSON,
 * por eso los textos son nulables aquí y se validan en AuthRepository.
 */
data class LoginResponseDto(
    val token: String?,
    val id: Long,
    val username: String?,
    val role: String?
)
