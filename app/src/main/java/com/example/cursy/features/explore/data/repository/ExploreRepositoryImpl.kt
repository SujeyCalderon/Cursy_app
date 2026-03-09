package com.example.cursy.features.explore.data.repository

import com.example.cursy.core.network.CoursyApi
import com.example.cursy.features.explore.data.remote.mapper.toDomain
import com.example.cursy.features.explore.domain.entities.UserItem
import com.example.cursy.features.explore.domain.repository.ExploreRepository
import javax.inject.Inject

class ExploreRepositoryImpl @Inject constructor(
    private val api: CoursyApi
) : ExploreRepository {

    override suspend fun getUsers(): Result<List<UserItem>> {
        return try {
            val response = api.getUsers()
            val users = response.users.map { it.toDomain() }
            Result.success(users)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}