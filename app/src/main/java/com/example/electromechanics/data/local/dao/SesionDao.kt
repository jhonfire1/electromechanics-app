package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.electromechanics.data.local.entity.CuentaLocalEntity
import com.example.electromechanics.data.local.entity.SesionEntity

@Dao
interface SesionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(sesion: SesionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardarCuenta(cuenta: CuentaLocalEntity)

    @Transaction
    suspend fun guardarLogin(cuenta: CuentaLocalEntity) {
        guardarCuenta(cuenta)
        guardar(cuenta.comoSesion())
    }

    @Query("SELECT * FROM sesion WHERE id = 1 LIMIT 1")
    suspend fun obtener(): SesionEntity?

    @Query("SELECT * FROM cuentas_locales WHERE username = :username")
    suspend fun obtenerCuenta(username: String): CuentaLocalEntity?

    @Query("DELETE FROM cuentas_locales WHERE username = :username")
    suspend fun borrarCuenta(username: String)

    @Query("DELETE FROM cuentas_locales WHERE expiraEnMs <= :ahora")
    suspend fun borrarCuentasVencidas(ahora: Long)

    @Query("DELETE FROM sesion")
    suspend fun borrar()
}
