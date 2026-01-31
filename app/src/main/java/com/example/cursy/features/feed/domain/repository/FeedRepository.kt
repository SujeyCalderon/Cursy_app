package com.example.cursy.features.feed.domain.repository

import com.example.cursy.features.feed.domain.entities.Course

interface FeedRepository {
    suspend fun getFeed(): Result<List<Course>>
}