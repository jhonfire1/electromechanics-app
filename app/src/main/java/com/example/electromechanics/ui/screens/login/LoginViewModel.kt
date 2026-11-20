package com.example.electromechanics.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.electromechanics.data.repository.AuthRepository
import com.example.electromechanics.data.repository.CuentaLocalRegistrada
import com.example.electromechanics.data.repository.LoginResult
import com.example.electromechanics.domain.util.LoginValidator
import com.example.electromechanics.domain.util.ValidationResult
import com.google.gson.JsonParseException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.io.IOException

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

    fun limpiarFormulario() {
        _uiState.update { LoginUiState() }
    }

    fun registrarCliente() {
        val estado = _uiState.value
        if (estado.isLoading) return
        val rutError = (LoginValidator.validarRut(estado.rutInput) as? ValidationResult.Error)?.message
        val passwordError = if (estado.passwordInput.length < 8) {
            "Usa al menos 8 caracteres."
        } else {
            (LoginValidator.validarPassword(estado.passwordInput) as? ValidationResult.Error)?.message
        }
        if (rutError != null || passwordError != null) {
            _uiState.update { it.copy(rutError = rutError, passwordError = passwordError) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                val rut = LoginValidator.normalizarRut(estado.rutInput)
                val resultado = authRepository.registrarCliente(rut, estado.passwordInput)
                if (resultado is LoginResult.Exito) {
                    _uiState.update { it.copy(rolAutenticado = resultado.rol, passwordInput = "") }
                } else {
                    mostrarErrorRegistro("La cuenta se creó. Intenta iniciar sesión.")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (_: CuentaLocalRegistrada) {
                mostrarErrorRegistro("Este RUT ya tiene una cuenta. Intenta iniciar sesión.")
            } catch (e: HttpException) {
                mostrarErrorRegistro(when (e.code()) {
                    400 -> "Revisa el RUT y la contraseña."
                    409 -> "Este RUT ya tiene una cuenta. Intenta iniciar sesión."
                    else -> "El servidor no pudo registrar la cuenta."
                })
            } catch (e: IOException) {
                mostrarErrorRegistro("Revisa la conexión e intenta nuevamente.")
            } catch (e: JsonParseException) {
                mostrarErrorRegistro("El servidor no respondió correctamente. Intenta iniciar sesión.")
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun mostrarErrorRegistro(mensaje: String) {
        _uiState.update { it.copy(errorMessage = mensaje) }
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
