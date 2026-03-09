package com.example.cursy.features.chat.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.chat.domain.entities.ChatUser
import com.example.cursy.features.chat.domain.entities.Conversation
import com.example.cursy.features.chat.domain.entities.Message
import com.example.cursy.features.chat.domain.usecases.CreateConversationUseCase
import com.example.cursy.features.chat.domain.usecases.GetConversationsUseCase
import com.example.cursy.features.chat.domain.usecases.GetMessagesUseCase
import com.example.cursy.features.chat.domain.usecases.SearchUsersUseCase
import com.example.cursy.features.chat.domain.usecases.SendMessageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatUiState(
    val conversations: List<Conversation> = emptyList(),
    val currentMessages: List<Message> = emptyList(),
    val currentConversation: Conversation? = null,
    val currentConversationId: String? = null,
    val searchUsers: List<ChatUser> = emptyList(),
    val currentUserId: String? = null,
    val myProfile: com.example.cursy.features.profile.domain.entities.Profile? = null,
    val userStatuses: Map<String, Boolean> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val getConversationsUseCase: GetConversationsUseCase,
    private val getMessagesUseCase: GetMessagesUseCase,
    private val sendMessageUseCase: SendMessageUseCase,
    private val searchUsersUseCase: SearchUsersUseCase,
    private val createConversationUseCase: CreateConversationUseCase,
    private val authManager: com.example.cursy.core.di.AuthManager,
    private val repository: com.example.cursy.features.chat.domain.repositories.ChatRepository,
    private val getMyProfileUseCase: com.example.cursy.features.profile.domain.usecases.GetMyProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState(currentUserId = authManager.getCurrentUserId()))
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()


    private val _navigationEvent = Channel<String>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private var messagesObserverJob: Job? = null

    init {
        loadConversations()
        loadMyProfile()
        repository.startSession()
        observeGlobalStatuses()
        // Respaldo REST para obtener estado en línea (el snapshot WebSocket puede perderse)
        viewModelScope.launch {
            kotlinx.coroutines.delay(1000)
            repository.fetchOnlineUsers()
        }
    }

    // Observa el estado en línea/desconectado de los usuarios
    private fun observeGlobalStatuses() {
        viewModelScope.launch {
            repository.observeUserStatuses().collect { statuses ->
                _uiState.update { it.copy(userStatuses = statuses) }
            }
        }
    }

    private fun loadMyProfile() {
        viewModelScope.launch {
            getMyProfileUseCase().onSuccess { profile ->
                _uiState.update { it.copy(myProfile = profile) }
            }
        }
    }

    fun loadConversations() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            getConversationsUseCase().onSuccess { conversations ->
                _uiState.update { it.copy(conversations = conversations, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }

    // Carga mensajes del servidor y observa Room como fuente de verdad
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, currentConversationId = conversationId) }

            var currentConv = _uiState.value.conversations.find { it.id == conversationId }
            if (currentConv == null) {
                getConversationsUseCase().onSuccess { conversations ->
                    _uiState.update { it.copy(conversations = conversations) }
                    currentConv = conversations.find { it.id == conversationId }
                }
            }

            _uiState.update { it.copy(currentConversation = currentConv ?: it.currentConversation) }

            getMessagesUseCase(conversationId)

            _uiState.update { it.copy(isLoading = false) }

            // Observar Room (SSOT): la UI se actualiza automáticamente
            messagesObserverJob?.cancel()
            messagesObserverJob = viewModelScope.launch {
                repository.observeMessagesFromDb(conversationId).collect { messages ->
                    _uiState.update { it.copy(currentMessages = messages) }
                }
            }
        }
    }

    // Envía un mensaje identificando al destinatario de la conversación
    fun sendMessage(conversationId: String, content: String) {
        viewModelScope.launch {
            var receiverId = _uiState.value.currentConversation?.otherUserId
                ?: _uiState.value.conversations.find { it.id == conversationId }?.otherUserId
                ?: _uiState.value.currentMessages.firstOrNull { it.senderId != _uiState.value.currentUserId }?.senderId
                ?: ""

            if (receiverId.isEmpty()) {
                getConversationsUseCase().onSuccess { conversations ->
                    _uiState.update { it.copy(conversations = conversations) }
                    receiverId = conversations.find { it.id == conversationId }?.otherUserId ?: ""
                }

                if (receiverId.isEmpty()) {
                    kotlinx.coroutines.delay(1000)
                    receiverId = _uiState.value.currentConversation?.otherUserId
                        ?: _uiState.value.conversations.find { it.id == conversationId }?.otherUserId
                        ?: ""
                }
            }

            if (receiverId.isEmpty()) {
                _uiState.update { it.copy(error = "No se pudo identificar al destinatario.") }
                return@launch
            }

            sendMessageUseCase(conversationId, receiverId, content).onFailure { error ->
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun searchUsers(query: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            searchUsersUseCase(query).onSuccess { users ->
                _uiState.update { it.copy(searchUsers = users, isLoading = false) }
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }

    fun createConversation(otherUserId: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            createConversationUseCase(otherUserId).onSuccess { conversation ->
                _uiState.update { it.copy(isLoading = false) }
                _navigationEvent.send(conversation.id)
            }.onFailure { error ->
                _uiState.update { it.copy(error = error.message, isLoading = false) }
            }
        }
    }
}
