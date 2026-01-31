package com.example.cursy.features.course.domain.usecases

import com.example.cursy.features.course.domain.entities.CourseDetail
import com.example.cursy.features.course.domain.repository.CourseRepository

class GetCourseDetailUseCase(private val repository: CourseRepository) {
    suspend operator fun invoke(courseId: String): Result<Triple<CourseDetail, Boolean, Boolean>> {
        return repository.getCourseDetail(courseId)
    }
}