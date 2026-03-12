package com.example.cursy.features.Review.Data.Repository

import com.example.cursy.core.network.CoursyApi
import com.example.cursy.features.Review.Data.Remote.Dto.CreateCommentRequest
import com.example.cursy.features.Review.Data.Remote.Mapper.toDomain
import com.example.cursy.features.Review.Domain.Entities.Review
import com.example.cursy.features.Review.Domain.Repository.ReviewRepository
import javax.inject.Inject

class ReviewRepositoryImpl @Inject constructor(
    private val api: CoursyApi
) : ReviewRepository {

    override suspend fun getComments(courseId: String): Result<List<Review>> {
        return try {
            val response = api.getComments(courseId)
            val reviews = response.comments?.map { it.toDomain() } ?: emptyList()
            Result.success(reviews)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createComment(courseId: String, content: String): Result<Review> {
        return try {
            val response = api.createComment(courseId, CreateCommentRequest(content))
            val review = response.comment?.toDomain()
                ?: return Result.failure(Exception("Respuesta vacía del servidor"))
            Result.success(review)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}