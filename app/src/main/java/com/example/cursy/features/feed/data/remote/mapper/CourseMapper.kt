package com.example.cursy.features.feed.data.remote.mapper

import com.example.cursy.features.feed.data.remote.dto.CourseDto
import com.example.cursy.features.feed.domain.entities.Course

fun CourseDto.toDomain(): Course {
    return Course(
        id = id,
        authorId = authorId ?: "",
        title = title,
        description = description,
        coverImage = coverImage ?: "",
        authorName = authorName ?: "Autor desconocido",
        authorImage = authorImage ?: ""
    )
}