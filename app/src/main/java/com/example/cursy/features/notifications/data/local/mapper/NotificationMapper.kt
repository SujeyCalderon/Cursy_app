package com.example.cursy.features.notifications.data.local.mapper

import com.example.cursy.features.notifications.data.local.NotificationEntity
import com.example.cursy.features.notifications.domain.models.Notification

fun NotificationEntity.toDomain(): Notification {
    return Notification(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        isRead = isRead
    )
}

fun Notification.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = id,
        title = title,
        message = message,
        timestamp = timestamp,
        isRead = isRead
    )
}
