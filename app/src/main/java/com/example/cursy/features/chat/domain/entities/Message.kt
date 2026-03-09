package com.example.cursy.features.chat.domain.entities

import java.util.Date

data class Message(
    val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: Date,
    val type: String = "chat",
    val status: MessageStatus = MessageStatus.SENT
)

enum class MessageStatus { PENDING, SENT, FAILED }
