package com.example.cursy.features.Review.Domain.Usecases

import com.example.cursy.features.Review.Domain.Repository.ReviewRepository
import javax.inject.Inject

class DeleteCommentUseCase @Inject constructor(
    private val repository: ReviewRepository
) {
    suspend operator fun invoke(courseId: String, commentId: String): Result<Unit> {
        return repository.deleteComment(courseId, commentId)
    }
}