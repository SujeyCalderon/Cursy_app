package com.example.cursy.features.Review.Domain.Entities

data class Review(
    val id: String,
    val courseId: String,
    val userId: String,
    val userName: String,
    val userImage: String,
    val content: String,
    val createdAt: String
)