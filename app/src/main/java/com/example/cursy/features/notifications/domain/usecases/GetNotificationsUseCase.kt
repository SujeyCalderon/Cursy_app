package com.example.cursy.features.notifications.domain.usecases

import com.example.cursy.features.notifications.domain.models.Notification
import com.example.cursy.features.notifications.domain.repositories.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val repository: NotificationRepository
) {
    operator fun invoke(): Flow<List<Notification>> {
        return repository.getNotifications()
    }
}
