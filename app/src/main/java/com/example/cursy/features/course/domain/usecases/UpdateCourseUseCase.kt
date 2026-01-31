package com.example.cursy.features.course.domain.usecases

import com.example.cursy.features.course.domain.repository.BlockInput
import com.example.cursy.features.course.domain.repository.CourseRepository
import com.example.cursy.features.course.domain.repository.UpdateCourseInput

class UpdateCourseUseCase(private val repository: CourseRepository) {

    suspend operator fun invoke(
        courseId: String,
        title: String?,
        description: String?,
        coverImage: String?,
        blocks: List<BlockInput>?,
        publish: Boolean = false
    ): Result<Unit> {
        val updateResult = repository.updateCourse(
            courseId,
            UpdateCourseInput(
                title = title,
                description = description,
                coverImage = coverImage,
                blocks = blocks
            )
        )

        return updateResult.fold(
            onSuccess = {
                if (publish) {
                    repository.publishCourse(courseId)
                } else {
                    Result.success(Unit)
                }
            },
            onFailure = { Result.failure(it) }
        )
    }
}
