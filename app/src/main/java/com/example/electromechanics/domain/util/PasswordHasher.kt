package com.example.electromechanics.domain.util

import java.security.GeneralSecurityException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Verificador local de contraseña para el login sin conexión (PBKDF2 con sal).
 * Formato guardado: "algoritmo:iteraciones:saltHex:hashHex".
 * Es Kotlin/Java puro (sin clases de android.*) para poder probarlo con JUnit en la JVM.
 */
object PasswordHasher {

    private const val ITERACIONES = 120_000
    private const val BYTES_SAL = 16
    private const val BITS_HASH = 256
    private const val SEPARADOR = ":"

    // PBKDF2WithHmacSHA256 existe desde API 26; la app soporta desde API 23.
    private const val ALGORITMO_PREFERIDO = "PBKDF2WithHmacSHA256"
    private const val ALGORITMO_RESPALDO = "PBKDF2WithHmacSHA1"

    fun crearVerificador(password: String): String {
        val sal = ByteArray(BYTES_SAL).also { SecureRandom().nextBytes(it) }
        val algoritmo = algoritmoDisponible()
        val hash = derivar(password, sal, ITERACIONES, algoritmo)
        return listOf(algoritmo, ITERACIONES.toString(), sal.aHex(), hash.aHex())
            .joinToString(SEPARADOR)
    }

    fun verificar(password: String, verificador: String): Boolean {
        val partes = verificador.split(SEPARADOR)
        if (partes.size != 4) return false
        val iteraciones = partes[1].toIntOrNull() ?: return false
        val sal = partes[2].desdeHex() ?: return false
        val esperado = partes[3].desdeHex() ?: return false
        val calculado = try {
            derivar(password, sal, iteraciones, partes[0])
        } catch (e: GeneralSecurityException) {
            return false
        }
        // Comparación en tiempo constante: no filtra en qué byte difieren.
        return MessageDigest.isEqual(esperado, calculado)
    }

    private fun algoritmoDisponible(): String =
        try {
            SecretKeyFactory.getInstance(ALGORITMO_PREFERIDO)
            ALGORITMO_PREFERIDO
        } catch (e: NoSuchAlgorithmException) {
            ALGORITMO_RESPALDO
        }

    private fun derivar(password: String, sal: ByteArray, iteraciones: Int, algoritmo: String): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), sal, iteraciones, BITS_HASH)
        try {
            return SecretKeyFactory.getInstance(algoritmo).generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    private fun ByteArray.aHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.desdeHex(): ByteArray? {
        if (length % 2 != 0) return null
        return try {
            ByteArray(length / 2) { i -> substring(2 * i, 2 * i + 2).toInt(16).toByte() }
        } catch (e: NumberFormatException) {
            null
        }
    }
}
