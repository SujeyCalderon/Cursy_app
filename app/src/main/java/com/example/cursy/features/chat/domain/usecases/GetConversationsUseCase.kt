package com.example.cursy.features.chat.domain.usecases

import com.example.cursy.features.chat.domain.entities.Conversation
import com.example.cursy.features.chat.domain.repositories.ChatRepository
import javax.inject.Inject

class GetConversationsUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<Conversation>> {
        return repository.getConversations()
    }
}
