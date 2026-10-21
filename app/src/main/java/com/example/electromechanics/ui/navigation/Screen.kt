package com.example.electromechanics.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login_screen")
    object HojaRuta : Screen("hoja_ruta_screen")
    object Jornada : Screen("jornada_screen")
    object DetalleServicio : Screen("detalle_servicio_screen/{idServicio}") {
        fun createRoute(idServicio: String) = "detalle_servicio_screen/$idServicio"
    }
    object FormularioActividad : Screen("formulario_actividad_screen/{idServicio}") {
        fun createRoute(idServicio: String) = "formulario_actividad_screen/$idServicio"
    }
}