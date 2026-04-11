package com.example.cursy.features.course.presentation

import com.example.cursy.features.course.domain.entities.CourseDetail
import com.example.cursy.features.feed.data.local.DownloadStatus

data class CourseDetailUiState(
    val isLoading: Boolean = false,
    val course: CourseDetail? = null,
    val isOwner: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val deleteSuccess: Boolean = false,
    val showMenu: Boolean = false,
    // david: Campos para rastrear el estado de descarga en la UI
    val downloadStatus: DownloadStatus = DownloadStatus.PENDING,
    val downloadProgress: Int = 0
)
