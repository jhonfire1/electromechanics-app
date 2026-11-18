package com.example.electromechanics.ui.navigation

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.electromechanics.data.local.AppDatabase
import com.example.electromechanics.data.remote.RetrofitClient
import com.example.electromechanics.data.repository.AuthRepository
import com.example.electromechanics.data.repository.EvidenciaRepository
import com.example.electromechanics.data.repository.JornadaRepository
import com.example.electromechanics.data.repository.OrdenTrabajoRepository
import com.example.electromechanics.data.repository.Roles
import com.example.electromechanics.data.session.SessionManager
import com.example.electromechanics.ui.screens.detalle.DetalleOrdenScreen
import com.example.electromechanics.ui.screens.detalle.DetalleOrdenViewModel
import com.example.electromechanics.ui.screens.detalle.EvidenciaViewModel
import com.example.electromechanics.ui.screens.hojaruta.HojaRutaScreen
import com.example.electromechanics.ui.screens.hojaruta.HojaRutaViewModel
import com.example.electromechanics.ui.screens.jornada.JornadaScreen
import com.example.electromechanics.ui.screens.jornada.JornadaViewModel
import com.example.electromechanics.ui.screens.login.LoginScreen
import com.example.electromechanics.ui.screens.login.LoginViewModel
import com.example.electromechanics.ui.screens.supervisor.SupervisorHomeScreen
import kotlinx.coroutines.launch

/** Destino inicial según el rol. Un rol desconocido o ausente vuelve al login. */
private fun rutaInicialPorRol(rol: String?): String = when (rol) {
    Roles.TECNICO -> Screen.HojaRuta.route
    Roles.SUPERVISOR -> Screen.SupervisorHome.route
    else -> Screen.Login.route
}

@Composable
fun NavGraph(
    navController: NavHostController,
    database: AppDatabase
) {
    // Una sola instancia compartida por Splash, Login y el cierre de sesión.
    val authRepository = remember { AuthRepository(database.sesionDao(), RetrofitClient.authApi) }
    val scope = rememberCoroutineScope()

    val cerrarSesion: () -> Unit = {
        scope.launch {
            authRepository.cerrarSesion()
            // Limpia todo el back stack: con "Atrás" no se puede volver a una pantalla autenticada.
            navController.navigate(Screen.Login.route) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        // Animaciones de transición entre pantallas (deslizar + desvanecer)
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        // 0. Splash: recupera la sesión guardada y decide el destino según el rol
        composable(Screen.Splash.route) {
            LaunchedEffect(Unit) {
                val rol = authRepository.restaurarSesion()?.rol
                navController.navigate(rutaInicialPorRol(rol)) {
                    popUpTo(Screen.Splash.route) { inclusive = true }
                }
            }
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // 1. Login
        composable(Screen.Login.route) {
            // viewModel(factory) en vez de remember: sobrevive a rotaciones de pantalla.
            val viewModel: LoginViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return LoginViewModel(authRepository) as T
                    }
                }
            )

            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { rol ->
                    navController.navigate(rutaInicialPorRol(rol)) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // 1b. Panel provisional del Supervisor (sus pantallas reales llegan en fases posteriores)
        composable(Screen.SupervisorHome.route) {
            SupervisorHomeScreen(onCerrarSesion = cerrarSesion)
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
                },
                onJornadaClick = {
                    navController.navigate(Screen.Jornada.route)
                },
                onCerrarSesion = cerrarSesion
            )
        }

        // 3. Marcar entrada / salida con GPS
        composable(Screen.Jornada.route) {
            val repository = remember { JornadaRepository(database.registroJornadaDao()) }
            val viewModel: JornadaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return JornadaViewModel(repository, SessionManager.idEmpleadoActual ?: "") as T
                    }
                }
            )

            JornadaScreen(
                viewModel = viewModel,
                onVolverClick = { navController.popBackStack() }
            )
        }

        // 4. Detalle de la Orden de Trabajo / Servicio
        composable(
            route = Screen.DetalleServicio.route,
            arguments = listOf(navArgument("idServicio") { type = NavType.StringType })
        ) { backStackEntry ->
            val idServicio = backStackEntry.arguments?.getString("idServicio") ?: ""
            val repository = remember {
                OrdenTrabajoRepository(database.ordenTrabajoDao(), database.actividadDao())
            }
            val evidenciaRepository = remember { EvidenciaRepository(database.evidenciaDao()) }

            val viewModel: DetalleOrdenViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return DetalleOrdenViewModel(repository, idServicio) as T
                    }
                }
            )

            val evidenciaViewModel: EvidenciaViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return EvidenciaViewModel(evidenciaRepository, idServicio) as T
                    }
                }
            )

            DetalleOrdenScreen(
                viewModel = viewModel,
                evidenciaViewModel = evidenciaViewModel,
                onVolverClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
