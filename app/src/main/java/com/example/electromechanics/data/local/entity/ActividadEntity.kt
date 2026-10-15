package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey
    val idActividad: String,
    val idServicio: String,
    val detalleTareaEjecutada: String,
    val repuestosUtilizados: String,
    val horaRegistro: String,
    val comentariosTecnicos: String
)