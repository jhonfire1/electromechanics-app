package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.electromechanics.data.local.entity.ServicioEntity

@Dao
interface ServicioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarServicio(servicio: ServicioEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarServicios(servicios: List<ServicioEntity>)

    @Query("SELECT * FROM servicios WHERE idEmpleadoAsignado = :idEmpleado")
    suspend fun obtenerPorEmpleado(idEmpleado: String): List<ServicioEntity>

    @Query("SELECT * FROM servicios WHERE idServicio = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): ServicioEntity?

    @Update
    suspend fun actualizarServicio(servicio: ServicioEntity)
}