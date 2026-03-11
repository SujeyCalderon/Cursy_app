package com.example.cursy.features.notifications.domain.models

data class Notification(
    val id: Int,
    val title: String,
    val message: String,
    val timestamp: Long,
    val isRead: Boolean
)
