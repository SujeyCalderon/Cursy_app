package com.example.cursy.features.chat.domain.usecases

import com.example.cursy.features.chat.domain.entities.ChatUser
import com.example.cursy.features.chat.domain.repositories.ChatRepository
import javax.inject.Inject

class SearchUsersUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(query: String? = null): Result<List<ChatUser>> {
        return repository.searchUsers(query)
    }
}
