package com.example.cursy.features.Review.Data.Remote.Dto

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    @SerializedName("id")          val id: String?,
    @SerializedName("course_id")   val courseId: String?,
    @SerializedName("user_id")     val userId: String?,
    @SerializedName("user_name")   val userName: String?,
    @SerializedName("user_image")  val userImage: String?,
    @SerializedName("content")     val content: String?,
    @SerializedName("created_at")  val createdAt: String?
)

data class GetCommentsResponse(
    @SerializedName("comments") val comments: List<ReviewDto>?,
    @SerializedName("count")    val count: Int?
)

data class CreateCommentRequest(
    @SerializedName("content") val content: String
)

data class CreateCommentResponse(
    @SerializedName("message") val message: String?,
    @SerializedName("comment") val comment: ReviewDto?
)