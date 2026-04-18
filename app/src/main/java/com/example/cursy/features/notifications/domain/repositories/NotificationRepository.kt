package com.example.cursy.features.notifications.domain.repositories

import com.example.cursy.features.notifications.domain.models.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<Notification>>
    suspend fun insertNotification(notification: Notification)
    suspend fun markAsRead(notificationId: Int)
    suspend fun markAllAsRead()
}
