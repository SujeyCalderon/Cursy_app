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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CreateEditCourseViewModel @Inject constructor(
    private val createCourseUseCase: CreateCourseUseCase,
    private val updateCourseUseCase: UpdateCourseUseCase,
    private val getCourseDetailUseCase: GetCourseDetailUseCase,
    private val uploadImageUseCase: UploadImageUseCase,
    private val insertNotificationUseCase: InsertNotificationUseCase // Added
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _initialTitle = MutableStateFlow("")
    val initialTitle = _initialTitle.asStateFlow()

    private val _initialDescription = MutableStateFlow("")
    val initialDescription = _initialDescription.asStateFlow()

    private val _initialCoverImage = MutableStateFlow("")
    val initialCoverImage = _initialCoverImage.asStateFlow()

    private val _initialBlocks = MutableStateFlow<List<EditableBlock>>(emptyList())
    val initialBlocks = _initialBlocks.asStateFlow()

    private val _courseLoaded = MutableStateFlow(false)
    val courseLoaded = _courseLoaded.asStateFlow()

    private val _navigateBack = MutableStateFlow(false)
    val navigateBack = _navigateBack.asStateFlow()

    private val _coursePublished = MutableStateFlow(false)
    val coursePublished = _coursePublished.asStateFlow()

    fun loadCourseForEdit(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = getCourseDetailUseCase(courseId)
            result.fold(
                onSuccess = { (course, _, _) ->
                    _initialTitle.value = course.title
                    _initialDescription.value = course.description
                    _initialCoverImage.value = course.coverImage
                    _initialBlocks.value = course.blocks.map {
                        EditableBlock(
                            type = it.type.name.lowercase(),
                            content = it.content
                        )
                    }
                    _courseLoaded.value = true
                    _isLoading.value = false
                },
                onFailure = {
                    Log.e("CreateEditCourse", "Error al cargar curso: ${it.message}")
                    _isLoading.value = false
                }
            )
        }
    }

    fun createCourse(
        title: String,
        description: String,
        coverImage: String,
        blocks: List<EditableBlock>,
        publish: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blockInputs = blocks.mapIndexed { index, block ->
                    BlockInput(
                        type = block.type.lowercase(),
                        content = block.content,
                        order = index + 1
                    )
                }

                val result = createCourseUseCase(
                    title = title,
                    description = description,
                    coverImage = coverImage.takeIf { it.isNotEmpty() },
                    blocks = blockInputs,
                    publish = publish
                )

                result.fold(
                    onSuccess = { courseId ->
                        Log.d("CreateCourse", "Curso creado: $courseId")
<<<<<<< Updated upstream
                        if (publish) _coursePublished.value = true
                        _navigateBack.value = true
=======
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
>>>>>>> Stashed changes
                    },
                    onFailure = { error ->
                        Log.e("CreateCourse", "Error al crear curso: ${error.message}")
                    }
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCourse(
        courseId: String,
        title: String,
        description: String,
        coverImage: String,
        blocks: List<EditableBlock>,
        publish: Boolean
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val blockInputs = blocks.mapIndexed { index, block ->
                    BlockInput(
                        type = block.type.lowercase(),
                        content = block.content,
                        order = index + 1
                    )
                }

                val result = updateCourseUseCase(
                    courseId = courseId,
                    title = title,
                    description = description,
                    coverImage = coverImage,
                    blocks = blockInputs,
                    publish = publish
                )

                result.onSuccess {
                    _navigateBack.value = true
                }
                result.onFailure {
                    Log.e("EditCourse", "Error al actualizar curso: ${it.message}")
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun uploadImage(file: File): Result<String> {
        return uploadImageUseCase(file)
    }

    fun resetNavigateBack() {
        _navigateBack.value = false
    }
}
