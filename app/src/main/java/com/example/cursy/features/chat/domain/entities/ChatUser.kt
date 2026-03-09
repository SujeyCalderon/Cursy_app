package com.example.cursy.features.chat.domain.entities

data class ChatUser(
    val id: String,
    val name: String,
    val email: String,
    val profileImage: String,
    val bio: String? = null
)
