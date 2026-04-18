package com.example.cursy.features.notifications.data.repositories

import com.example.cursy.features.notifications.data.local.NotificationDao
import com.example.cursy.features.notifications.data.local.mapper.toDomain
import com.example.cursy.features.notifications.data.local.mapper.toEntity
import com.example.cursy.features.notifications.domain.models.Notification
import com.example.cursy.features.notifications.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotificationRepositoryImpl @Inject constructor(
    private val dao: NotificationDao
) : NotificationRepository {

    override fun getNotifications(): Flow<List<Notification>> {
        return dao.getNotifications().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertNotification(notification: Notification) {
        dao.insertNotification(notification.toEntity())
    }

    override suspend fun markAsRead(notificationId: Int) {
        dao.markAsRead(notificationId)
    }

    override suspend fun markAllAsRead() {
        dao.markAllAsRead()
    }
}
