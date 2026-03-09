package com.example.cursy.features.chat.data.remote.mapper

import com.example.cursy.features.chat.data.remote.dto.ConversationDto
import com.example.cursy.features.chat.data.remote.dto.MessageDto
import com.example.cursy.features.chat.domain.entities.Conversation
import com.example.cursy.features.chat.domain.entities.Message
import java.text.SimpleDateFormat
import java.util.*

private val apiDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

fun ConversationDto.toDomain(): Conversation {
    return Conversation(
        id = id,
        otherUserId = otherUser?.id ?: (participants?.getOrNull(1) ?: ""),
        otherUserName = otherUser?.name ?: "Chat",
        otherUserImage = otherUser?.profileImage ?: "",
        lastMessage = lastMessage ?: "",
        updatedAt = try { 
            val dateStr = updatedAt?.substringBefore(".") ?: ""
            apiDateFormat.parse(dateStr) ?: Date() 
        } catch (e: Exception) { Date() }
    )
}

fun MessageDto.toDomain(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        content = content,
        createdAt = try { 
            val dateStr = updatedAt.substringBefore(".")
            apiDateFormat.parse(dateStr) ?: Date() 
        } catch (e: Exception) { Date() }
    )
}
