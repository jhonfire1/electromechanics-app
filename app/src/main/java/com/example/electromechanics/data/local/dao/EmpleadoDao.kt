package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.EmpleadoEntity

@Dao
interface EmpleadoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEmpleado(empleado: EmpleadoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarEmpleados(empleados: List<EmpleadoEntity>)

    @Query("SELECT * FROM empleados WHERE correoCorporativo = :correo LIMIT 1")
    suspend fun obtenerPorCorreo(correo: String): EmpleadoEntity?

    @Query("SELECT * FROM empleados WHERE idEmpleado = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): EmpleadoEntity?
}