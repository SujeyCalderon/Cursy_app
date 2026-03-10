package com.example.cursy.features.profile.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.course.domain.usecases.UploadImageUseCase
import com.example.cursy.features.profile.domain.usecases.UpdateProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class EditProfileUiState(
    val name: String = "",
    val bio: String = "",
    val university: String = "",
    val profileImageUrl: String = "",
    val initialProfileImage: String = "",
    val isLoading: Boolean = false,
    val isUploading: Boolean = false,
    val saveSuccess: Boolean = false
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase,
    private val uploadImageUseCase: UploadImageUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    fun initProfile(name: String, bio: String, university: String, profileImage: String) {
        // Solo inicializar si aún no se ha cargado (evitar sobreescribir al recomposición)
        if (_uiState.value.initialProfileImage.isEmpty() && _uiState.value.name.isEmpty()) {
            _uiState.update {
                it.copy(
                    name = name,
                    bio = bio,
                    university = university,
                    profileImageUrl = profileImage,
                    initialProfileImage = profileImage
                )
            }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value) }
    }

    fun onBioChange(value: String) {
        _uiState.update { it.copy(bio = value) }
    }

    fun onUniversityChange(value: String) {
        _uiState.update { it.copy(university = value) }
    }

    fun uploadImage(file: File) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            try {
                val result = uploadImageUseCase(file)
                result.fold(
                    onSuccess = { url ->
                        _uiState.update { it.copy(profileImageUrl = url) }
                    },
                    onFailure = { /* Manejar error */ }
                )
            } finally {
                _uiState.update { it.copy(isUploading = false) }
            }
        }
    }

    fun saveProfile(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val state = _uiState.value
            val imageToSave = if (state.profileImageUrl != state.initialProfileImage) state.profileImageUrl else null
            val result = updateProfileUseCase(
                name = state.name,
                profileImage = imageToSave,
                bio = state.bio,
                university = state.university
            )
            result.fold(
                onSuccess = {
                    _uiState.update { it.copy(isLoading = false) }
                    onSuccess()
                },
                onFailure = {
                    _uiState.update { it.copy(isLoading = false) }
                }
            )
        }
    }
}
