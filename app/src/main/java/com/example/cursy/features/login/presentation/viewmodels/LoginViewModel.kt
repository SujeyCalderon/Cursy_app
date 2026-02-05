package com.example.cursy.features.login.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.core.network.LoginResponse
import com.example.cursy.features.login.domain.usecases.LoginUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginViewModel(private val loginUseCase: LoginUseCase) : ViewModel() {

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow("")
    val error: StateFlow<String> = _error.asStateFlow()

    private val _loginSuccess = MutableStateFlow<LoginResponse?>(null)
    val loginSuccess: StateFlow<LoginResponse?> = _loginSuccess.asStateFlow()

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun onLogin() {
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _error.value = "Por favor, completa todos los campos"
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            val result = loginUseCase(_email.value, _password.value)

            result.fold(
                onSuccess = { response ->
                    _loginSuccess.value = response
                },
                onFailure = { exception ->
                    val errorMessage = when {
                        exception.message?.contains("401") == true -> "Email o contraseña incorrecta"
                        exception.message?.contains("500") == true -> "Error del servidor. Intenta más tarde"
                        exception.message?.contains("timeout", ignoreCase = true) == true -> "Tiempo de espera agotado"
                        exception.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                        else -> "Error al iniciar sesión"
                    }
                    _error.value = errorMessage
                }
            )

            _isLoading.value = false
        }
    }

    fun clearError() {
        _error.value = ""
    }
}
