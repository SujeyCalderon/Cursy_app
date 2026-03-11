package com.example.cursy.features.course.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.course.domain.repository.BlockInput
import com.example.cursy.features.course.domain.usecases.CreateCourseUseCase
import com.example.cursy.features.course.domain.usecases.GetCourseDetailUseCase
import com.example.cursy.features.course.domain.usecases.UpdateCourseUseCase
import com.example.cursy.features.course.domain.usecases.UploadImageUseCase
import com.example.cursy.features.course.presentation.screens.EditableBlock
import com.example.cursy.features.notifications.domain.models.Notification
import com.example.cursy.features.notifications.domain.usecases.InsertNotificationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class CreateEditCourseUiState(
    val title: String = "",
    val description: String = "",
    val coverImage: String = "",
    val blocks: List<EditableBlock> = listOf(EditableBlock()),
    val showBlockTypeDialog: Boolean = false,
    val isPublishing: Boolean = false,
    val isProcessingImage: Boolean = false,
    val activeBlockIndex: Int? = null,
    val isLoading: Boolean = false,
    val courseLoaded: Boolean = false,
    val navigateBack: Boolean = false,
    val coursePublished: Boolean = false
)

@HiltViewModel
class CreateEditCourseViewModel @Inject constructor(
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val insertNotificationUseCase: InsertNotificationUseCase // Added
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreateEditCourseUiState())
    val uiState: StateFlow<CreateEditCourseUiState> = _uiState.asStateFlow()

    // Funciones para actualizar el estado del formulario

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onCoverImageChange(value: String) {
        _uiState.update { it.copy(coverImage = value) }
    }

    fun onBlocksChange(blocks: List<EditableBlock>) {
        _uiState.update { it.copy(blocks = blocks) }
    }

    fun showBlockTypeDialog() {
        _uiState.update { it.copy(showBlockTypeDialog = true) }
    }

    fun hideBlockTypeDialog() {
        _uiState.update { it.copy(showBlockTypeDialog = false) }
    }

    fun setActiveBlockIndex(index: Int?) {
        _uiState.update { it.copy(activeBlockIndex = index) }
    }

    fun setIsProcessingImage(value: Boolean) {
        _uiState.update { it.copy(isProcessingImage = value) }
    }

    fun setIsPublishing(value: Boolean) {
        _uiState.update { it.copy(isPublishing = value) }
    }

    fun updateBlockContent(index: Int, newContent: String) {
        val currentBlocks = _uiState.value.blocks.toMutableList()
        if (index < currentBlocks.size) {
            currentBlocks[index] = currentBlocks[index].copy(content = newContent)
            _uiState.update { it.copy(blocks = currentBlocks) }
        }
    }

    // Carga los datos del curso para edición

    fun loadCourseForEdit(courseId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val result = getCourseDetailUseCase(courseId)
            result.fold(
                onSuccess = { (course, _, _) ->
                    _uiState.update {
                        it.copy(
                            title = course.title,
                            description = course.description,
                            coverImage = course.coverImage,
                            blocks = course.blocks.map { block ->
                                EditableBlock(
                                    type = block.type.name.lowercase(),
                                    content = block.content
                                )
                            }.ifEmpty { listOf(EditableBlock()) },
                            courseLoaded = true,
                            isLoading = false
                        )
                    }
                },
                onFailure = {
                    Log.e("CreateEditCourse", "Error al cargar curso: ${it.message}")
                    _uiState.update { state -> state.copy(isLoading = false) }
                }
            )
        }
    }

    // Crear curso nuevo

    fun createCourse(publish: Boolean) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val blockInputs = state.blocks.mapIndexed { index, block ->
                    BlockInput(
                        type = block.type.lowercase(),
                        content = block.content,
                        order = index + 1
                    )
                }

                val result = createCourseUseCase(
                    title = state.title,
                    description = state.description,
                    coverImage = state.coverImage.takeIf { it.isNotEmpty() },
                    blocks = blockInputs,
                    publish = publish
                )

                result.fold(
                    onSuccess = { courseId ->
                        Log.d("CreateCourse", "Curso creado: $courseId")
                        // Enviar notificación local
                        val action = if (publish) "publicado" else "creado como borrador"
                        val notifTitle = "¡Curso $action!"
                        val notifMsg = "Tu curso '${state.title}' ha sido $action con éxito."
                        viewModelScope.launch {
                            insertNotificationUseCase(
                                Notification(id = 0, title = notifTitle, message = notifMsg, timestamp = System.currentTimeMillis(), isRead = false)
                            )
                        }

                        _uiState.update {
                            it.copy(
                                coursePublished = if (publish) true else it.coursePublished,
                                navigateBack = true
                            )
                        }
                    },
                    onFailure = { error ->
                        Log.e("CreateCourse", "Error al crear curso: ${error.message}")
                    }
                )
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Actualizar curso existente

    fun updateCourse(courseId: String, publish: Boolean) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val blockInputs = state.blocks.mapIndexed { index, block ->
                    BlockInput(
                        type = block.type.lowercase(),
                        content = block.content,
                        order = index + 1
                    )
                }

                val result = updateCourseUseCase(
                    courseId = courseId,
                    title = state.title,
                    description = state.description,
                    coverImage = state.coverImage,
                    blocks = blockInputs,
                    publish = publish
                )

                result.onSuccess {
                    _uiState.update { it.copy(navigateBack = true) }
                }
                result.onFailure {
                    Log.e("EditCourse", "Error al actualizar curso: ${it.message}")
                }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    // Subir imagen

    suspend fun uploadImage(file: File): Result<String> {
        return uploadImageUseCase(file)
    }

    fun resetNavigateBack() {
        _uiState.update { it.copy(navigateBack = false) }
    }
}
