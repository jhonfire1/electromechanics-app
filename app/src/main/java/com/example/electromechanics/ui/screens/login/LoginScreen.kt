package com.example.electromechanics.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.electromechanics.BuildConfig
import com.example.electromechanics.ui.components.CampoTextoValidado

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (rol: String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }
    var registrando by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(uiState.rolAutenticado) {
        uiState.rolAutenticado?.let { rol -> onLoginSuccess(rol) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Electromechanics",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Text(
            text = if (registrando) "Crea tu cuenta de cliente" else "Inicia sesión con tu RUT",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        CampoTextoValidado(
            value = uiState.rutInput,
            onValueChange = { viewModel.onRutChanged(it) },
            label = "RUT (ej: 12345678-5)",
            errorText = uiState.rutError,
            leadingIcon = Icons.Default.AccountBox,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
        )

        Spacer(modifier = Modifier.height(16.dp))

        CampoTextoValidado(
            value = uiState.passwordInput,
            onValueChange = { viewModel.onPasswordChanged(it) },
            label = if (registrando) "Contraseña (mínimo 8 caracteres)" else "Contraseña",
            errorText = uiState.passwordError,
            leadingIcon = Icons.Default.Lock,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }) {
                    Text(
                        text = if (passwordVisible) "Ocultar" else "Mostrar",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        )

        uiState.errorMessage?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { if (registrando) viewModel.registrarCliente() else viewModel.login() },
            enabled = !uiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (registrando) "Registrar cuenta" else "Iniciar sesión")
            }
        }

        TextButton(
            onClick = {
                viewModel.limpiarFormulario()
                passwordVisible = false
                registrando = !registrando
            },
            enabled = !uiState.isLoading
        ) {
            Text(if (registrando) "Volver a iniciar sesión" else "Crear cuenta de cliente")
        }

        if (uiState.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = if (BuildConfig.DEMO_LOCAL) "Preparando tu sesión…" else
                    "Conectando con el servidor. La primera vez puede tardar hasta 1 minuto.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
