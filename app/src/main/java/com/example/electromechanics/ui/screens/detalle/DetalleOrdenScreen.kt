package com.example.electromechanics.ui.screens.detalle

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import com.example.electromechanics.ui.components.CampoTextoValidado
import com.example.electromechanics.ui.components.SeccionEvidencias
import com.example.electromechanics.ui.components.TarjetaLinterna
import com.example.electromechanics.util.vibrar
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    viewModel: DetalleOrdenViewModel,
    evidenciaViewModel: EvidenciaViewModel,
    onVolverClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var vibrando by remember { mutableStateOf(false) }

    // Al guardar una actividad: vibra, muestra un Toast y la tarjeta tiembla (igual que en clases)
    LaunchedEffect(uiState.actividadGuardada) {
        if (uiState.actividadGuardada) {
            vibrar(context, 500L)
            Toast.makeText(context, "Actividad registrada correctamente", Toast.LENGTH_SHORT).show()
            vibrando = true
            delay(500L)
            vibrando = false
            viewModel.onActividadGuardadaConsumida()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = uiState.orden?.let { "OT #${it.idOrden}" } ?: "Detalle de Orden") },
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
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            uiState.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.errorMessage ?: "Error al cargar la orden",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            uiState.orden != null -> {
                val orden = uiState.orden!!

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. Información General de la OT
                    item {
                        SeccionInformacionOrden(orden = orden)
                    }

                    // 2. Control de Cambio de Estado
                    item {
                        SeccionCambioEstado(
                            estadoActual = orden.estado,
                            onEstadoSeleccionado = { nuevoEstado ->
                                viewModel.cambiarEstadoOrden(nuevoEstado)
                            }
                        )
                    }

                    // 3. Linterna (recurso nativo) para trabajar en lugares oscuros
                    item {
                        TarjetaLinterna(context)
                    }

                    // 3b. Evidencia fotográfica (cámara)
                    item {
                        SeccionEvidencias(
                            viewModel = evidenciaViewModel,
                            idOrden = orden.idOrden
                        )
                    }

                    // 4. Formulario para Nueva Actividad
                    item {
                        SeccionFormularioActividad(
                            uiState = uiState,
                            vibrando = vibrando,
                            onTareaChange = viewModel::onTareaChanged,
                            onRepuestosChange = viewModel::onRepuestosChanged,
                            onComentariosChange = viewModel::onComentariosChanged,
                            onRegistrarClick = viewModel::registrarActividad
                        )
                    }

                    // 5. Encabezado Trazabilidad
                    item {
                        Text(
                            text = "Historial de Actividades (Trazabilidad)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (uiState.actividades.isEmpty()) {
                        item {
                            Text(
                                text = "No hay actividades registradas aún.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.actividades) { actividad ->
                            ItemActividad(actividad = actividad)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionInformacionOrden(orden: OrdenTrabajoEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = orden.clienteNombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Dirección: ${orden.direccion}", style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = "Programada: ${orden.fechaProgramada}", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Problema Reportado:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(text = orden.descripcionProblema, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SeccionCambioEstado(
    estadoActual: String,
    onEstadoSeleccionado: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estado Actual: $estadoActual",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("PENDIENTE", "EN_PROCESO", "COMPLETADA").forEach { estado ->
                    if (estado == estadoActual) {
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = estado.replace("_", " "), style = MaterialTheme.typography.labelSmall)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { onEstadoSeleccionado(estado) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = estado.replace("_", " "), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SeccionFormularioActividad(
    uiState: DetalleOrdenUiState,
    vibrando: Boolean,
    onTareaChange: (String) -> Unit,
    onRepuestosChange: (String) -> Unit,
    onComentariosChange: (String) -> Unit,
    onRegistrarClick: () -> Unit
) {
    // animación de temblor (misma que MyAppRecursosNativos)
    val transicion = rememberInfiniteTransition(label = "temblor")
    val desplazamiento by transicion.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(40), RepeatMode.Reverse),
        label = "desplazamiento"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(if (vibrando) desplazamiento.roundToInt() else 0, 0) },
        colors = CardDefaults.cardColors(
            containerColor = if (vibrando) MaterialTheme.colorScheme.tertiaryContainer
            else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Registrar Nueva Actividad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            CampoTextoValidado(
                value = uiState.nuevaTarea,
                onValueChange = onTareaChange,
                label = "Tarea Ejecutada (*)",
                errorText = uiState.tareaError,
                leadingIcon = Icons.Default.Build
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoTextoValidado(
                value = uiState.nuevosRepuestos,
                onValueChange = onRepuestosChange,
                label = "Repuestos Utilizados",
                errorText = uiState.repuestosError,
                leadingIcon = Icons.Default.Settings
            )

            Spacer(modifier = Modifier.height(8.dp))

            CampoTextoValidado(
                value = uiState.nuevosComentarios,
                onValueChange = onComentariosChange,
                label = "Comentarios Técnicos",
                errorText = uiState.comentariosError,
                leadingIcon = Icons.Default.Edit,
                singleLine = false
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRegistrarClick,
                enabled = !uiState.isGuardandoActividad,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isGuardandoActividad) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Guardar Actividad")
                }
            }
        }
    }
}

@Composable
private fun ItemActividad(actividad: ActividadEntity) {
    val fechaFormateada = try {
        val millis = actividad.horaRegistro.toLongOrNull() ?: System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        sdf.format(Date(millis))
    } catch (e: Exception) {
        actividad.horaRegistro
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = actividad.detalleTareaEjecutada,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = fechaFormateada,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (actividad.repuestosUtilizados.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Repuestos: ${actividad.repuestosUtilizados}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (actividad.comentariosTecnicos.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Notas: ${actividad.comentariosTecnicos}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}