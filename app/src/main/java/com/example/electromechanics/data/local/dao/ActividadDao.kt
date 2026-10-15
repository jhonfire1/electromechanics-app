package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.ActividadEntity

@Dao
interface ActividadDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarActividad(actividad: ActividadEntity)

    @Query("SELECT * FROM actividades WHERE idServicio = :idServicio")
    suspend fun obtenerPorServicio(idServicio: String): List<ActividadEntity>
}