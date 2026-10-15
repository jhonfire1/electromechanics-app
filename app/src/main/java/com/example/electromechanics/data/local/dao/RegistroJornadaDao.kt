package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.RegistroJornadaEntity

@Dao
interface RegistroJornadaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarRegistro(registro: RegistroJornadaEntity)

    @Query("SELECT * FROM registros_jornada WHERE idEmpleado = :idEmpleado ORDER BY fechaHoraMarcacion DESC")
    suspend fun obtenerPorEmpleado(idEmpleado: String): List<RegistroJornadaEntity>
}