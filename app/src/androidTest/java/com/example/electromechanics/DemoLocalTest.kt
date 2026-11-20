package com.example.electromechanics

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.local.entity.EmpleadoEntity
import com.example.electromechanics.data.remote.CrearOrdenDto
import com.example.electromechanics.data.repository.AuthRepository
import com.example.electromechanics.data.repository.CuentaLocalRegistrada
import com.example.electromechanics.data.repository.DemoLocal
import com.example.electromechanics.data.repository.LoginResult
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import com.example.electromechanics.data.repository.Roles
import com.example.electromechanics.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class DemoLocalTest {
    @Test
    fun cuentasYOrdenesFuncionanSinApiYConservanSusCambios() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        val demo = DemoLocal(database)
        val auth = AuthRepository(database.sesionDao(), null, demo)
        val ordenes = OrdenTrabajoRepository(database, null, demo)
        try {
            database.empleadoDao().insertarEmpleado(
                EmpleadoEntity("tecnico-anterior", "123456785", "Cuenta anterior", Roles.TECNICO,
                    "", "", "verificador-anterior")
            )
            assertNull(auth.restaurarSesion())
            assertEquals(LoginResult.CredencialesInvalidas, auth.login("123456785", "incorrecta"))
            assertEquals(LoginResult.Exito(Roles.TECNICO, true), auth.login("123456785", "12345"))
            val muestra = ordenes.obtenerOrdenesPorTecnico("1").first()
            assertEquals(3, muestra.size)
            assertEquals(setOf("PENDIENTE", "EN_PROCESO", "COMPLETADA"), muestra.map { it.estado }.toSet())
            val muestraTecnico2 = ordenes.obtenerOrdenesPorTecnico("5").first().single()
            assertEquals("Demo Subestación Oriente", muestraTecnico2.clienteNombre)
            assertEquals("PENDIENTE", muestraTecnico2.estado)
            assertTrue(ordenes.obtenerOrdenesPorTecnico("6").first().isEmpty())
            val pendiente = muestra.single { it.estado == "PENDIENTE" }
            ordenes.actualizarEstado(pendiente.idOrden, "EN_PROCESO")
            SessionManager.cerrarSesion()
            assertEquals(Roles.TECNICO, auth.restaurarSesion()?.rol)
            assertEquals("EN_PROCESO", ordenes.obtenerOrdenPorId(pendiente.idOrden).first()?.estado)

            auth.cerrarSesion()
            assertEquals(LoginResult.Exito(Roles.SUPERVISOR, true), auth.login("111111111", "DemoSupervisor26"))
            assertEquals(listOf("123456785", "222222222", "333333333"), auth.listarTecnicos().map { it.username })
            val orden = CrearOrdenDto(UUID.randomUUID().toString(), 5, "Prueba", "Taller", "Revisión", "2026-11-20")
            assertEquals(ordenes.crear(orden), ordenes.crear(orden))
            auth.cerrarSesion()
            assertEquals(LoginResult.Exito(Roles.TECNICO, true), auth.login("222222222", "DemoTecnico22"))
            assertEquals(setOf(orden.id, muestraTecnico2.idOrden),
                ordenes.obtenerOrdenesPorTecnico("5").first().map { it.idOrden }.toSet())

            auth.cerrarSesion()
            val cliente = "444444444"
            val clave = "ClienteDemoLocal"
            assertEquals(LoginResult.Exito(Roles.CLIENTE, true), auth.registrarCliente(cliente, clave))
            auth.cerrarSesion()
            assertTrue(runCatching { auth.registrarCliente(cliente, clave) }.exceptionOrNull() is CuentaLocalRegistrada)
            assertEquals(LoginResult.Exito(Roles.CLIENTE, true), auth.login(cliente, clave))
        } finally {
            SessionManager.cerrarSesion()
            database.close()
        }
    }
}
