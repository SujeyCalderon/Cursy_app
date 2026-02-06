package com.example.cursy.features.Register.presenstation.viewmodels

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.profile.domain.usecases.RegisterProfileUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterViewModel(
    private val registerProfileUseCase: RegisterProfileUseCase
): ViewModel() {

    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()


    private val _ineUrl = MutableStateFlow("https://miservidor.com/uploads/ine.jpg")
    val ineUrl = _ineUrl.asStateFlow()

    private val _university = MutableStateFlow("")
    val university = _university.asStateFlow()

    private val _error = MutableStateFlow("")
    val error = _error.asStateFlow()

    private val _message = MutableStateFlow("")
    val message = _message.asStateFlow()

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible = _passwordVisible.asStateFlow()

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }


    fun onNameChange(value: String) {
        _name.value = value
    }

    fun onEmailChange(value: String) {
        _email.value = value
    }

    fun onPasswordChange(value: String) {
        _password.value = value
    }

    fun onRegister() {
        // Validations
        if (_name.value.isBlank()) {
            _error.value = "Por favor, ingresa tu nombre"
            return
        }
        if (_email.value.isBlank()) {
            _error.value = "Por favor, ingresa tu correo electrónico"
            return
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(_email.value).matches()) {
            _error.value = "Por favor, ingresa un correo electrónico válido"
            return
        }
        if (_password.value.length < 8) {
            _error.value = "La contraseña debe tener al menos 8 caracteres"
            return
        }

        _error.value = ""
        viewModelScope.launch {
            try {
                registerProfileUseCase(
                    name.value,
                    email.value,
                    password.value,
                    ineUrl.value,
                    university.value
                )
                _message.value = "Registro exitoso"
            } catch (e: Exception) {
                _error.value = "Error con el registro: ${e.message}"
            }
        }
    }

}
