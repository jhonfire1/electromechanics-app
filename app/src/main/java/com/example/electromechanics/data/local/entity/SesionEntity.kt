package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Sesión autenticada del dispositivo. Siempre hay como máximo UNA fila (id fijo = 1):
 * un dispositivo tiene una sola sesión activa a la vez.
 */
@Entity(tableName = "sesion")
data class SesionEntity(
    @PrimaryKey
    val id: Int = ID_UNICO,
    val idUsuario: String,
    /** RUT normalizado (sin puntos ni guion), tal como se envía al backend. */
    val username: String,
    val rol: String,
    val token: String,
    /** Expiración del JWT en milisegundos (claim "exp" * 1000). Solo para control de interfaz. */
    val expiraEnMs: Long,
    /** "algoritmo:iteraciones:saltHex:hashHex". Nunca se guarda la contraseña. */
    val verificadorLocal: String
) {
    companion object {
        const val ID_UNICO = 1
    }
}
