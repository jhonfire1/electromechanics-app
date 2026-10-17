package com.example.electromechanics.ui.screens.login

data class LoginUiState(
    val rutInput: String = "",
    val passwordInput: String = "",
    val rutError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isSuccess: Boolean = false
)