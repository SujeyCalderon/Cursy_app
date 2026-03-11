package com.example.cursy.features.notifications.domain.usecases

import com.example.cursy.features.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

class MarkNotificationAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke(notificationId: Int) {
        repository.markAsRead(notificationId)
    }
}
