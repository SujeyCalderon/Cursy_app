package com.example.cursy.features.notifications.domain.usecases

import com.example.cursy.features.notifications.domain.models.Notification
import com.example.cursy.features.notifications.domain.repositories.NotificationRepository
import com.example.cursy.core.Hardware.Domain.DeviceNotifier
import javax.inject.Inject

class InsertNotificationUseCase @Inject constructor(
    private val repository: NotificationRepository,
    private val deviceNotifier: DeviceNotifier
) {
    suspend operator fun invoke(notification: Notification) {
        repository.insertNotification(notification)
        deviceNotifier.playNotificationFeedback()
    }
}
