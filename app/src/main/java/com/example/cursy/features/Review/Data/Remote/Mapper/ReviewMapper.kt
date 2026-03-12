package com.example.cursy.features.Review.Data.Remote.Mapper

import com.example.cursy.features.Review.Data.Remote.Dto.ReviewDto
import com.example.cursy.features.Review.Domain.Entities.Review

fun ReviewDto.toDomain(): Review {
    return Review(
        id        = id        ?: "",
        courseId  = courseId  ?: "",
        userId    = userId    ?: "",
        userName  = userName  ?: "Usuario desconocido",
        userImage = userImage ?: "",
        content   = content   ?: "",
        createdAt = createdAt ?: ""
    )
}