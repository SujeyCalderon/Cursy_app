package com.example.cursy.features.course.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.course.domain.usecases.DeleteCourseUseCase
import com.example.cursy.features.course.domain.usecases.GetCourseDetailUseCase
import com.example.cursy.features.course.domain.usecases.SaveCourseUseCase
import com.example.cursy.features.course.presentation.CourseDetailUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CourseDetailViewModel(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase,
    private val saveCourseUseCase: SaveCourseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getCourseDetailUseCase(courseId)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { (course, isOwner, isSaved) ->
                        currentState.copy(
                            isLoading = false,
                            course = course,
                            isOwner = isOwner,
                            isSaved = isSaved,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar curso"
                        )
                    }
                )
            }
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false) }

            val result = deleteCourseUseCase(courseId)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = {
                        currentState.copy(deleteSuccess = true)
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            error = exception.message ?: "Error al eliminar curso"
                        )
                    }
                )
            }
        }
    }

    fun toggleSave() {
        val course = uiState.value.course ?: return
        
        viewModelScope.launch {
            val newSavedState = !uiState.value.isSaved
            _uiState.update { it.copy(isSaved = newSavedState) }

            val result = saveCourseUseCase(course.id, newSavedState)
            
            result.onFailure {
                _uiState.update { it.copy(isSaved = !newSavedState) }
            }
        }
    }
}