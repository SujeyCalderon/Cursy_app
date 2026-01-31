package com.example.cursy.features.feed.domain.entities

data class Course(
    val id: String,
    val authorId: String,
    val title: String,
    val description: String,
    val coverImage: String,
    val authorName: String,
    val authorImage: String
)