package com.example.electromechanics.data.repository

import com.example.electromechanics.data.local.dao.EvidenciaDao
import com.example.electromechanics.data.local.entity.EvidenciaEntity
import kotlinx.coroutines.flow.Flow

class EvidenciaRepository(private val evidenciaDao: EvidenciaDao) {

    fun observarPorOrden(idOrden: String): Flow<List<EvidenciaEntity>> {
        return evidenciaDao.observarPorServicio(idOrden)
    }

    suspend fun registrar(evidencia: EvidenciaEntity) {
        evidenciaDao.insertarEvidencia(evidencia)
    }
}
