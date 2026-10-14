package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registros_jornada")
data class RegistroJornadaEntity(
    @PrimaryKey
    val idRegistro: String,
    val idEmpleado: String,
    val tipoMarcacion: String, // ENTRADA o SALIDA
    val fechaHoraMarcacion: String,
    val latitudMarcacion: Double,
    val longitudMarcacion: Double
)