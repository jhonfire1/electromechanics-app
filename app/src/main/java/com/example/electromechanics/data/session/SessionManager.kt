package com.example.electromechanics.data.session

/**
 * Sesión en memoria. La fuente persistente es la tabla "sesion" de Room;
 * AuthRepository la vuelve a cargar aquí al abrir la app (restaurarSesion).
 */
object SessionManager {
    var idEmpleadoActual: String? = null
        private set
    var rolActual: String? = null
        private set
    var tokenActual: String? = null
        private set

    fun iniciarSesion(idEmpleado: String, rol: String, token: String) {
        idEmpleadoActual = idEmpleado
        rolActual = rol
        tokenActual = token
    }

    fun cerrarSesion() {
        idEmpleadoActual = null
        rolActual = null
        tokenActual = null
    }

    fun autorizacion(): String = "Bearer ${checkNotNull(tokenActual) { "Se requiere iniciar sesión" }}"
}
