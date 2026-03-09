package com.example.cursy.features.chat.domain.entities

import java.util.Date

data class Conversation(
    val id: String,
    val otherUserId: String,
    val otherUserName: String,
    val otherUserImage: String,
    val lastMessage: String,
    val updatedAt: Date
)
