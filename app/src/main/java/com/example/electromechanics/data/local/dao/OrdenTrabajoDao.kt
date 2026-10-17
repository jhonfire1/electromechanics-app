package com.example.electromechanics.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OrdenTrabajoDao {

    @Query("SELECT * FROM ordenes_trabajo WHERE idEmpleadoAsignado = :idEmpleado ORDER BY fechaProgramada DESC")
    fun obtenerOrdenesPorTecnico(idEmpleado: String): Flow<List<OrdenTrabajoEntity>>

    @Query("SELECT * FROM ordenes_trabajo WHERE idOrden = :idOrden LIMIT 1")
    fun obtenerOrdenPorId(idOrden: String): Flow<OrdenTrabajoEntity?>

    @Query("UPDATE ordenes_trabajo SET estado = :nuevoEstado WHERE idOrden = :idOrden")
    suspend fun actualizarEstado(idOrden: String, nuevoEstado: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(ordenes: List<OrdenTrabajoEntity>)
}