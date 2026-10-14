package com.example.electromechanics.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "evidencias")
data class EvidenciaEntity(
    @PrimaryKey
    val idEvidencia: String,
    val idServicio: String,
    val tipo: String, // INICIAL o FINAL
    val fotoPath: String,
    val fechaHora: String
)