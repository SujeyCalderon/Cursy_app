package com.example.cursy.features.Review.Domain.UseCases

import com.example.cursy.features.Review.Domain.Entities.Review
import com.example.cursy.features.Review.Domain.Repository.ReviewRepository
import javax.inject.Inject

class CreateCommentUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(courseId: String, content: String): Result<Review> {
        return repository.createComment(courseId, content)
    }
}