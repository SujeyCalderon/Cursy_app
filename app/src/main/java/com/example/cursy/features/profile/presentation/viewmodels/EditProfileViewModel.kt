package com.example.cursy.features.profile.presentation.viewmodels

import android.util.Log
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.*
import com.example.cursy.core.Hardware.Domain.BiometricManager
import com.example.cursy.core.Hardware.Domain.CameraManager
import com.example.cursy.core.di.AuthManager
import com.example.cursy.features.course.domain.usecases.UploadImageUseCase
import com.example.cursy.features.profile.data.workers.UploadProfileWorker
import com.example.cursy.features.profile.domain.entities.Biometric
import com.example.cursy.features.profile.domain.usecases.GetMyProfileUseCase
import com.example.cursy.features.profile.domain.usecases.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val cameraManager: CameraManager,
    private val biometricManager: BiometricManager,
    private val authManager: AuthManager,
    private val workManager: WorkManager
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

    // NUEVO: Estado del worker para mostrar progreso al usuario
    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _huellaEnabled = MutableStateFlow(false)
    val huellaEnabled: StateFlow<Boolean> = _huellaEnabled.asStateFlow()

    private val _huellaMessage = MutableStateFlow("")
    val huellaMessage: StateFlow<String> = _huellaMessage.asStateFlow()

    private var selectedImageFile: File? = null
    private var currentWorkId: UUID? = null

    val hasCamera: Boolean get() = cameraManager.hasCamera()
    val isBiometricAvailable: Boolean get() = biometricManager.isBiometricAvailable()

    fun initWith(name: String, bio: String, university: String, profileImage: String) {
        _name.value = name
        _bio.value = bio
        _university.value = university
        _profileImageUrl.value = profileImage

        val userId = authManager.getCurrentUserId()
        Log.d("EditProfileVM", "initWith - userId: $userId")

        userId ?: return
        viewModelScope.launch {
            biometricManager.getHuella(userId).fold(
                onSuccess = { biometric ->
                    _huellaEnabled.value = biometric?.stateHuella == true
                },
                onFailure = { Log.e("EditProfileVM", "Error obteniendo huella: ${it.message}") }
            )
        }

        // NUEVO: Observar trabajos pendientes al iniciar (por si hay uno en progreso)
        observePendingWork()
    }

    // NUEVO: Observar si hay trabajos de subida pendientes al iniciar la pantalla
    private fun observePendingWork() {
        workManager.getWorkInfosByTagLiveData("profile_upload").observeForever { workInfos ->
            workInfos?.forEach { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        _isUploading.value = true
                        _uploadState.value = UploadState.Uploading
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val newUrl = workInfo.outputData.getString(UploadProfileWorker.KEY_RESULT_URL)
                        newUrl?.let {
                            _profileImageUrl.value = it
                            _uploadState.value = UploadState.Success(it)
                        }
                        _isUploading.value = false
                        // Limpiar el worker completado
                        workManager.pruneWork()
                    }
                    WorkInfo.State.FAILED -> {
                        _isUploading.value = false
                        _uploadState.value = UploadState.Error("Error al subir la imagen")
                    }
                    WorkInfo.State.CANCELLED -> {
                        _isUploading.value = false
                        _uploadState.value = UploadState.Idle
                    }
                    else -> {}
                }
            }
        }
    }

    fun onNameChange(value: String) { _name.value = value }
    fun onBioChange(value: String) { _bio.value = value }
    fun onUniversityChange(value: String) { _university.value = value }

    fun activarHuella(activity: FragmentActivity) {
        // ... (mantén el código existente igual)
    }

    fun desactivarHuella() {
        // ... (mantén el código existente igual)
    }

    fun onHuellaMessageHandled() { _huellaMessage.value = "" }

    fun onImageSelected(file: File) {
        selectedImageFile = file
        _profileImageUrl.value = file.absolutePath
        _uploadState.value = UploadState.Idle // Resetear estado
    }

    fun saveProfile(initialProfileImage: String) {
        val hasNewImage = selectedImageFile != null

        if (hasNewImage) {
            // NUEVO: Programar el worker y observar su progreso
            scheduleUploadWorkerAndObserve()
        } else {
            viewModelScope.launch {
                _isLoading.value = true
                updateProfileUseCase(
                    name = _name.value,
                    profileImage = null,
                    bio = _bio.value,
                    university = _university.value
                ).fold(
                    onSuccess = { _saveSuccess.value = true },
                    onFailure = {
                        Log.e("EditProfileVM", "Error guardando perfil: ${it.message}")
                        _uploadState.value = UploadState.Error(it.message ?: "Error desconocido")
                    }
                )
                _isLoading.value = false
            }
        }
    }

    // NUEVO: Programar worker y observar su estado en tiempo real
    private fun scheduleUploadWorkerAndObserve() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val inputData = workDataOf(
            UploadProfileWorker.KEY_IMAGE_PATH to selectedImageFile?.absolutePath,
            UploadProfileWorker.KEY_NAME to _name.value,
            UploadProfileWorker.KEY_BIO to _bio.value,
            UploadProfileWorker.KEY_UNIVERSITY to _university.value
        )

        val uploadWorkRequest = OneTimeWorkRequestBuilder<UploadProfileWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                java.util.concurrent.TimeUnit.MILLISECONDS
            )
            .addTag("profile_upload")
            .build()

        currentWorkId = uploadWorkRequest.id

        // NUEVO: Observar este trabajo específico
        workManager.getWorkInfoByIdLiveData(uploadWorkRequest.id)
            .observeForever { workInfo ->
                workInfo?.let { handleWorkInfo(it) }
            }

        workManager.enqueueUniqueWork(
            "profile_upload_unique",
            ExistingWorkPolicy.REPLACE,
            uploadWorkRequest
        )

        _isUploading.value = true
        _uploadState.value = UploadState.Uploading
    }

    // NUEVO: Manejar los diferentes estados del worker
    private fun handleWorkInfo(workInfo: WorkInfo) {
        when (workInfo.state) {
            WorkInfo.State.SUCCEEDED -> {
                val newUrl = workInfo.outputData.getString(UploadProfileWorker.KEY_RESULT_URL)
                Log.d("EditProfileVM", "Worker completado. Nueva URL: $newUrl")

                newUrl?.let {
                    _profileImageUrl.value = it
                    _uploadState.value = UploadState.Success(it)
                    _saveSuccess.value = true
                }
                _isUploading.value = false
            }
            WorkInfo.State.FAILED -> {
                Log.e("EditProfileVM", "Worker falló")
                _isUploading.value = false
                _uploadState.value = UploadState.Error("No se pudo subir la imagen. Se reintentará automáticamente.")
            }
            WorkInfo.State.RUNNING -> {
                _isUploading.value = true
            }
            else -> {}
        }
    }

    fun onSaveHandled() {
        _saveSuccess.value = false
        _uploadState.value = UploadState.Idle
    }

    // NUEVO: Estados de subida para la UI
    sealed class UploadState {
        object Idle : UploadState()
        object Uploading : UploadState()
        data class Success(val imageUrl: String) : UploadState()
        data class Error(val message: String) : UploadState()
    }
}