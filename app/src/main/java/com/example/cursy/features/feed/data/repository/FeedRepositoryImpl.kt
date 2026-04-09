package com.example.cursy.features.feed.data.repository

import com.example.cursy.core.network.CoursyApi
import com.example.cursy.features.feed.data.local.FeedDao
import com.example.cursy.features.feed.data.local.mapper.toCourseList
import com.example.cursy.features.feed.data.local.mapper.toFeedEntity
import com.example.cursy.features.feed.data.remote.mapper.toDomain
import com.example.cursy.features.feed.domain.FeedLoadResult
import com.example.cursy.features.feed.domain.repository.FeedRepository

class FeedRepositoryImpl @javax.inject.Inject constructor(
    private val api: CoursyApi,
    private val feedDao: FeedDao
) : FeedRepository {

    override suspend fun getFeed(): Result<FeedLoadResult> {
        return try {
            val response = api.getFeed()
            val courses = response.courses?.map { it.toDomain() } ?: emptyList()
            val entities = courses.mapIndexed { index, course -> course.toFeedEntity(index) }
            feedDao.replaceAll(entities)
            Result.success(FeedLoadResult(courses = courses, fromCache = false))
        } catch (e: Exception) {
            val cached = feedDao.getAllOrdered().toCourseList()
            if (cached.isNotEmpty()) {
                Result.success(FeedLoadResult(courses = cached, fromCache = true))
            } else {
                Result.failure(e)
            }
        }
    }
}