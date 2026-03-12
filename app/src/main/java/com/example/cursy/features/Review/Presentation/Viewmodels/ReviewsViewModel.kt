package com.example.cursy.features.Review.Presentation.Viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cursy.features.Review.Domain.UseCases.CreateCommentUseCase
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
    private val createCommentUseCase: CreateCommentUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewUiState())
    val uiState: StateFlow<ReviewUiState> = _uiState.asStateFlow()

    fun onCommentTextChange(value: String) {
        _uiState.value = _uiState.value.copy(commentText = value)
    }

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

    fun onCommentSentHandled() {
        _uiState.value = _uiState.value.copy(commentSent = false)
    }

    fun onErrorHandled() {
        _uiState.value = _uiState.value.copy(error = "")
    }

    private fun formatRelativeDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val date = sdf.parse(dateStr) ?: return dateStr
            val now = Date()
            val diffMs    = now.time - date.time
            val diffHours = diffMs / (1000 * 60 * 60)
            val diffDays  = diffMs / (1000 * 60 * 60 * 24)
            when {
                diffHours < 1  -> "Hace menos de 1 hora"
                diffHours < 24 -> "Hace $diffHours hora${if (diffHours > 1) "s" else ""}"
                diffDays == 1L -> "Ayer"
                diffDays < 7   -> "Hace $diffDays días"
                else           -> SimpleDateFormat("dd MMM yyyy", Locale("es")).format(date)
            }
        } catch (e: Exception) {
            dateStr
        }
    }
}