package com.example.cursy.features.feed.domain.usecases

import com.example.cursy.features.feed.domain.FeedLoadResult
import com.example.cursy.features.feed.domain.repository.FeedRepository

class GetFeedUseCase @javax.inject.Inject constructor(private val repository: FeedRepository) {
    suspend operator fun invoke(): Result<FeedLoadResult> {
        return repository.getFeed()
    }
}