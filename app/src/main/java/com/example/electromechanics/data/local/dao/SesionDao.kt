package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.SesionEntity

@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(sesion: SesionEntity)

    @Query("SELECT * FROM sesion WHERE id = 1 LIMIT 1")
    suspend fun obtener(): SesionEntity?

    @Query("DELETE FROM sesion")
    suspend fun borrar()
}
