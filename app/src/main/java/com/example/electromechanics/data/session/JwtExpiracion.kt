package com.example.electromechanics.data.session

import android.util.Base64
import org.json.JSONException
import org.json.JSONObject

/**
 * Lee el claim "exp" del payload de un JWT. NO valida la firma: Android no posee el secreto
 * y la validación criptográfica la hace el backend. Este valor solo sirve para decidir en la
 * interfaz si la sesión local sigue vigente (8 horas).
 */
object JwtExpiracion {

    /** @return expiración en milisegundos desde epoch, o null si el token no es legible. */
    fun leerExpiracionMs(token: String): Long? {
        val partes = token.split(".")
        if (partes.size != 3) return null
        return try {
            val bytes = Base64.decode(partes[1], Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            val payload = JSONObject(String(bytes, Charsets.UTF_8))
            if (payload.has("exp")) payload.getLong("exp") * 1000L else null
        } catch (e: IllegalArgumentException) {
            null
        } catch (e: JSONException) {
            null
        }
    }
}
