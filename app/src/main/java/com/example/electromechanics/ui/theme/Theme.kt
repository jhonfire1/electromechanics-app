package com.example.electromechanics.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AzulClaro,
    onPrimary = AzulSobreClaro,
    secondary = GrisAzulClaro,
    secondaryContainer = GrisAzulContenedorOscuro,
    tertiary = NaranjaClaro,
    tertiaryContainer = NaranjaContenedorOscuro
)

private val LightColorScheme = lightColorScheme(
    primary = AzulAcero,
    secondary = GrisAzul,
    secondaryContainer = GrisAzulContenedor,
    tertiary = NaranjaSeguridad,
    tertiaryContainer = NaranjaContenedor

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun ElectromechanicsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Color dinámico desactivado para que se vea la paleta propia de la app
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}