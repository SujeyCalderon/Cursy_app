package com.example.cursy.features.profile.domain.entities

data class Profile(
    val id: String,
    val name: String,
    val password: String? = null,
    val email: String,
    val profileImage: String,
    val bio: String,
    val university: String,
    val publishedCoursesCount: Int,
    val draftCoursesCount: Int,
    val savedCoursesCount: Int
)

data class CourseItem(
    val id: String,
    val title: String,
    val description: String,
    val coverImage: String,
    val isDraft: Boolean
)