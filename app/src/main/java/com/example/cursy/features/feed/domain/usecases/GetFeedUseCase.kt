package com.example.cursy.features.feed.domain.usecases

import com.example.cursy.features.feed.domain.entities.Course
import com.example.cursy.features.feed.domain.repository.FeedRepository

class GetFeedUseCase(private val repository: FeedRepository) {
    suspend operator fun invoke(): Result<List<Course>> {
        return repository.getFeed()
    }
}