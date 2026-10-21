package com.example.electromechanics.ui.components

import android.content.Context
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TarjetaLinterna(context: Context) {
    var encendida by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Linterna", style = MaterialTheme.typography.titleMedium)
            Switch(
                checked = encendida,
                onCheckedChange = { nuevoEstado ->
                    if (cambiarLinterna(context, nuevoEstado)) {
                        encendida = nuevoEstado
                    }
                }
            )
        }
    }
}

fun cambiarLinterna(context: Context, encender: Boolean): Boolean {
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val idCamara = cameraManager.cameraIdList.firstOrNull { id ->
        cameraManager.getCameraCharacteristics(id)
            .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
    } ?: return false

    cameraManager.setTorchMode(idCamara, encender)
    Log.d("Linterna", "Luz en cámara $idCamara -> ${if (encender) "ENCENDIDA" else "APAGADA"}")
    return true
}
