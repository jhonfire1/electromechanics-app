package com.example.electromechanics.ui.screens.jornada

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electromechanics.data.local.entity.RegistroJornadaEntity
import com.example.electromechanics.util.UbicacionHelper
import com.example.electromechanics.util.vibrar
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JornadaScreen(
    viewModel: JornadaViewModel,
    onVolverClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    // Pide la ubicación al GPS y, si llega, la entrega al ViewModel
    fun marcar() {
        viewModel.onObteniendoUbicacion()
        UbicacionHelper.obtenerUbicacionActual(
            context = context,
            onUbicacion = { latitud, longitud -> viewModel.registrarMarcacion(latitud, longitud) },
            onError = { mensaje -> viewModel.onUbicacionError(mensaje) }
        )
    }

    // Gestión del permiso de ubicación en tiempo de ejecución
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { resultado ->
        if (resultado.values.any { it }) {
            marcar()
        } else {
            viewModel.onUbicacionError("Sin permiso de ubicación no se puede marcar")
        }
    }

    // Al confirmar la marcación: vibra y oculta el aviso después de un momento
    LaunchedEffect(uiState.mensajeVisible) {
        if (uiState.mensajeVisible) {
            vibrar(context, 300L)
            delay(2500L)
            viewModel.onMensajeConsumido()
        }
    }

    // Animación: el botón cambia de color suavemente entre ENTRADA y SALIDA
    val colorBoton by animateColorAsState(
        targetValue = if (uiState.proximoTipo == "ENTRADA") MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.tertiary,
        label = "colorBoton"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Marcar jornada") },
                navigationIcon = {
                    IconButton(onClick = onVolverClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Próxima marcación: ${uiState.proximoTipo}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Se guarda tu hora y tu ubicación GPS.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (UbicacionHelper.tienePermiso(context)) {
                                marcar()
                            } else {
                                permisoLauncher.launch(
                                    arrayOf(
                                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        enabled = !uiState.isObteniendoUbicacion,
                        colors = ButtonDefaults.buttonColors(containerColor = colorBoton),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (uiState.isObteniendoUbicacion) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Obteniendo ubicación...")
                        } else {
                            Icon(Icons.Default.LocationOn, contentDescription = null)
                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                            Text("Marcar ${uiState.proximoTipo}")
                        }
                    }

                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // Animación: la confirmación aparece deslizando y se desvanece sola
            AnimatedVisibility(
                visible = uiState.mensajeVisible,
                enter = fadeIn() + slideInVertically(),
                exit = fadeOut() + slideOutVertically()
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 6.dp))
                        Text(text = uiState.mensaje, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Historial de marcaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            if (uiState.registros.isEmpty()) {
                Text(
                    text = "Aún no has marcado entrada o salida.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.registros, key = { it.idRegistro }) { registro ->
                        ItemRegistro(registro)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemRegistro(registro: RegistroJornadaEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = registro.tipoMarcacion,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = registro.fechaHoraMarcacion,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(
                text = String.format(
                    Locale.US, "%.5f, %.5f",
                    registro.latitudMarcacion, registro.longitudMarcacion
                ),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
