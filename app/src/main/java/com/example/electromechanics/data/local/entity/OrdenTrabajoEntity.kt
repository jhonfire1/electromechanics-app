package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ordenes_trabajo")
data class OrdenTrabajoEntity(
    @PrimaryKey val idOrden: String,
    val idEmpleadoAsignado: String,
    val clienteNombre: String,
    val direccion: String,
    val descripcionProblema: String,
    val estado: String, // "PENDIENTE", "EN_PROCESO", "COMPLETADA"
    val fechaProgramada: String
)