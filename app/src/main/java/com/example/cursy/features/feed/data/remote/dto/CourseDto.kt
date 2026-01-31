package com.example.cursy.features.feed.data.remote.dto

import com.google.gson.annotations.SerializedName

data class FeedResponse(
    val courses: List<CourseDto>?,
    val count: Int = 0
)

data class CourseDto(
    val id: String,
    @SerializedName("author_id")
    val authorId: String? = null,
    val title: String,
    val description: String,
    @SerializedName("cover_image")
    val coverImage: String?,
    val status: String? = null,
    @SerializedName("created_at")
    val createdAt: String? = null,
    @SerializedName("updated_at")
    val updatedAt: String? = null,
    @SerializedName("author_name")
    val authorName: String? = null,
    @SerializedName("author_image")
    val authorImage: String? = null,
    val blocks: List<ContentBlockDto>? = null
)

data class ContentBlockDto(
    val type: String,
    val content: String,
    val order: Int? = null
)