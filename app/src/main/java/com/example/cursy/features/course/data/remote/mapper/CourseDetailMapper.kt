package com.example.cursy.features.course.data.remote.mapper

import com.example.cursy.features.course.data.remote.dto.ContentBlockDto
import com.example.cursy.features.course.data.remote.dto.CourseDetailDto
import com.example.cursy.features.course.domain.entities.ContentBlock
import com.example.cursy.features.course.domain.entities.ContentBlockType
import com.example.cursy.features.course.domain.entities.CourseDetail

fun CourseDetailDto.toDomain(): CourseDetail {
    return CourseDetail(
        id = id,
        authorId = authorId ?: "",
        title = title,
        description = description,
        coverImage = coverImage ?: "",
        blocks = blocks?.map { it.toDomain() } ?: emptyList(),
        authorName = authorName ?: "Autor desconocido",
        authorImage = authorImage ?: ""
    )
}

fun ContentBlockDto.toDomain(): ContentBlock {
    return ContentBlock(
        type = when (type.uppercase()) {
            "HEADER" -> ContentBlockType.HEADER
            "TEXT" -> ContentBlockType.TEXT
            "IMAGE" -> ContentBlockType.IMAGE
            "VIDEO" -> ContentBlockType.VIDEO
            "CODE" -> ContentBlockType.CODE
            else -> ContentBlockType.TEXT
        },
        content = content,
        order = order ?: 0
    )
}