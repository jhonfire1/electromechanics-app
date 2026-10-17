package com.example.electromechanics.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import com.example.electromechanics.ui.screens.detalle.DetalleOrdenScreen
import com.example.electromechanics.ui.screens.detalle.DetalleOrdenViewModel
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
        // 1. Login
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

        // 2. Hoja de Ruta (Listado de OTs)
        composable(Screen.HojaRuta.route) {
            val repository = remember {
                OrdenTrabajoRepository(database.ordenTrabajoDao(), database.actividadDao())
            }
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
                    navController.navigate(Screen.DetalleServicio.createRoute(idOrden))
                }
            )
        }

        // 3. Detalle de la Orden de Trabajo / Servicio
        composable(
            route = Screen.DetalleServicio.route,
            arguments = listOf(navArgument("idServicio") { type = NavType.StringType })
        ) { backStackEntry ->
            val idServicio = backStackEntry.arguments?.getString("idServicio") ?: ""
            val repository = remember {
                OrdenTrabajoRepository(database.ordenTrabajoDao(), database.actividadDao())
            }

            val viewModel: DetalleOrdenViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DetalleOrdenViewModel(repository, idServicio) as T
                    }
                }
            )

            DetalleOrdenScreen(
                viewModel = viewModel,
                onVolverClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}