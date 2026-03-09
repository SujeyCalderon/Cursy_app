package com.example.cursy.features.chat.data.local.mapper

import com.example.cursy.features.chat.data.local.entities.MessageEntity
import com.example.cursy.features.chat.domain.entities.Message
import com.example.cursy.features.chat.domain.entities.MessageStatus
import java.util.Date

fun MessageEntity.toDomain(): Message {
    return Message(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        content = content,
        createdAt = Date(createdAt),
        type = type,
        status = try { MessageStatus.valueOf(status) } catch (e: Exception) { MessageStatus.SENT }
    )
}

fun Message.toEntity(): MessageEntity {
    return MessageEntity(
        id = id,
        conversationId = conversationId,
        senderId = senderId,
        content = content,
        createdAt = createdAt.time,
        type = type,
        status = status.name
    )
}
