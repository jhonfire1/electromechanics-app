package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sucursales")
data class SucursalEntity(
    @PrimaryKey
    val idSucursal: String,
    val nombreCliente: String,
    val direccionSucursal: String,
    val latitudRef: Double,
    val longitudRef: Double
)
