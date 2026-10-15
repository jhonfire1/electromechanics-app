package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.EvidenciaEntity

@Dao
interface EvidenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvidencia(evidencia: EvidenciaEntity)

    @Query("SELECT * FROM evidencias WHERE idServicio = :idServicio")
    suspend fun obtenerPorServicio(idServicio: String): List<EvidenciaEntity>
}