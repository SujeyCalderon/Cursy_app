package com.example.cursy.features.chat.domain.repositories

import com.example.cursy.features.chat.domain.entities.ChatUser
import com.example.cursy.features.chat.domain.entities.Conversation
import com.example.cursy.features.chat.domain.entities.Message
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    suspend fun getConversations(): Result<List<Conversation>>
    suspend fun getMessages(conversationId: String): Result<List<Message>>
    suspend fun createConversation(otherUserId: String): Result<Conversation>
    suspend fun searchUsers(query: String?): Result<List<ChatUser>>

    // Tiempo real
    fun startSession()
    fun endSession()
    fun observeUserStatuses(): StateFlow<Map<String, Boolean>>
    suspend fun fetchOnlineUsers()
    suspend fun sendMessage(conversationId: String, receiverId: String, content: String): Result<Unit>

    // Observar mensajes desde Room (fuente de verdad)
    fun observeMessagesFromDb(conversationId: String): Flow<List<Message>>
}
