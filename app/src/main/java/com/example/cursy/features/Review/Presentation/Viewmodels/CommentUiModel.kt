package com.example.cursy.features.Review.Presentation

data class CommentUiModel(
    val id: String,
    val userId: String,
    val userName: String,
    val userImage: String,
    val content: String,
    val createdAt: String
)