package com.example.electromechanics.data.repository

import androidx.room.withTransaction
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.local.entity.EmpleadoEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import com.example.electromechanics.data.local.entity.SesionEntity
import com.example.electromechanics.data.remote.CrearOrdenDto
import com.example.electromechanics.data.remote.OrdenDto
import com.example.electromechanics.data.remote.TecnicoDto
import com.example.electromechanics.data.session.SessionManager
import com.example.electromechanics.domain.util.LoginValidator
import com.example.electromechanics.domain.util.PasswordHasher
import com.example.electromechanics.domain.util.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CuentaLocalRegistrada : IllegalStateException("Este RUT ya tiene una cuenta.")

/** Datos y operaciones de la demostración dentro de Room. */
class DemoLocal(private val database: AppDatabase) {
    private val empleados = database.empleadoDao()
    private val ordenes = database.ordenTrabajoDao()

    suspend fun preparar() = database.withTransaction {
        // Conserva los clientes que ya iniciaron sesión en este teléfono.
        val clientesGuardados = database.sesionDao().obtenerCuentasPorRol(Roles.CLIENTE)
        empleados.insertarSiNoExisten(clientesGuardados.map { cuenta ->
            cuentaCliente(cuenta.username, cuenta.verificadorLocal)
        }.filter { buscarCuenta(it.rut) == null })
        ordenes.insertarSiNoExisten(ordenesDeMuestra())
    }

    suspend fun autenticar(rut: String, password: String): SesionEntity? {
        val cuenta = buscarCuenta(rut) ?: return null
        val valida = withContext(Dispatchers.Default) {
            PasswordHasher.verificar(password, cuenta.passwordHash)
        }
        return if (valida && cuenta.rol in Roles.PERMITIDOS) cuenta.comoSesion() else null
    }

    suspend fun recuperarSesion(guardada: SesionEntity): SesionEntity? {
        val cuenta = buscarCuenta(guardada.username) ?: return null
        return cuenta.takeIf { it.rol == guardada.rol && it.rol in Roles.PERMITIDOS }?.comoSesion()
    }

    suspend fun registrarCliente(rut: String, password: String): SesionEntity {
        val normalizado = LoginValidator.normalizarRut(rut)
        require(LoginValidator.validarRut(normalizado) == ValidationResult.Success)
        require(password.length >= 8 && LoginValidator.validarPassword(password) == ValidationResult.Success)
        val verificador = withContext(Dispatchers.Default) { PasswordHasher.crearVerificador(password) }
        val cuenta = cuentaCliente(normalizado, verificador)
        return database.withTransaction {
            if (buscarCuenta(normalizado) != null) throw CuentaLocalRegistrada()
            empleados.insertarEmpleado(cuenta)
            cuenta.comoSesion()
        }
    }

    fun listarTecnicos(): List<TecnicoDto> {
        exigirRol(Roles.SUPERVISOR)
        return tecnicosDeMuestra.map { tecnico ->
            TecnicoDto(tecnico.idEmpleado.toLong(), tecnico.rut)
        }
    }

    suspend fun crearOrden(datos: CrearOrdenDto): OrdenDto = database.withTransaction {
        exigirRol(Roles.SUPERVISOR)
        require(tecnicosDeMuestra.any { it.idEmpleado == datos.tecnicoId.toString() }) {
            "Selecciona un técnico válido."
        }
        val nueva = OrdenTrabajoEntity(
            idOrden = datos.id,
            idEmpleadoAsignado = datos.tecnicoId.toString(),
            clienteNombre = datos.cliente.trim(),
            direccion = datos.direccion.trim(),
            descripcionProblema = datos.descripcion.trim(),
            estado = "PENDIENTE",
            fechaProgramada = datos.fecha
        )
        val anterior = ordenes.buscarOrdenPorId(datos.id)
        if (anterior != null) {
            check(anterior.copy(estado = nueva.estado) == nueva) { "La orden ya existe con otros datos." }
            anterior.comoDto()
        } else {
            ordenes.insertarSiNoExisten(listOf(nueva))
            nueva.comoDto()
        }
    }

    suspend fun cambiarEstado(idOrden: String, nuevoEstado: String) = database.withTransaction {
        exigirRol(Roles.TECNICO)
        val orden = checkNotNull(ordenes.buscarOrdenPorId(idOrden)) { "Orden no encontrada." }
        check(orden.idEmpleadoAsignado == SessionManager.idEmpleadoActual) { "La orden pertenece a otro técnico." }
        if (orden.estado == nuevoEstado) return@withTransaction
        require(
            (orden.estado == "PENDIENTE" && nuevoEstado == "EN_PROCESO") ||
                (orden.estado == "EN_PROCESO" && nuevoEstado == "COMPLETADA")
        ) { "Cambio de estado inválido." }
        ordenes.cambiarEstado(idOrden, nuevoEstado)
    }

    private fun exigirRol(rol: String) {
        check(SessionManager.rolActual == rol && SessionManager.tokenActual == TOKEN) {
            "Tu cuenta no tiene permiso para realizar esta acción."
        }
    }

    // Los RUT de prueba conservan sus claves e identificadores incluso en instalaciones
    // con empleados creados por versiones antiguas.
    private suspend fun buscarCuenta(rut: String): EmpleadoEntity? =
        cuentasDeMuestra.firstOrNull { it.rut == rut } ?: empleados.obtenerPorId("cliente-$rut")

    private fun EmpleadoEntity.comoSesion() = SesionEntity(
        idUsuario = idEmpleado,
        username = rut,
        rol = rol,
        token = TOKEN,
        expiraEnMs = Long.MAX_VALUE,
        verificadorLocal = passwordHash
    )

    private fun OrdenTrabajoEntity.comoDto() = OrdenDto(
        idOrden, idEmpleadoAsignado.toLong(), clienteNombre, direccion, descripcionProblema,
        fechaProgramada, estado
    )

    private fun cuentaCliente(rut: String, verificador: String) =
        cuenta("cliente-$rut", rut, "Cliente", Roles.CLIENTE, verificador)

    private fun ordenesDeMuestra() = listOf(
        ordenMuestra("01", "Demo Taller Norte", "Sector de pruebas A",
            "Revisión del motor eléctrico y prueba de funcionamiento.", -2, "COMPLETADA"),
        ordenMuestra("02", "Demo Planta Central", "Sector de pruebas B",
            "Diagnóstico de bomba. Falta la prueba final de presión.", 0, "EN_PROCESO"),
        ordenMuestra("03", "Demo Bodega Sur", "Sector de pruebas C",
            "Inspección programada del tablero de control.", 2, "PENDIENTE"),
        ordenMuestra("04", "Demo Subestación Oriente", "Sector de pruebas D",
            "Inspección de contactores y protecciones del tablero.", 1, "PENDIENTE", tecnicoId = "5")
    )

    private fun ordenMuestra(
        sufijo: String, nombre: String, direccion: String, trabajo: String, dias: Int, estado: String,
        tecnicoId: String = "1"
    ): OrdenTrabajoEntity {
        val fecha = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, dias) }
        return OrdenTrabajoEntity(
            "4ca19990-d752-418b-9bb2-d5fd3ac25a$sufijo", tecnicoId, nombre, direccion, trabajo, estado,
            SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(fecha.time)
        )
    }

    companion object {
        // Marca interna de una sesión de Room. El modo conectado usa JWT del servidor.
        const val TOKEN = "sesion-local"

        private fun cuenta(id: String, rut: String, nombre: String, rol: String, verificador: String) =
            EmpleadoEntity(id, rut, nombre, rol, "", "", verificador)

        private val cuentasDeMuestra = listOf(
            cuenta("2", "111111111", "Supervisor de prueba", Roles.SUPERVISOR,
                "PBKDF2WithHmacSHA1:120000:c3492e4ba26942beb6da3ff3dbd640fb:12c6586e3a04fc3643ae80ce323ae3cb886f3c68d312ef974855b435af37af2b"),
            cuenta("1", "123456785", "Técnico 1", Roles.TECNICO,
                "PBKDF2WithHmacSHA1:120000:2376b6a36b50d8c2a833cb95001bf421:94a09ee8d24980dee7a85d738a1b06e68438052075a0a184a30467d7b8555981"),
            cuenta("5", "222222222", "Técnico 2", Roles.TECNICO,
                "PBKDF2WithHmacSHA1:120000:57545ac45776162fb4b2d4eb7d6b5cbc:ecaeb9d2feadc6dbfa63f150d750d030ea4a603b609d79041f354e541a15e02c"),
            cuenta("6", "333333333", "Técnico 3", Roles.TECNICO,
                "PBKDF2WithHmacSHA1:120000:abb19fa64b67c1a0a31123cd7a076276:f0385a4607bf466d61fb5929eee89858c0bf7f72294f9fb0edf011bcda918495")
        )
        private val tecnicosDeMuestra = cuentasDeMuestra.filter { it.rol == Roles.TECNICO }
    }
}
