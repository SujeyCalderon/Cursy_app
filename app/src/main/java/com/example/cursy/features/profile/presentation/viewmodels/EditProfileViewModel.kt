package com.example.cursy.features.profile.presentation.viewmodels

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.core.Hardware.Domain.BiometricManager
import com.example.cursy.core.Hardware.Domain.CameraManager
import com.example.cursy.core.di.AuthManager
import com.example.cursy.features.course.domain.usecases.UploadImageUseCase
import com.example.cursy.features.profile.domain.entities.Biometric
import com.example.cursy.features.profile.domain.usecases.GetMyProfileUseCase
import com.example.cursy.features.profile.domain.usecases.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val cameraManager: CameraManager,
    private val biometricManager: BiometricManager,
    private val authManager: AuthManager
) : ViewModel() {

    private val _name = MutableStateFlow("")
    val name: StateFlow<String> = _name.asStateFlow()

    private val _bio = MutableStateFlow("")
    val bio: StateFlow<String> = _bio.asStateFlow()

    private val _university = MutableStateFlow("")
    val university: StateFlow<String> = _university.asStateFlow()

    private val _profileImageUrl = MutableStateFlow("")
    val profileImageUrl: StateFlow<String> = _profileImageUrl.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUploading = MutableStateFlow(false)
    val isUploading: StateFlow<Boolean> = _isUploading.asStateFlow()

    private val _saveSuccess = MutableStateFlow(false)
    val saveSuccess: StateFlow<Boolean> = _saveSuccess.asStateFlow()

    private val _huellaEnabled = MutableStateFlow(false)
    val huellaEnabled: StateFlow<Boolean> = _huellaEnabled.asStateFlow()

    private val _huellaMessage = MutableStateFlow("")
    val huellaMessage: StateFlow<String> = _huellaMessage.asStateFlow()

    val hasCamera: Boolean get() = cameraManager.hasCamera()
    val isBiometricAvailable: Boolean get() = biometricManager.isBiometricAvailable()

    fun initWith(name: String, bio: String, university: String, profileImage: String) {
        _name.value = name
        _bio.value = bio
        _university.value = university
        _profileImageUrl.value = profileImage

        val userId = authManager.getCurrentUserId()
        Log.d("EditProfileVM", "initWith - userId: $userId")
        Log.d("EditProfileVM", "initWith - token: ${authManager.getAuthToken()?.take(10)}")
        Log.d("EditProfileVM", "initWith - isBiometricAvailable: ${biometricManager.isBiometricAvailable()}")

        userId ?: return
        viewModelScope.launch {
            biometricManager.getHuella(userId).fold(
                onSuccess = { biometric ->
                    _huellaEnabled.value = biometric?.stateHuella == true
                    Log.d("EditProfileVM", "huellaEnabled: ${_huellaEnabled.value}")
                },
                onFailure = { Log.e("EditProfileVM", "Error obteniendo huella: ${it.message}") }
            )
        }
    }

    fun onNameChange(value: String) { _name.value = value }
    fun onBioChange(value: String) { _bio.value = value }
    fun onUniversityChange(value: String) { _university.value = value }

    fun activarHuella(activity: FragmentActivity) {
        val userId = authManager.getCurrentUserId()
        val token = authManager.getAuthToken()

        Log.d("EditProfileVM", "activarHuella - userId: $userId")
        Log.d("EditProfileVM", "activarHuella - token: ${token?.take(10)}")

        if (userId == null || token == null) {
            Log.e("EditProfileVM", "activarHuella - sesión inválida")
            _huellaMessage.value = "Sesión no válida"
            return
        }

        val keyAlias = "cursy_key_$userId"
        Log.d("EditProfileVM", "activarHuella - keyAlias: $keyAlias")

        if (!biometricManager.generateKey(keyAlias)) {
            Log.e("EditProfileVM", "activarHuella - error generando key")
            _huellaMessage.value = "Error al generar clave biométrica"
            return
        }

        val cipher = biometricManager.getCipherForEncryption(keyAlias)
        if (cipher == null) {
            Log.e("EditProfileVM", "activarHuella - cipher null")
            _huellaMessage.value = "Error al preparar cifrado"
            return
        }

        Log.d("EditProfileVM", "activarHuella - mostrando prompt biométrico")
        val executor = ContextCompat.getMainExecutor(activity)
        BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    Log.d("EditProfileVM", "autenticación exitosa, cifrando token")
                    viewModelScope.launch {
                        val authenticatedCipher = result.cryptoObject?.cipher
                        if (authenticatedCipher == null) {
                            Log.e("EditProfileVM", "authenticatedCipher es null")
                            _huellaMessage.value = "Error al obtener cipher autenticado"
                            return@launch
                        }

                        val encryptedToken = biometricManager.encryptWithCipher(authenticatedCipher, token)
                        if (encryptedToken == null) {
                            Log.e("EditProfileVM", "encryptedToken es null")
                            _huellaMessage.value = "Error al cifrar el token"
                            return@launch
                        }

                        biometricManager.saveHuella(
                            Biometric(
                                userId = userId,
                                keyUser = keyAlias,
                                tokenLogin = encryptedToken,
                                stateHuella = true
                            )
                        ).fold(
                            onSuccess = {
                                _huellaEnabled.value = true
                                _huellaMessage.value = "¡Huella activada correctamente!"
                                Log.d("EditProfileVM", "Huella activada y guardada")
                            },
                            onFailure = {
                                Log.e("EditProfileVM", "Error guardando huella: ${it.message}")
                                _huellaMessage.value = "Error al guardar la huella"
                            }
                        )
                    }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    Log.e("EditProfileVM", "onAuthenticationError: $errorCode - $errString")
                    _huellaMessage.value = "Error: $errString"
                }

                override fun onAuthenticationFailed() {
                    Log.w("EditProfileVM", "onAuthenticationFailed")
                    _huellaMessage.value = "Huella no reconocida"
                }
            }
        ).authenticate(
            BiometricPrompt.PromptInfo.Builder()
                .setTitle("Activar inicio con huella")
                .setSubtitle("Confirma tu huella para activar el acceso biométrico")
                .setNegativeButtonText("Cancelar")
                .build(),
            BiometricPrompt.CryptoObject(cipher)
        )
    }

    fun desactivarHuella() {
        val userId = authManager.getCurrentUserId() ?: return
        viewModelScope.launch {
            biometricManager.deleteHuella(userId).fold(
                onSuccess = {
                    _huellaEnabled.value = false
                    _huellaMessage.value = "Huella desactivada"
                    Log.d("EditProfileVM", "Huella desactivada")
                },
                onFailure = {
                    Log.e("EditProfileVM", "Error desactivando huella: ${it.message}")
                    _huellaMessage.value = "Error al desactivar"
                }
            )
        }
    }

    fun onHuellaMessageHandled() { _huellaMessage.value = "" }

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _isUploading.value = true
            uploadImageUseCase(file).fold(
                onSuccess = { url -> _profileImageUrl.value = url },
                onFailure = { Log.e("EditProfileVM", "Error subiendo imagen: ${it.message}") }
            )
            _isUploading.value = false
        }
    }

    fun saveProfile(initialProfileImage: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val imageToSave = if (_profileImageUrl.value != initialProfileImage) _profileImageUrl.value else null
            updateProfileUseCase(
                name = _name.value,
                profileImage = imageToSave,
                bio = _bio.value,
                university = _university.value
            ).fold(
                onSuccess = { _saveSuccess.value = true },
                onFailure = { Log.e("EditProfileVM", "Error guardando perfil: ${it.message}") }
            )
            _isLoading.value = false
        }
    }

    fun onSaveHandled() { _saveSuccess.value = false }
}