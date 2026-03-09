package com.example.cursy.features.explore.domain.usecases

import com.example.cursy.features.explore.domain.entities.UserItem
import com.example.cursy.features.explore.domain.repository.ExploreRepository
import javax.inject.Inject

class GetUsersUseCase @Inject constructor(
    private val repository: ExploreRepository
) {
    suspend operator fun invoke(): Result<List<UserItem>> {
        return repository.getUsers()
    }
}