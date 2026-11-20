package com.example.electromechanics.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.repository.AuthRepository
import com.example.electromechanics.data.repository.LoginResult
import com.example.electromechanics.domain.util.LoginValidator
import com.example.electromechanics.domain.util.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onRutChanged(newRut: String) {
        _uiState.update { it.copy(rutInput = newRut, rutError = null, errorMessage = null) }
    }

    fun onPasswordChanged(newPassword: String) {
        _uiState.update { it.copy(passwordInput = newPassword, passwordError = null, errorMessage = null) }
    }

    fun login() {
        if (_uiState.value.isLoading) return // evita doble envío mientras el servidor despierta

        val rutVal = LoginValidator.validarRut(_uiState.value.rutInput)
        val passVal = LoginValidator.validarPassword(_uiState.value.passwordInput)

        val rutErrorMsg = (rutVal as? ValidationResult.Error)?.message
        val passErrorMsg = (passVal as? ValidationResult.Error)?.message

        if (rutErrorMsg != null || passErrorMsg != null) {
            _uiState.update { it.copy(rutError = rutErrorMsg, passwordError = passErrorMsg) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val resultado = authRepository.login(
                rutNormalizado = LoginValidator.normalizarRut(_uiState.value.rutInput),
                password = _uiState.value.passwordInput
            )

            _uiState.update { estado ->
                when (resultado) {
                    is LoginResult.Exito -> estado.copy(
                        isLoading = false,
                        rolAutenticado = resultado.rol,
                        sinConexion = resultado.sinConexion
                    )
                    LoginResult.CredencialesInvalidas -> estado.copy(
                        isLoading = false,
                        errorMessage = "Credenciales incorrectas. Verifique su RUT o contraseña."
                    )
                    LoginResult.SinSesionLocal -> estado.copy(
                        isLoading = false,
                        errorMessage = "Sin conexión. Ingresa primero con internet en este dispositivo " +
                            "para habilitar el acceso local de esta cuenta."
                    )
                    LoginResult.SesionLocalExpirada -> estado.copy(
                        isLoading = false,
                        errorMessage = "La sesión local expiró (8 horas). Conéctese a internet para iniciar sesión."
                    )
                    is LoginResult.ErrorServidor -> estado.copy(
                        isLoading = false,
                        errorMessage = "El servidor no respondió correctamente. Intente nuevamente."
                    )
                }
            }
        }
    }
}
