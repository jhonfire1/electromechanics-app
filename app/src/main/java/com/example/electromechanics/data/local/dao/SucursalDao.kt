package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.SucursalEntity

@Dao
interface SucursalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSucursal(sucursal: SucursalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarSucursales(sucursales: List<SucursalEntity>)

    @Query("SELECT * FROM sucursales WHERE idSucursal = :id LIMIT 1")
    suspend fun obtenerPorId(id: String): SucursalEntity?

    @Query("SELECT * FROM sucursales")
    suspend fun obtenerTodas(): List<SucursalEntity>
}