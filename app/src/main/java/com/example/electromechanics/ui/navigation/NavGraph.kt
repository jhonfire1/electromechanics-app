package com.example.electromechanics.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun SetupNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(route = Screen.Login.route) {
            PlaceholderScreen(title = "Pantalla de Login (Paso 2)")
        }
        composable(route = Screen.HojaRuta.route) {
            PlaceholderScreen(title = "Hoja de Ruta / Servicios (Paso 3)")
        }
        composable(route = Screen.DetalleServicio.route) {
            PlaceholderScreen(title = "Detalle del Servicio (Paso 4)")
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title)
    }
}