package com.example.electromechanics.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// Recurso nativo: GPS. Encapsula el acceso a la ubicación para que la UI no lo conozca.
object UbicacionHelper {

    fun tienePermiso(context: Context): Boolean {
        val fina = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        val aproximada = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
        return fina == PackageManager.PERMISSION_GRANTED || aproximada == PackageManager.PERMISSION_GRANTED
    }

    fun gpsActivado(context: Context): Boolean {
        val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    @SuppressLint("MissingPermission")
    fun obtenerUbicacionActual(
        context: Context,
        onUbicacion: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!tienePermiso(context)) {
            onError("Falta el permiso de ubicación")
            return
        }
        if (!gpsActivado(context)) {
            onError("Activa el GPS del celular para poder marcar")
            return
        }

        val cliente = LocationServices.getFusedLocationProviderClient(context)
        cliente.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, CancellationTokenSource().token)
            .addOnSuccessListener { ubicacion ->
                if (ubicacion != null) {
                    onUbicacion(ubicacion.latitude, ubicacion.longitude)
                } else {
                    onError("No se pudo obtener la ubicación. Intenta de nuevo")
                }
            }
            .addOnFailureListener {
                onError("Error al obtener la ubicación")
            }
    }
}
