package com.example.electromechanics

import android.util.Base64
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.remote.AuthApi
import com.example.electromechanics.data.remote.TecnicoDto
import com.example.electromechanics.data.remote.dto.LoginRequestDto
import com.example.electromechanics.data.remote.dto.LoginResponseDto
import com.example.electromechanics.data.repository.AuthRepository
import com.example.electromechanics.data.repository.LoginResult
import com.example.electromechanics.data.session.SessionManager
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class SesionLocalTest {
    @Test
    fun ambosRolesPuedenVolverAEntrarSinRedHastaQueVenzaLaCuenta() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        var ahora = 1_000L
        var sinConexion = false
        val payload = Base64.encodeToString(
            "{\"exp\":60}".toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING
        )
        val api = object : AuthApi {
            override suspend fun login(request: LoginRequestDto): LoginResponseDto {
                if (sinConexion) throw IOException("Sin conexión de prueba")
                val (id, rol) = if (request.username == "123456785") 1L to "TECNICO" else 2L to "SUPERVISOR"
                return LoginResponseDto("cabecera.$payload.firma", id, request.username, rol)
            }

            override suspend fun tecnicos(token: String): List<TecnicoDto> = emptyList()
        }
        val auth = AuthRepository(database.sesionDao(), api) { ahora }

        try {
            assertEquals(LoginResult.Exito("TECNICO", false), auth.login("123456785", "clave-tecnico"))
            auth.cerrarSesion()
            assertEquals(LoginResult.Exito("SUPERVISOR", false), auth.login("111111111", "clave-supervisor"))
            auth.cerrarSesion()
            assertNull(auth.restaurarSesion())

            sinConexion = true
            assertEquals(LoginResult.CredencialesInvalidas, auth.login("123456785", "incorrecta"))
            assertEquals(LoginResult.Exito("TECNICO", true), auth.login("123456785", "clave-tecnico"))
            assertEquals("1", SessionManager.idEmpleadoActual)
            auth.cerrarSesion()
            assertNull(SessionManager.tokenActual)
            assertEquals(LoginResult.Exito("SUPERVISOR", true), auth.login("111111111", "clave-supervisor"))
            auth.cerrarSesion()

            ahora = 60_000L
            assertEquals(LoginResult.SesionLocalExpirada, auth.login("111111111", "clave-supervisor"))
        } finally {
            SessionManager.cerrarSesion()
            database.close()
        }
    }
}
