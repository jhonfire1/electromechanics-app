package com.example.electromechanics.ui.components

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.electromechanics.data.local.entity.EvidenciaEntity
import com.example.electromechanics.ui.screens.detalle.EvidenciaViewModel
import com.example.electromechanics.util.EvidenciaArchivos
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun SeccionEvidencias(
    viewModel: EvidenciaViewModel,
    idOrden: String
) {
    val context = LocalContext.current
    val evidencias by viewModel.evidencias.collectAsState()

    // rememberSaveable: si la pantalla se recrea mientras la cámara está abierta, no se pierde la ruta
    var rutaPendiente by rememberSaveable { mutableStateOf<String?>(null) }
    var tipoPendiente by rememberSaveable { mutableStateOf("INICIAL") }
    var mensajeError by remember { mutableStateOf<String?>(null) }

    // 1) Resultado de la cámara: si sacó la foto se guarda en Room, si canceló se borra el archivo vacío
    val camaraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { exito ->
        val ruta = rutaPendiente
        if (exito && ruta != null) {
            viewModel.guardarEvidencia(ruta, tipoPendiente)
        } else if (ruta != null) {
            File(ruta).delete()
        }
        rutaPendiente = null
    }

    // 2) Prepara el archivo, obtiene su Uri segura y abre la cámara
    fun abrirCamara() {
        val archivo = EvidenciaArchivos.crearArchivo(context, idOrden)
        rutaPendiente = archivo.absolutePath
        camaraLauncher.launch(EvidenciaArchivos.obtenerUri(context, archivo))
    }

    // 3) Permiso de cámara en tiempo de ejecución
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concedido ->
        if (concedido) {
            abrirCamara()
        } else {
            mensajeError = "Sin permiso de cámara no se puede tomar la foto"
        }
    }

    fun pedirFoto(tipo: String) {
        tipoPendiente = tipo
        mensajeError = null
        val tienePermiso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        if (tienePermiso) abrirCamara() else permisoLauncher.launch(Manifest.permission.CAMERA)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Evidencia fotográfica",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { pedirFoto("INICIAL") },
                    modifier = Modifier.weight(1f)
                ) { Text("Foto inicial") }

                OutlinedButton(
                    onClick = { pedirFoto("FINAL") },
                    modifier = Modifier.weight(1f)
                ) { Text("Foto final") }
            }

            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajeError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (evidencias.isEmpty()) {
                Text(
                    text = "Aún no hay fotos de esta orden.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    evidencias.forEach { evidencia ->
                        ItemEvidencia(evidencia)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemEvidencia(evidencia: EvidenciaEntity) {
    // La foto se decodifica fuera del hilo principal para no congelar la pantalla
    val imagen by produceState<ImageBitmap?>(null, evidencia.fotoPath) {
        value = withContext(Dispatchers.IO) { EvidenciaArchivos.cargarMiniatura(evidencia.fotoPath) }
    }

    Column {
        Text(
            text = "${evidencia.tipo} - ${evidencia.fechaHora}",
            style = MaterialTheme.typography.labelMedium
        )
        Spacer(modifier = Modifier.height(4.dp))

        val forma = RoundedCornerShape(12.dp)
        val bitmap = imagen
        if (bitmap != null) {
            // Ajuste de presentación: ancho completo, alto fijo, recorte y borde redondeado
            Image(
                bitmap = bitmap,
                contentDescription = "Foto ${evidencia.tipo.lowercase()}",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(forma)
                    .border(2.dp, MaterialTheme.colorScheme.primary, forma)
            )
        } else {
            Text(
                text = "No se pudo cargar la foto",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
