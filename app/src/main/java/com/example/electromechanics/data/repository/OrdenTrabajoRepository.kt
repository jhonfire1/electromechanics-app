package com.example.electromechanics.data.repository

import com.example.electromechanics.data.local.dao.ActividadDao
import com.example.electromechanics.data.local.dao.OrdenTrabajoDao
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import kotlinx.coroutines.flow.Flow

class OrdenTrabajoRepository(
    private val ordenTrabajoDao: OrdenTrabajoDao,
    private val actividadDao: ActividadDao
) {
    fun obtenerOrdenesPorTecnico(idEmpleado: String): Flow<List<OrdenTrabajoEntity>> {
        return ordenTrabajoDao.obtenerOrdenesPorTecnico(idEmpleado)
    }

    fun obtenerOrdenPorId(idOrden: String): Flow<OrdenTrabajoEntity?> {
        return ordenTrabajoDao.obtenerOrdenPorId(idOrden)
    }

    suspend fun actualizarEstado(idOrden: String, nuevoEstado: String) {
        ordenTrabajoDao.actualizarEstado(idOrden, nuevoEstado)
    }

    // Métodos con ActividadDao
    fun obtenerActividadesPorOrden(idOrden: String): Flow<List<ActividadEntity>> {
        return actividadDao.obtenerPorOrden(idOrden)
    }

    suspend fun registrarActividad(actividad: ActividadEntity) {
        actividadDao.insertarActividad(actividad)
    }
}