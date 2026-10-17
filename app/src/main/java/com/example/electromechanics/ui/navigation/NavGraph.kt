package com.example.electromechanics.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.ui.screens.login.LoginScreen
import com.example.electromechanics.ui.screens.login.LoginViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            val empleadoDao = database.empleadoDao()
            val viewModel = remember { LoginViewModel(empleadoDao) }

            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.HojaRuta.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.HojaRuta.route) {
            // Pantalla temporal de destino post-login
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "¡Bienvenido a la Hoja de Ruta! (Paso 3)",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    }
}