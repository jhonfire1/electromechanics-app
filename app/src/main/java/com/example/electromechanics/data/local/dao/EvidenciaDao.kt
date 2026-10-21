package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EvidenciaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEvidencia(evidencia: EvidenciaEntity)

    @Query("SELECT * FROM evidencias WHERE idServicio = :idServicio")
    suspend fun obtenerPorServicio(idServicio: String): List<EvidenciaEntity>

    // Versión reactiva: la pantalla se actualiza sola al guardar una foto nueva
    @Query("SELECT * FROM evidencias WHERE idServicio = :idServicio ORDER BY fechaHora DESC")
    fun observarPorServicio(idServicio: String): Flow<List<EvidenciaEntity>>
}
