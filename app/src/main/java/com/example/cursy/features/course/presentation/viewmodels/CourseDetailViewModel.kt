package com.example.cursy.features.course.presentation.viewmodels

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.course.domain.usecases.DeleteCourseUseCase
import com.example.cursy.features.course.domain.usecases.GetCourseDetailUseCase
import com.example.cursy.features.course.domain.usecases.SaveCourseUseCase
import com.example.cursy.features.notifications.domain.models.Notification
import com.example.cursy.features.notifications.domain.usecases.InsertNotificationUseCase
import com.example.cursy.features.course.presentation.CourseDetailUiState
import com.example.cursy.features.feed.data.local.DownloadService
import com.example.cursy.features.feed.data.local.DownloadStatus
import com.example.cursy.features.feed.data.repositories.DownloadRepository
import com.example.cursy.features.course.domain.entities.ContentBlockType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseDetailViewModel @Inject constructor(
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val deleteCourseUseCase: DeleteCourseUseCase,
    private val saveCourseUseCase: SaveCourseUseCase,
    private val insertNotificationUseCase: InsertNotificationUseCase,
    // david: Inyectando el repositorio de descargas
    private val downloadRepository: DownloadRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(CourseDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun loadCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getCourseDetailUseCase(courseId)

            result.fold(
                onSuccess = { (course, isOwner, isSaved) ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            isLoading = false,
                            course = course,
                            isOwner = isOwner,
                            isSaved = isSaved,
                            error = null
                        )
                    }
                    // david: Observar el estado de la descarga local para este curso
                    observeDownloadStatus(courseId)
                },
                onFailure = { exception ->
                    _uiState.update { it.copy(
                        isLoading = false,
                        error = exception.message ?: "Error al cargar curso"
                    ) }
                }
            )
        }
    }

    // david: Función para observar cambios en el estado de la descarga desde Room
    private fun observeDownloadStatus(courseId: String) {
        viewModelScope.launch {
            downloadRepository.allDownloads.collect { downloads ->
                val myDownload = downloads.find { it.courseId == courseId }
                if (myDownload != null) {
                    _uiState.update { it.copy(
                        downloadStatus = myDownload.status,
                        downloadProgress = myDownload.progress
                    ) }
                }
            }
        }
    }

    // david: Lógica para iniciar el servicio de descarga usando ContextCompat para compatibilidad
    fun startCourseDownload() {
        val course = uiState.value.course ?: return
        val videoBlock = course.blocks.find { it.type == ContentBlockType.VIDEO } ?: return

        viewModelScope.launch {
            downloadRepository.startDownload(
                courseId = course.id,
                title = course.title,
                url = videoBlock.content
            )

            val intent = Intent(context, DownloadService::class.java).apply {
                putExtra("courseId", course.id)
                putExtra("title", course.title)
                putExtra("url", videoBlock.content)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    // david: Eliminar descarga local
    fun deleteDownload(courseId: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(courseId)
            _uiState.update { it.copy(
                downloadStatus = DownloadStatus.PENDING,
                downloadProgress = 0
            ) }
        }
    }

    fun showDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = true) }
    }

    fun hideDeleteDialog() {
        _uiState.update { it.copy(showDeleteDialog = false) }
    }

    fun showMenu() {
        _uiState.update { it.copy(showMenu = true) }
    }

    fun hideMenu() {
        _uiState.update { it.copy(showMenu = false) }
    }

    fun deleteCourse(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(showDeleteDialog = false) }

            val result = deleteCourseUseCase(courseId)

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = {
                        val title = uiState.value.course?.title ?: "Curso"
                        viewModelScope.launch {
                            insertNotificationUseCase(
                                Notification(id = 0, title = "Curso eliminado", message = "El curso '$title' ha sido eliminado correctamente.", timestamp = System.currentTimeMillis(), isRead = false)
                            )
                        }
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

            result.onSuccess {
                if (newSavedState) {
                    val title = course.title
                    viewModelScope.launch {
                        insertNotificationUseCase(
                            Notification(id = 0, title = "Curso guardado", message = "Has guardado el curso '$title' en tu perfil.", timestamp = System.currentTimeMillis(), isRead = false)
                        )
                    }
                }
            }.onFailure {
                _uiState.update { it.copy(isSaved = !newSavedState) }
            }
        }
    }
}
