package com.example.electromechanics.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Recurso nativo: cámara. Manejo de los archivos de las fotos de evidencia.
object EvidenciaArchivos {

    // Crea el archivo vacío donde la cámara va a guardar la foto (almacenamiento interno de la app)
    fun crearArchivo(context: Context, idOrden: String): File {
        val carpeta = File(context.filesDir, "evidencias").apply { mkdirs() }
        val marca = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(carpeta, "OT${idOrden}_$marca.jpg")
    }

    // La cámara no puede escribir en nuestra carpeta directamente: se le entrega una Uri segura (FileProvider)
    fun obtenerUri(context: Context, archivo: File): Uri {
        return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", archivo)
    }

    // Carga la foto reducida (para no gastar memoria) y corrige la rotación según los datos EXIF
    fun cargarMiniatura(ruta: String, maxLado: Int = 800): ImageBitmap? {
        val archivo = File(ruta)
        if (!archivo.exists() || archivo.length() == 0L) return null

        val limites = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(ruta, limites)

        var muestra = 1
        while (limites.outWidth / muestra > maxLado || limites.outHeight / muestra > maxLado) {
            muestra *= 2
        }

        val opciones = BitmapFactory.Options().apply { inSampleSize = muestra }
        val bitmap = BitmapFactory.decodeFile(ruta, opciones) ?: return null

        val grados = try {
            val exif = ExifInterface(ruta)
            when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                else -> 0f
            }
        } catch (e: Exception) {
            0f
        }

        val resultado: Bitmap = if (grados != 0f) {
            val matriz = Matrix().apply { postRotate(grados) }
            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matriz, true)
        } else {
            bitmap
        }
        return resultado.asImageBitmap()
    }
}
