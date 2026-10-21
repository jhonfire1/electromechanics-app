package com.example.electromechanics.data.repository

import com.example.electromechanics.data.local.dao.RegistroJornadaDao
import com.example.electromechanics.data.local.entity.RegistroJornadaEntity
import kotlinx.coroutines.flow.Flow

class JornadaRepository(private val registroJornadaDao: RegistroJornadaDao) {

    fun observarPorEmpleado(idEmpleado: String): Flow<List<RegistroJornadaEntity>> {
        return registroJornadaDao.observarPorEmpleado(idEmpleado)
    }

    suspend fun registrar(registro: RegistroJornadaEntity) {
        registroJornadaDao.insertarRegistro(registro)
    }
}
