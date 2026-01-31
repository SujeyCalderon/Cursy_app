package com.example.cursy.features.course.domain.usecases

import com.example.cursy.features.course.domain.repository.CourseRepository

class DeleteCourseUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(courseId: String): Result<Unit> {
        return repository.deleteCourse(courseId)
    }
}