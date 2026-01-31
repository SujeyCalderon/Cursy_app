package com.example.cursy.features.profile.presentation

import com.example.cursy.features.profile.domain.entities.CourseItem
import com.example.cursy.features.profile.domain.entities.Profile

data class ProfileUiState(
    val isLoading: Boolean = false,
    val profile: Profile? = null,
    val publishedCourses: List<CourseItem> = emptyList(),
    val draftCourses: List<CourseItem> = emptyList(),
    val savedCourses: List<CourseItem> = emptyList(),
    val selectedTab: Int = 0, // 0 = Mis Publicaciones, 1 = Guardados
    val error: String? = null
)