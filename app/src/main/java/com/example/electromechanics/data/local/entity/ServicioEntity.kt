package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "servicios")
data class ServicioEntity(
    @PrimaryKey
    val idServicio: String,
    val idEmpleadoAsignado: String,
    val idSucursal: String,
    val fechaProgramada: String,
    val descripcionTrabajo: String,
    val estado: String // PENDIENTE, EN_PROCESO, FINALIZADO
)