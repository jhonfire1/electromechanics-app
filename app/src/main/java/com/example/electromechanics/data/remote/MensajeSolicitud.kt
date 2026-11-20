package com.example.electromechanics.data.remote

import retrofit2.HttpException
import java.io.IOException

fun mensajeSolicitud(error: Exception, mensajeDefault: String): String = when (error) {
    is IOException -> "Revisa la conexión con el servicio e intenta nuevamente."
    is HttpException -> when (error.code()) {
        400 -> "Revisa los datos ingresados o vuelve a seleccionar el técnico."
        401 -> "La sesión venció. Cierra sesión e inicia nuevamente."
        403 -> "Tu rol no tiene permiso para realizar esta acción."
        404 -> "El registro ya no está disponible. Actualiza la lista."
        409 -> "La operación cambió. Actualiza la lista o vuelve a intentar."
        else -> mensajeDefault
    }
    else -> mensajeDefault
}
