package com.example.cursy.features.profile.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.profile.domain.usecases.GetMyCoursesUseCase
import com.example.cursy.features.profile.domain.usecases.GetMyProfileUseCase
import com.example.cursy.features.profile.domain.usecases.GetSavedCoursesUseCase
import com.example.cursy.features.profile.presentation.ProfileUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getMyProfileUseCase: GetMyProfileUseCase,
    private val getMyCoursesUseCase: GetMyCoursesUseCase,
    private val getSavedCoursesUseCase: GetSavedCoursesUseCase? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            // Cargar perfil
            val profileResult = getMyProfileUseCase()

            // Cargar mis cursos
            val coursesResult = getMyCoursesUseCase()
            
            // Cargar cursos guardados
            val savedResult = getSavedCoursesUseCase?.invoke()

            _uiState.update { currentState ->
                val profile = profileResult.getOrNull()
                val courses = coursesResult.getOrNull()
                val saved = savedResult?.getOrNull() ?: emptyList()

                currentState.copy(
                    isLoading = false,
                    profile = profile,
                    publishedCourses = courses?.first ?: emptyList(),
                    draftCourses = courses?.second ?: emptyList(),
                    savedCourses = saved,
                    error = profileResult.exceptionOrNull()?.message
                )
            }
        }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }
    
    fun refresh() {
        loadProfile()
    }
}