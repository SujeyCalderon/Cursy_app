package com.example.cursy.features.profile.domain.usecases

import com.example.cursy.features.profile.domain.entities.CourseItem
import com.example.cursy.features.profile.domain.repository.ProfileRepository

class GetSavedCoursesUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<List<CourseItem>> {
        return repository.getSavedCourses()
    }
}
