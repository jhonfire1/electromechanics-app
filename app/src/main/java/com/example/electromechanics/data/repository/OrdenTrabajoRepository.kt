package com.example.electromechanics.data.repository

import com.example.electromechanics.data.local.dao.OrdenTrabajoDao
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import kotlinx.coroutines.flow.Flow

class OrdenTrabajoRepository(
    private val ordenTrabajoDao: OrdenTrabajoDao
) {
    fun obtenerOrdenesPorTecnico(idEmpleado: String): Flow<List<OrdenTrabajoEntity>> {
        return ordenTrabajoDao.obtenerOrdenesPorTecnico(idEmpleado)
    }
}