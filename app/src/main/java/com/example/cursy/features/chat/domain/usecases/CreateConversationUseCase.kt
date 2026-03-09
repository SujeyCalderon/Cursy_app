package com.example.cursy.features.chat.domain.usecases

import com.example.cursy.features.chat.domain.entities.Conversation
import com.example.cursy.features.chat.domain.repositories.ChatRepository
import javax.inject.Inject

class CreateConversationUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(otherUserId: String): Result<Conversation> {
        return repository.createConversation(otherUserId)
    }
}
