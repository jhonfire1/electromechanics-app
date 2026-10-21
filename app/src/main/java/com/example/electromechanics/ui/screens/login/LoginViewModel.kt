package com.example.electromechanics.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.local.dao.EmpleadoDao
import com.example.electromechanics.data.session.SessionManager
import com.example.electromechanics.domain.util.LoginValidator
import com.example.electromechanics.domain.util.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(
    private val empleadoDao: EmpleadoDao
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
        val rutVal = LoginValidator.validarRut(_uiState.value.rutInput)
        val passVal = LoginValidator.validarPassword(_uiState.value.passwordInput)

        val rutErrorMsg = (rutVal as? ValidationResult.Error)?.message
        val passErrorMsg = (passVal as? ValidationResult.Error)?.message

        if (rutErrorMsg != null || passErrorMsg != null) {
            _uiState.update {
                it.copy(rutError = rutErrorMsg, passwordError = passErrorMsg)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val rutFormateado = _uiState.value.rutInput.replace(".", "").replace("-", "").trim().uppercase()

            // Verificación local contra la base de datos Room
            val empleado = empleadoDao.autenticar(
                rut = rutFormateado,
                passwordHash = _uiState.value.passwordInput
            )

            if (empleado != null) {
                SessionManager.iniciarSesion(empleado.idEmpleado)
                _uiState.update { it.copy(isLoading = false, isSuccess = true) }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Credenciales incorrectas. Verifique su RUT o contraseña."
                    )
                }
            }
        }
    }
}