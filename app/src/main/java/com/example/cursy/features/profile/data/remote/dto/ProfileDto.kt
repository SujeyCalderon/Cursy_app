package com.example.cursy.features.profile.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    val user: UserDto,
    val stats: StatsDto
)

data class UserDto(
    val id: String,
    val name: String,
    val email: String,
    val password: String? = null,
    @SerializedName("profile_image")
    val profileImage: String?,
    val bio: String?,
    @SerializedName("university")
    val university: String?,
    @SerializedName("has_published_course")
    val hasPublishedCourse: Boolean = false,
    @SerializedName("is_verified")
    val isVerified: Boolean = false
)

data class StatsDto(
    @SerializedName("published_courses")
    val publishedCourses: Int,
    @SerializedName("draft_courses")
    val draftCourses: Int,
    @SerializedName("saved_courses")
    val savedCourses: Int
)

data class MyCoursesResponse(
    val published: List<CourseItemDto>?,
    val drafts: List<CourseItemDto>?
)

data class CourseItemDto(
    val id: String,
    @SerializedName("author_id")
    val authorId: String? = null,
    val title: String,
    val description: String,
    @SerializedName("cover_image")
    val coverImage: String?,
    val status: String,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")  
    val updatedAt: String? = null
)