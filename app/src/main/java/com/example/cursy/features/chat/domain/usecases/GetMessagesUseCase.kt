package com.example.cursy.features.chat.domain.usecases

import com.example.cursy.features.chat.domain.entities.Message
import com.example.cursy.features.chat.domain.repositories.ChatRepository
import javax.inject.Inject

class GetMessagesUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String): Result<List<Message>> {
        return repository.getMessages(conversationId)
    }
}
