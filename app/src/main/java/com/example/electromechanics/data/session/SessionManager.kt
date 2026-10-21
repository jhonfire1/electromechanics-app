package com.example.electromechanics.data.session

// Guarda qué empleado inició sesión (objeto simple, igual que LoginValidator)
object SessionManager {
    var idEmpleadoActual: String? = null
        private set

    fun iniciarSesion(idEmpleado: String) {
        idEmpleadoActual = idEmpleado
    }
}
