package com.example.electromechanics.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.electromechanics.data.remote.OrdenDto

object NotificacionOrden {
    private const val CANAL = "ordenes_asignadas"

    // La pantalla solicita el permiso en Android 13 antes de llamar a esta función.
    fun mostrar(context: Context, orden: OrdenDto): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return false
        val manager = NotificationManagerCompat.from(context)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(
                NotificationChannel(CANAL, "Órdenes asignadas", NotificationManager.IMPORTANCE_DEFAULT)
            )
            if (manager.getNotificationChannel(CANAL)?.importance == NotificationManager.IMPORTANCE_NONE) {
                return false
            }
        }
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Orden asignada")
            .setContentText("La orden quedó registrada para el técnico.")
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()

        return try {
            manager.notify(orden.id, 1, notificacion)
            true
        } catch (_: SecurityException) {
            false
        }
    }
}
