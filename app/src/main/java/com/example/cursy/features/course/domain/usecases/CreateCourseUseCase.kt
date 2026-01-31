package com.example.cursy.features.course.domain.usecases

import com.example.cursy.features.course.domain.repository.BlockInput
import com.example.cursy.features.course.domain.repository.CourseRepository
import com.example.cursy.features.course.domain.repository.CreateCourseInput

class CreateCourseUseCase(private val repository: CourseRepository) {
    
    suspend operator fun invoke(
        title: String,
        description: String,
        coverImage: String?,
        blocks: List<BlockInput>?,
        publish: Boolean = true
    ): Result<String> {
        // Crear el curso
        val createResult = repository.createCourse(
            CreateCourseInput(
                title = title,
                description = description,
                coverImage = coverImage,
                blocks = blocks
            )
        )
        
        // Si se creó exitosamente y se debe publicar
        return createResult.fold(
            onSuccess = { courseId ->
                if (publish) {
                    repository.publishCourse(courseId).fold(
                        onSuccess = { Result.success(courseId) },
                        onFailure = { Result.failure(it) }
                    )
                } else {
                    Result.success(courseId)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
