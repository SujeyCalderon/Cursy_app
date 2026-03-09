package com.example.cursy.features.chat.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey val id: String,
    val conversationId: String,
    val senderId: String,
    val content: String,
    val createdAt: Long,
    val type: String = "chat",
    val status: String = "SENT"
)
