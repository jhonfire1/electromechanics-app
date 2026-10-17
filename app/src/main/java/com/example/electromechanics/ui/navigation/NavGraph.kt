package com.example.electromechanics.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import com.example.electromechanics.ui.screens.hojaruta.HojaRutaScreen
import com.example.electromechanics.ui.screens.hojaruta.HojaRutaViewModel
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
            val repository = remember { OrdenTrabajoRepository(database.ordenTrabajoDao()) }
            val viewModel: HojaRutaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return HojaRutaViewModel(repository) as T
                    }
                }
            )

            HojaRutaScreen(
                viewModel = viewModel,
                onOrdenClick = { idOrden ->
                    // Próximamente: Navegar al detalle de la OT (Paso 4)
                }
            )
        }
    }
}