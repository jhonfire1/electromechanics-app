package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cuentas_locales")
data class CuentaLocalEntity(
    @PrimaryKey val username: String,
    val idUsuario: String,
    val rol: String,
    val token: String,
    val expiraEnMs: Long,
    val verificadorLocal: String
) {
    fun comoSesion() = SesionEntity(
        username = username,
        idUsuario = idUsuario,
        rol = rol,
        token = token,
        expiraEnMs = expiraEnMs,
        verificadorLocal = verificadorLocal
    )
}
