package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "actividades")
data class ActividadEntity(
    @PrimaryKey
    val idActividad: String = UUID.randomUUID().toString(),
    val idOrden: String, // Usamos idOrden para coincidir con OrdenTrabajoEntity
    val detalleTareaEjecutada: String,
    val repuestosUtilizados: String = "",
    val horaRegistro: String = System.currentTimeMillis().toString(),
    val comentariosTecnicos: String = ""
)