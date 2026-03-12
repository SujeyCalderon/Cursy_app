package com.example.cursy.features.Review.Domain.Repository

import com.example.cursy.features.Review.Domain.Entities.Review

interface ReviewRepository {
    suspend fun getComments(courseId: String): Result<List<Review>>
    suspend fun createComment(courseId: String, content: String): Result<Review>
}