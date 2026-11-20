package com.example.electromechanics.ui.screens.supervisor

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.electromechanics.util.NotificacionOrden

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupervisorHomeScreen(viewModel: SupervisorViewModel, onCerrarSesion: () -> Unit) {
    val estado by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var menuAbierto by rememberSaveable { mutableStateOf(false) }
    var mensajeNotificacion by rememberSaveable { mutableStateOf<String?>(null) }

    fun mostrarNotificacion() {
        val orden = estado.ordenCreada ?: return
        mensajeNotificacion = if (NotificacionOrden.mostrar(context, orden)) {
            "Notificación enviada a este dispositivo."
        } else {
            "Las notificaciones están desactivadas en este dispositivo."
        }
    }

    val permisoNotificaciones = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { autorizado ->
        if (autorizado) mostrarNotificacion()
        else mensajeNotificacion = "Las notificaciones están desactivadas en este dispositivo."
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Panel supervisor") },
                actions = { TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") } }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Asignar orden de trabajo", style = MaterialTheme.typography.headlineSmall)
            Box {
                OutlinedButton(
                    onClick = { menuAbierto = true },
                    enabled = estado.tecnicos.isNotEmpty() && !estado.guardando && !estado.cargandoTecnicos,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(estado.tecnico?.let { "Técnico: ${it.username}" } ?: "Seleccionar técnico")
                }
                DropdownMenu(expanded = menuAbierto, onDismissRequest = { menuAbierto = false }) {
                    estado.tecnicos.forEach { tecnico ->
                        DropdownMenuItem(
                            text = { Text(tecnico.username) },
                            onClick = {
                                viewModel.seleccionarTecnico(tecnico)
                                menuAbierto = false
                            }
                        )
                    }
                }
            }
            estado.errorTecnico?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            if (estado.tecnicos.isEmpty()) {
                Text(if (estado.cargandoTecnicos) "Cargando técnicos…" else "No hay técnicos disponibles.")
            }
            TextButton(
                onClick = viewModel::cargarTecnicos,
                enabled = !estado.guardando && !estado.cargandoTecnicos
            ) { Text("Actualizar técnicos") }

            CampoOrden.entries.forEach { campo ->
                val error = estado.errores[campo]
                OutlinedTextField(
                    value = estado.campos[campo].orEmpty(),
                    onValueChange = { viewModel.editar(campo, it) },
                    label = { Text(campo.etiqueta) },
                    singleLine = campo != CampoOrden.DESCRIPCION,
                    enabled = !estado.guardando,
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
            estado.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            Button(
                onClick = {
                    mensajeNotificacion = null
                    viewModel.asignarOrden()
                },
                enabled = !estado.guardando && !estado.cargandoTecnicos,
                modifier = Modifier.fillMaxWidth()
            ) { Text(if (estado.guardando) "Guardando…" else "Asignar orden") }

            estado.ordenCreada?.let { orden ->
                Text("Orden asignada para ${orden.fecha}.", style = MaterialTheme.typography.titleMedium)
                OutlinedButton(onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
                        PackageManager.PERMISSION_GRANTED
                    ) {
                        permisoNotificaciones.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        mostrarNotificacion()
                    }
                }) {
                    Text("Mostrar notificación")
                }
                mensajeNotificacion?.let { Text(it) }
            }
        }
    }
}
