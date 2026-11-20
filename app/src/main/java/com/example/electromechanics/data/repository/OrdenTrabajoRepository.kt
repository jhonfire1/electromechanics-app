package com.example.electromechanics.data.repository

import androidx.room.withTransaction
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import com.example.electromechanics.data.remote.CambioEstadoDto
import com.example.electromechanics.data.remote.CrearOrdenDto
import com.example.electromechanics.data.remote.OrdenDto
import com.example.electromechanics.data.remote.OrdenesApi
import com.example.electromechanics.data.session.SessionManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class OrdenTrabajoRepository(
    private val database: AppDatabase,
    private val api: OrdenesApi?,
    private val demoLocal: DemoLocal? = null
) {
    private val ordenTrabajoDao = database.ordenTrabajoDao()
    private val actividadDao = database.actividadDao()
    private val sincronizacion = Mutex()

    suspend fun crear(orden: CrearOrdenDto): OrdenDto =
        demoLocal?.crearOrden(orden) ?: checkNotNull(api).crear(SessionManager.autorizacion(), orden)

    suspend fun actualizarAsignadas(idEmpleado: String) = sincronizacion.withLock {
        // En la demo, la hoja de ruta ya observa las órdenes guardadas en Room.
        if (demoLocal != null) return@withLock
        val ordenes = checkNotNull(api).asignadas(SessionManager.autorizacion()).map { it.aEntidad() }
        check(ordenes.all { it.idEmpleadoAsignado == idEmpleado })
        database.withTransaction {
            ordenTrabajoDao.eliminarOrdenesDelTecnico(idEmpleado)
            ordenTrabajoDao.insertAll(ordenes)
        }
    }

    fun obtenerOrdenesPorTecnico(idEmpleado: String): Flow<List<OrdenTrabajoEntity>> {
        return ordenTrabajoDao.obtenerOrdenesPorTecnico(idEmpleado)
    }

    fun obtenerOrdenPorId(idOrden: String): Flow<OrdenTrabajoEntity?> {
        return ordenTrabajoDao.obtenerOrdenPorId(idOrden)
    }

    suspend fun actualizarEstado(idOrden: String, nuevoEstado: String) = sincronizacion.withLock {
        if (demoLocal != null) {
            demoLocal.cambiarEstado(idOrden, nuevoEstado)
            return@withLock
        }
        val orden = checkNotNull(api).cambiarEstado(
            SessionManager.autorizacion(), idOrden, CambioEstadoDto(nuevoEstado)
        )
        ordenTrabajoDao.insertAll(listOf(orden.aEntidad()))
    }

    // Métodos con ActividadDao
    fun obtenerActividadesPorOrden(idOrden: String): Flow<List<ActividadEntity>> {
        return actividadDao.obtenerPorOrden(idOrden)
    }

    suspend fun registrarActividad(actividad: ActividadEntity) {
        actividadDao.insertarActividad(actividad)
    }

    private fun OrdenDto.aEntidad() = OrdenTrabajoEntity(
        idOrden = id,
        idEmpleadoAsignado = tecnicoId.toString(),
        clienteNombre = cliente,
        direccion = direccion,
        descripcionProblema = descripcion,
        estado = estado,
        fechaProgramada = fecha
    )
}
