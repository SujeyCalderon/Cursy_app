package com.example.cursy.features.profile.domain.usecases

import com.example.cursy.features.profile.domain.entities.CourseItem
import com.example.cursy.features.profile.domain.repository.ProfileRepository

class GetMyCoursesUseCase(private val repository: ProfileRepository) {
    suspend operator fun invoke(): Result<Pair<List<CourseItem>, List<CourseItem>>> {
        return repository.getMyCourses()
    }
}