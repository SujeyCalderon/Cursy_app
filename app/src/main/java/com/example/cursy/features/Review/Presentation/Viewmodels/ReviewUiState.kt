package com.example.cursy.features.Review.Presentation

data class ReviewUiState(
    val isLoading: Boolean = false,
    val isSending: Boolean = false,
    val comments: List<CommentUiModel> = emptyList(),
    val commentText: String = "",
    val error: String = "",
    val commentSent: Boolean = false
)