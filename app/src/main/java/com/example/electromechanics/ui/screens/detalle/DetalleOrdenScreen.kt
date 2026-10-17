package com.example.electromechanics.ui.screens.detalle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.electromechanics.data.local.entity.ActividadEntity
import com.example.electromechanics.data.local.entity.OrdenTrabajoEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleOrdenScreen(
    viewModel: DetalleOrdenViewModel,
    onVolverClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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

                    // 3. Formulario para Nueva Actividad
                    item {
                        SeccionFormularioActividad(
                            uiState = uiState,
                            onTareaChange = viewModel::onTareaChanged,
                            onRepuestosChange = viewModel::onRepuestosChanged,
                            onComentariosChange = viewModel::onComentariosChanged,
                            onRegistrarClick = viewModel::registrarActividad
                        )
                    }

                    // 4. Encabezado Trazabilidad
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
    onTareaChange: (String) -> Unit,
    onRepuestosChange: (String) -> Unit,
    onComentariosChange: (String) -> Unit,
    onRegistrarClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Registrar Nueva Actividad",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.nuevaTarea,
                onValueChange = onTareaChange,
                label = { Text("Tarea Ejecutada (*)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.nuevosRepuestos,
                onValueChange = onRepuestosChange,
                label = { Text("Repuestos Utilizados") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.nuevosComentarios,
                onValueChange = onComentariosChange,
                label = { Text("Comentarios Técnicos") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onRegistrarClick,
                enabled = uiState.nuevaTarea.isNotBlank() && !uiState.isGuardandoActividad,
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