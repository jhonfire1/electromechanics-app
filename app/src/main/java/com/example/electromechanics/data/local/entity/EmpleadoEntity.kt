package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "empleados")
data class EmpleadoEntity(
    @PrimaryKey
    val idEmpleado: String,
    val rut: String,
    val nombreCompleto: String,
    val rol: String,
    val especialidadTecnica: String,
    val correoCorporativo: String,
    val passwordHash: String
)