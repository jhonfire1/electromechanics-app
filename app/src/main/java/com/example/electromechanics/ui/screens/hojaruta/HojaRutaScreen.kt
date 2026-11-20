package com.example.electromechanics.ui.screens.hojaruta

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.electromechanics.ui.components.TarjetaBateria
import com.example.electromechanics.ui.components.TarjetaOrdenTrabajo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HojaRutaScreen(
    viewModel: HojaRutaViewModel,
    onOrdenClick: (String) -> Unit,
    onJornadaClick: () -> Unit,
    onCerrarSesion: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoja de Ruta") },
                actions = {
                    TextButton(onClick = viewModel::cargarOrdenes, enabled = !uiState.isLoading) {
                        Text("Actualizar")
                    }
                    TextButton(onClick = onCerrarSesion) { Text("Cerrar sesión") }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Batería del dispositivo (recurso nativo): útil para el técnico en terreno
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                TarjetaBateria(context)
            }

            // Acceso a marcar entrada/salida con GPS
            OutlinedButton(
                onClick = onJornadaClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null)
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                Text("Marcar entrada / salida")
            }

            // Filtros rápidos
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                val filas = listOf(
                    listOf(FiltroEstado.TODAS, FiltroEstado.PENDIENTE, FiltroEstado.EN_PROCESO),
                    listOf(FiltroEstado.COMPLETADA)
                )
                filas.forEach { filtros ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        filtros.forEach { filtro ->
                            FilterChip(
                                selected = uiState.filtroSeleccionado == filtro,
                                onClick = { viewModel.seleccionarFiltro(filtro) },
                                label = { Text(filtro.name.replace("_", " ")) }
                            )
                        }
                    }
                }
            }

            uiState.errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 16.dp))
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.ordenesFiltradas.isEmpty() -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No hay órdenes de trabajo asignadas.")
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.ordenesFiltradas) { orden ->
                            TarjetaOrdenTrabajo(
                                orden = orden,
                                onClick = { onOrdenClick(orden.idOrden) }
                            )
                        }
                    }
                }
            }
        }
    }
}
