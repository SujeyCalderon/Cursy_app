package com.example.cursy.features.explore.domain.repository

import com.example.cursy.features.explore.domain.entities.UserItem

interface ExploreRepository {
    suspend fun getUsers(): Result<List<UserItem>>
}