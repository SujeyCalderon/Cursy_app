package com.example.cursy.features.notifications.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.notifications.domain.models.Notification
import com.example.cursy.features.notifications.domain.usecases.GetNotificationsUseCase
import com.example.cursy.features.notifications.domain.usecases.InsertNotificationUseCase
import com.example.cursy.features.notifications.domain.usecases.MarkNotificationAsReadUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationsUiState(
    val notifications: List<Notification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val insertNotificationUseCase: InsertNotificationUseCase,
    private val markNotificationAsReadUseCase: MarkNotificationAsReadUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getNotificationsUseCase()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { notifications ->
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            notifications = notifications
                        ) 
                    }
                }
        }
    }

    fun markAsRead(notificationId: Int) {
        viewModelScope.launch {
            try {
                markNotificationAsReadUseCase(notificationId)
            } catch (e: Exception) {

            }
        }
    }


    fun simulateNotification() {
        viewModelScope.launch {
            val count = _uiState.value.notifications.size + 1
            val notification = Notification(
                id = 0, 
                title = "Nueva alerta $count",
                message = "Esta es una notificación simulada de prueba para mostrar Room.",
                timestamp = System.currentTimeMillis(),
                isRead = false
            )
            insertNotificationUseCase(notification)
        }
    }
}
