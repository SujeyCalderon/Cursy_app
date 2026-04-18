package com.example.cursy.features.feed.domain.repository

import com.example.cursy.features.feed.domain.FeedLoadResult

interface FeedRepository {
    suspend fun getFeed(): Result<FeedLoadResult>
}