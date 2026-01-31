package com.example.cursy.features.profile.domain.repository

import com.example.cursy.features.profile.domain.entities.CourseItem
import com.example.cursy.features.profile.domain.entities.Profile

interface ProfileRepository {
    suspend fun getMyProfile(): Result<Profile>
    suspend fun getMyCourses(): Result<Pair<List<CourseItem>, List<CourseItem>>>
    suspend fun getSavedCourses(): Result<List<CourseItem>>
    suspend fun updateProfile(name: String?, profileImage: String?, bio: String?, university: String?): Result<Unit>
}