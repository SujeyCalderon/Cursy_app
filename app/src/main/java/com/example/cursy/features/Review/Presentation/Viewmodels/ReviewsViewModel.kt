package com.example.cursy.features.Review.Presentation.Viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.core.di.AuthManager
import com.example.cursy.features.Review.Domain.UseCases.CreateCommentUseCase
import com.example.cursy.features.Review.Domain.UseCases.DeleteCommentUseCase
import com.example.cursy.features.Review.Domain.UseCases.GetCommentsUseCase
import com.example.cursy.features.Review.Presentation.CommentUiModel
import com.example.cursy.features.Review.Presentation.ReviewUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ReviewsViewModel @Inject constructor(
    private val getCommentsUseCase: GetCommentsUseCase,
    private val createCommentUseCase: CreateCommentUseCase,
    private val deleteCommentUseCase: DeleteCommentUseCase,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun onCommentTextChange(value: String) {
        _uiState.value = _uiState.value.copy(commentText = value)
    }

    fun getCurrentUserId(): String? = authManager.getCurrentUserId()

    fun loadComments(courseId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            getCommentsUseCase(courseId).fold(
                onSuccess = { reviews ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        comments  = reviews.map { review ->
                            CommentUiModel(
                                id        = review.id,
                                userId    = review.userId,
                                userName  = review.userName,
                                userImage = review.userImage,
                                content   = review.content,
                                createdAt = formatRelativeDate(review.createdAt)
                            )
                        }
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error     = "Error al cargar los comentarios"
                    )
                }
            )
        }
    }

    fun sendComment(courseId: String) {
        val content = _uiState.value.commentText
        if (content.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "El comentario no puede estar vacío")
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            createCommentUseCase(courseId, content).fold(
                onSuccess = { review ->
                    _uiState.value = _uiState.value.copy(
                        isSending   = false,
                        commentText = "",
                        commentSent = true,
                        comments    = listOf(
                            CommentUiModel(
                                id        = review.id,
                                userId    = review.userId,
                                userName  = review.userName,
                                userImage = review.userImage,
                                content   = review.content,
                                createdAt = formatRelativeDate(review.createdAt)
                            )
                        ) + _uiState.value.comments
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        isSending = false,
                        error     = "Error al enviar el comentario"
                    )
                }
            )
        }
    }

    fun deleteComment(courseId: String, commentId: String) {
        viewModelScope.launch {
            deleteCommentUseCase(courseId, commentId).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        comments = _uiState.value.comments.filter { it.id != commentId }
                    )
                },
                onFailure = {
                    _uiState.value = _uiState.value.copy(
                        error = "Error al eliminar el comentario"
                    )
                }
            )
        }
    }

    fun onCommentSentHandled() {
        _uiState.value = _uiState.value.copy(commentSent = false)
    }

    fun onErrorHandled() {
        _uiState.value = _uiState.value.copy(error = "")
    }

    private fun formatRelativeDate(dateStr: String): String {
        return try {
            val cleanDate = if (dateStr.contains(".")) {
                dateStr.substringBefore(".") + "Z"
            } else {
                dateStr
            }

            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(cleanDate) ?: return dateStr
            
            val now = Date()
            val diffMs = now.time - date.time
            val diffSecs = diffMs / 1000
            val diffMins = diffSecs / 60
            val diffHours = diffMins / 60
            val diffDays = diffHours / 24

            when {
                diffSecs < 60 -> "Hace un momento"
                diffMins < 60 -> "Hace $diffMins min"
                diffHours < 24 -> {
                    if (diffHours == 1L) "Hace 1 hora" else "Hace $diffHours horas"
                }
                diffDays == 1L -> "Ayer"
                diffDays < 7 -> "Hace $diffDays días"
                else -> {
                    val outFormat = SimpleDateFormat("dd 'de' MMM, hh:mm a", Locale("es", "MX"))
                    outFormat.format(date)
                }
            }
        } catch (e: Exception) {
            Log.e("ReviewsViewModel", "Error parsing date: $dateStr", e)
            dateStr
        }
    }
}