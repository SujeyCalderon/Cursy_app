package com.example.cursy.features.course.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CourseDetailResponse(
    val course: CourseDetailDto,
    @SerializedName("is_owner")
    val isOwner: Boolean = false,
    @SerializedName("is_saved")
    val isSaved: Boolean = false
)

data class CourseDetailDto(
    val id: String,
    @SerializedName("author_id")
    val authorId: String? = null,
    val title: String,
    val description: String,
    @SerializedName("cover_image")
    val coverImage: String?,
    val status: String? = null,
    val blocks: List<ContentBlockDto>? = null,
    @SerializedName("author_name")
    val authorName: String? = null,
    @SerializedName("author_image")
    val authorImage: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null
)

data class ContentBlockDto(
    val type: String,
    val content: String,
    val order: Int? = null
)