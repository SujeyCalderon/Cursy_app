package com.example.cursy.features.notifications.domain.usecases

import com.example.cursy.features.notifications.domain.repositories.NotificationRepository
import javax.inject.Inject

class MarkAllNotificationsAsReadUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    suspend operator fun invoke() {
        repository.markAllAsRead()
    }
}
