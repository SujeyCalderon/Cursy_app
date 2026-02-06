package com.example.cursy.features.feed.presentation

import com.example.cursy.features.feed.domain.entities.Course

data class FeedUiState(
    val isLoading: Boolean = false,
    val courses: List<Course> = emptyList(),
    val error: String? = null,
    val showPublishFirstDialog: Boolean = false
)