package com.example.cursy.features.login.presentation.viewmodels

import android.util.Base64
import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.core.Hardware.Domain.BiometricManager
import com.example.cursy.core.di.AuthManager
import com.example.cursy.core.network.LoginResponse
import com.example.cursy.core.network.UserResponse
import com.example.cursy.features.login.domain.usecases.LoginUseCase
import com.example.cursy.features.profile.domain.entities.Biometric
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val biometricManager: BiometricManager,
    private val authManager: AuthManager
) : ViewModel() {

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

    private val _passwordVisible = MutableStateFlow(false)
    val passwordVisible: StateFlow<Boolean> = _passwordVisible.asStateFlow()

    private val _huellaDisponible = MutableStateFlow(false)
    val huellaDisponible: StateFlow<Boolean> = _huellaDisponible.asStateFlow()

    private val _huellaUsers = MutableStateFlow<List<Biometric>>(emptyList())

    init {
        checkHuellaDisponible()
    }

    fun togglePasswordVisibility() {
        _passwordVisible.value = !_passwordVisible.value
    }

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
                    Log.d("LoginViewModel", "Login exitoso - token: ${response.token.take(10)}...")
                    authManager.setAuthToken(response.token)
                    authManager.setCurrentUserId(response.user.id)
                    _loginSuccess.value = response
                },
                onFailure = { exception ->
                    Log.e("LoginViewModel", "Error en login: ${exception.message}", exception)
                    val errorMessage = when {
                        exception.message?.contains("401") == true -> "Email o contraseña incorrecta"
                        exception.message?.contains("500") == true -> "Error del servidor. Intenta más tarde"
                        exception.message?.contains("timeout", ignoreCase = true) == true -> "Tiempo de espera agotado"
                        exception.message?.contains("Unable to resolve host") == true -> "Sin conexión a internet"
                        else -> "Error al iniciar sesión: ${exception.message}"
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

    private fun checkHuellaDisponible() {
        viewModelScope.launch {
            biometricManager.getAllHuellas().fold(
                onSuccess = { huellas ->
                    _huellaDisponible.value = huellas.isNotEmpty()
                    _huellaUsers.value = huellas
                },
                onFailure = { }
            )
        }
    }

    fun loginConHuella(activity: FragmentActivity) {
        val huellas = _huellaUsers.value
        if (huellas.isEmpty()) return
        val biometric = huellas.first()

        // El token guardado tiene formato "IV:ENCRYPTED_TOKEN"
        val parts = biometric.tokenLogin.split(":")
        if (parts.size != 2) {
            _error.value = "Error: Datos de huella corruptos"
            return
        }
        val iv = Base64.decode(parts[0], Base64.DEFAULT)
        val encryptedTokenPart = parts[1]

        val cipher = biometricManager.getCipherForDecryption(biometric.keyUser, iv)
        if (cipher == null) {
            _error.value = "Error al preparar el descifrado"
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    viewModelScope.launch {
                        val authenticatedCipher = result.cryptoObject?.cipher
                        val decryptedToken = if (authenticatedCipher != null) {
                            biometricManager.decryptWithCipher(authenticatedCipher, encryptedTokenPart)
                        } else {
                            null
                        }

                        if (decryptedToken == null) {
                            _error.value = "Error al descifrar el token"
                            return@launch
                        }
                        authManager.setAuthToken(decryptedToken)
                        authManager.setCurrentUserId(biometric.userId.toString())
                        _loginSuccess.value = LoginResponse(
                            message = "Login exitoso con huella",
                            token = decryptedToken,
                            user = UserResponse(
                                id = biometric.userId,
                                email = "",
                                name = "",
                                bio = null,
                                profile_image = null,
                                university = null,
                                has_published_course = false,
                                is_verified = true
                            )
                        )
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    _error.value = "Error: $errString"
                }

                override fun onAuthenticationFailed() {
                    _error.value = "Huella no reconocida"
                }
            }
        )

        biometricPrompt.authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Iniciar sesión con huella")
                .setSubtitle("Usa tu huella para entrar a tu cuenta")
                .setNegativeButtonText("Usar contraseña")
                .build(),
            BiometricPrompt.CryptoObject(cipher)
        )
    }
}