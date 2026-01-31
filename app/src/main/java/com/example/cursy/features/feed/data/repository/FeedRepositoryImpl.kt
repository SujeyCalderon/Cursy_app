package com.example.cursy.features.feed.data.repository

import com.example.cursy.core.network.CoursyApi
import com.example.cursy.features.feed.data.remote.mapper.toDomain
import com.example.cursy.features.feed.domain.entities.Course
import com.example.cursy.features.feed.domain.repository.FeedRepository

class FeedRepositoryImpl(private val api: CoursyApi) : FeedRepository {

    override suspend fun getFeed(): Result<List<Course>> {
        return try {
            val response = api.getFeed()
            val courses = response.courses?.map { it.toDomain() } ?: emptyList()
            Result.success(courses)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}