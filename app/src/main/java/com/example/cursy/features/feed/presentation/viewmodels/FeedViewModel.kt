package com.example.cursy.features.feed.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.feed.domain.usecases.GetFeedUseCase
import com.example.cursy.features.feed.presentation.FeedUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedViewModel(private val getFeedUseCase: GetFeedUseCase) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            val result = getFeedUseCase()

            _uiState.update { currentState ->
                result.fold(
                    onSuccess = { courses ->
                        currentState.copy(
                            isLoading = false,
                            courses = courses,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        currentState.copy(
                            isLoading = false,
                            error = exception.message ?: "Error al cargar cursos"
                        )
                    }
                )
            }
        }
    }

    fun refresh() {
        loadFeed()
    }
}