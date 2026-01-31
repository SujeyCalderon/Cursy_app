package com.example.cursy.features.course.domain.usecases

import com.example.cursy.features.course.domain.repository.CourseRepository

class SaveCourseUseCase(private val repository: CourseRepository) {
    
    suspend operator fun invoke(courseId: String, save: Boolean): Result<Unit> {
        return if (save) {
            repository.saveCourse(courseId)
        } else {
            repository.unsaveCourse(courseId)
        }
    }
}
