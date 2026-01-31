package com.example.cursy.features.course.domain.entities

data class CourseDetail(
    val id: String,
    val authorId: String,
    val title: String,
    val description: String,
    val coverImage: String,
    val blocks: List<ContentBlock>,
    val authorName: String,
    val authorImage: String
)

data class ContentBlock(
    val type: ContentBlockType,
    val content: String,
    val order: Int
)

enum class ContentBlockType {
    HEADER,
    TEXT,
    IMAGE,
    VIDEO,
    CODE
}