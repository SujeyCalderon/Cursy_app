package com.example.cursy.features.profile.data.repository

import com.example.cursy.core.network.CoursyApi
import com.example.cursy.features.profile.data.remote.mapper.toDomain
import com.example.cursy.features.profile.data.remote.mapper.toCourseItem
import com.example.cursy.features.profile.domain.entities.CourseItem
import com.example.cursy.features.profile.domain.entities.Profile
import com.example.cursy.features.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(private val api: CoursyApi) : ProfileRepository {

    override suspend fun getMyProfile(): Result<Profile> {
        return try {
            val response = api.getMyProfile()
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getMyCourses(): Result<Pair<List<CourseItem>, List<CourseItem>>> {
        return try {
            val response = api.getMyCourses()
            val published = response.published?.map { it.toDomain() } ?: emptyList()
            val drafts = response.drafts?.map { it.toDomain() } ?: emptyList()
            Result.success(Pair(published, drafts))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getSavedCourses(): Result<List<CourseItem>> {
        return try {
            val response = api.getSavedCourses()
            val saved = response.courses?.map { it.toCourseItem() } ?: emptyList()
            Result.success(saved)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(
        name: String?,
        profileImage: String?,
        bio: String?,
        university: String?
    ): Result<Unit> {
        return try {
            api.updateProfile(
                com.example.cursy.core.network.UpdateProfileRequest(
                    name = name,
                    profile_image = profileImage,
                    bio = bio,
                    university = university
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}