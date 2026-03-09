package com.example.cursy.features.chat.domain.usecases

import com.example.cursy.features.chat.domain.repositories.ChatRepository
import javax.inject.Inject

class SendMessageUseCase @Inject constructor(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(conversationId: String, receiverId: String, content: String): Result<Unit> {
        return repository.sendMessage(conversationId, receiverId, content)
    }
}
