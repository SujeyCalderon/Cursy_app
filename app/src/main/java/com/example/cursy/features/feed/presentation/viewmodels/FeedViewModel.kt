package com.example.cursy.features.feed.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.feed.domain.usecases.GetFeedUseCase
import com.example.cursy.features.feed.presentation.FeedUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.cursy.features.feed.domain.SyncManager

import com.example.cursy.features.chat.domain.repositories.ChatRepository

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val getFeedUseCase: GetFeedUseCase,
    private val chatRepository: ChatRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
        observeGlobalEvents()
    }

    private fun observeGlobalEvents() {
        viewModelScope.launch {
            chatRepository.globalEvents.collect { event ->
                if (event == "new_course") {
                    loadFeed()
                }
            }
        }
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getFeedUseCase()

            _uiState.update { currentState ->
                val newPosts = syncManager.getNewPostsCount()
                result.fold(
                    onSuccess = { load ->
                        currentState.copy(
                            isLoading = false,
                            courses = load.courses,
                            error = null,
                            showingCachedFeed = load.fromCache,
                            newPostsCount = newPosts
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar cursos",
                            showingCachedFeed = false,
                            newPostsCount = newPosts
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadFeed()
    }

    fun showPublishDialog() {
        _uiState.update { it.copy(showPublishFirstDialog = true) }
    }

    fun hidePublishDialog() {
        _uiState.update { it.copy(showPublishFirstDialog = false) }
    }

    fun clearNewPostsAlert() {
        syncManager.clearNewPostsCount()
        _uiState.update { it.copy(newPostsCount = 0) }
    }
}