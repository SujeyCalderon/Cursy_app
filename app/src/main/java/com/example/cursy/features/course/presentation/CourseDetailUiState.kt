package com.example.cursy.features.course.presentation

import com.example.cursy.features.course.domain.entities.CourseDetail

data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val course: CourseDetail? = null,
    val isOwner: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val deleteSuccess: Boolean = false,
    val showMenu: Boolean = false
)