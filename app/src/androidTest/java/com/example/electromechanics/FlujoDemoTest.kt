package com.example.electromechanics

import android.Manifest
import android.os.Build
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FlujoDemoTest {
    @get:Rule
    val compose = createAndroidComposeRule<MainActivity>()

    @Test
    fun supervisorAsignaYTecnicoCompletaUnaOrdenConElBackendLocal() {
        assumeTrue(InstrumentationRegistry.getArguments().getString("demoLocal") == "true")
        val instrumentacion = InstrumentationRegistry.getInstrumentation()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentacion.uiAutomation.grantRuntimePermission(
                instrumentacion.targetContext.packageName, Manifest.permission.POST_NOTIFICATIONS
            )
        }
        entrar("11.111.111-1", "DemoSupervisor26")
        esperar("Panel supervisor")
        esperar("Seleccionar técnico")
        compose.waitUntil(30_000) { !compose.onAllNodesWithText("Cargando técnicos…").fetchSemanticsNodes().any() }
        compose.onNodeWithText("Seleccionar técnico").performClick()
        compose.onNodeWithText("123456785").performClick()

        val cliente = "Taller demo " + System.currentTimeMillis().toString().takeLast(6)
        val fecha = "2035-11-25"
        listOf(cliente, "Calle Demo 123", "Revisar equipo", fecha).forEachIndexed { indice, valor ->
            compose.onAllNodes(hasSetTextAction())[indice].performScrollTo().performTextInput(valor)
        }
        compose.onNodeWithText("Asignar orden").performScrollTo().performClick()
        esperar("Orden asignada para $fecha.")
        compose.onNodeWithText("Mostrar notificación").performScrollTo().performClick()
        esperar("Notificación enviada a este dispositivo.")
        compose.onNodeWithText("Cerrar sesión").performClick()

        entrar("12.345.678-5", "DemoTecnico26")
        val clienteEnTarjeta = "Cliente: $cliente"
        esperar(clienteEnTarjeta)
        compose.onNodeWithText(clienteEnTarjeta).performClick()
        esperar("Iniciar orden")
        compose.onNodeWithText("Iniciar orden").performClick()
        esperar("Completar orden")
        compose.onNodeWithText("Completar orden").performClick()
        esperar("Estado Actual: COMPLETADA")
    }

    private fun entrar(rut: String, clave: String) {
        esperar("Iniciar Sesión")
        compose.onAllNodes(hasSetTextAction())[0].performTextInput(rut)
        compose.onAllNodes(hasSetTextAction())[1].performTextInput(clave)
        compose.onNodeWithText("Iniciar Sesión").performClick()
    }

    private fun esperar(texto: String) {
        compose.waitUntil(30_000) { compose.onAllNodesWithText(texto).fetchSemanticsNodes().isNotEmpty() }
    }
}
