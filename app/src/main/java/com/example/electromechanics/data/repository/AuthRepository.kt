package com.example.electromechanics.data.repository

import com.example.electromechanics.data.local.dao.SesionDao
import com.example.electromechanics.data.local.entity.CuentaLocalEntity
import com.example.electromechanics.data.local.entity.SesionEntity
import com.example.electromechanics.data.remote.AuthApi
import com.example.electromechanics.data.remote.dto.LoginRequestDto
import com.example.electromechanics.data.remote.dto.LoginResponseDto
import com.example.electromechanics.data.session.JwtExpiracion
import com.example.electromechanics.data.session.SessionManager
import com.example.electromechanics.domain.util.PasswordHasher
import com.google.gson.JsonParseException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

object Roles {
    const val TECNICO = "TECNICO"
    const val SUPERVISOR = "SUPERVISOR"
    val PERMITIDOS = setOf(TECNICO, SUPERVISOR)
}

sealed interface LoginResult {
    /** [sinConexion] = true cuando el acceso se validó con el verificador local. */
    data class Exito(val rol: String, val sinConexion: Boolean) : LoginResult
    data object CredencialesInvalidas : LoginResult
    /** Sin internet y sin sesión local utilizable para ese RUT. */
    data object SinSesionLocal : LoginResult
    /** Sin internet y la sesión local ya superó las 8 horas. */
    data object SesionLocalExpirada : LoginResult
    data class ErrorServidor(val codigoHttp: Int?) : LoginResult
}

class AuthRepository(
    private val sesionDao: SesionDao,
    private val authApi: AuthApi,
    // Reloj inyectable: permite probar la expiración de 8 h sin esperar (P1-07).
    private val ahoraMs: () -> Long = { System.currentTimeMillis() }
) {

    /** Intenta login online; solo si no hay red cae al login local. */
    suspend fun login(rutNormalizado: String, password: String): LoginResult {
        return try {
            val respuesta = authApi.login(LoginRequestDto(rutNormalizado, password))
            procesarLoginOnline(rutNormalizado, password, respuesta)
        } catch (e: CancellationException) {
            throw e // nunca tragarse la cancelación de una corrutina
        } catch (e: HttpException) {
            // El servidor respondió: NO se cae al login local.
            if (e.code() == 401) {
                sesionDao.borrarCuenta(rutNormalizado)
                LoginResult.CredencialesInvalidas
            } else {
                LoginResult.ErrorServidor(e.code())
            }
        } catch (e: IOException) {
            // Sin conectividad, DNS o timeout: no hubo respuesta del servidor.
            loginLocal(rutNormalizado, password)
        } catch (e: JsonParseException) {
            LoginResult.ErrorServidor(null)
        }
    }

    /** Recupera la sesión al abrir la app. Devuelve null si no existe o ya expiró. */
    suspend fun restaurarSesion(): SesionEntity? {
        sesionDao.borrarCuentasVencidas(ahoraMs())
        val sesion = sesionDao.obtener() ?: return null
        if (ahoraMs() >= sesion.expiraEnMs) {
            cerrarSesion()
            return null
        }
        SessionManager.iniciarSesion(sesion.idUsuario, sesion.rol, sesion.token)
        return sesion
    }

    suspend fun cerrarSesion() {
        sesionDao.borrar()
        SessionManager.cerrarSesion()
    }

    suspend fun listarTecnicos() = authApi.tecnicos(SessionManager.autorizacion())

    private suspend fun procesarLoginOnline(
        rutNormalizado: String,
        password: String,
        respuesta: LoginResponseDto
    ): LoginResult {
        val token = respuesta.token
        val rol = respuesta.role
        if (token.isNullOrBlank() || respuesta.id <= 0 || rol == null || rol !in Roles.PERMITIDOS) {
            return LoginResult.ErrorServidor(null)
        }
        val expiraEnMs = JwtExpiracion.leerExpiracionMs(token) ?: return LoginResult.ErrorServidor(null)
        if (expiraEnMs <= ahoraMs()) return LoginResult.ErrorServidor(null)

        // PBKDF2 es costoso a propósito: se ejecuta fuera del hilo principal.
        val verificador = withContext(Dispatchers.Default) { PasswordHasher.crearVerificador(password) }

        sesionDao.guardarLogin(
            CuentaLocalEntity(
                idUsuario = respuesta.id.toString(),
                username = rutNormalizado,
                rol = rol,
                token = token,
                expiraEnMs = expiraEnMs,
                verificadorLocal = verificador
            )
        )
        SessionManager.iniciarSesion(respuesta.id.toString(), rol, token)
        return LoginResult.Exito(rol, sinConexion = false)
    }

    private suspend fun loginLocal(rutNormalizado: String, password: String): LoginResult {
        val cuenta = sesionDao.obtenerCuenta(rutNormalizado) ?: return LoginResult.SinSesionLocal
        if (ahoraMs() >= cuenta.expiraEnMs) {
            sesionDao.borrarCuenta(rutNormalizado)
            return LoginResult.SesionLocalExpirada
        }
        val valida = withContext(Dispatchers.Default) {
            PasswordHasher.verificar(password, cuenta.verificadorLocal)
        }
        if (!valida) return LoginResult.CredencialesInvalidas

        sesionDao.guardar(cuenta.comoSesion())
        SessionManager.iniciarSesion(cuenta.idUsuario, cuenta.rol, cuenta.token)
        return LoginResult.Exito(cuenta.rol, sinConexion = true)
    }
}
