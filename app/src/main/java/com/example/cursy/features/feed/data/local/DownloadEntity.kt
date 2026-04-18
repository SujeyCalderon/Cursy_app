package com.example.cursy.features.feed.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

// david: Entidad para rastrear el estado de las descargas offline
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val courseId: String,
    val title: String,
    val videoUrl: String,
    val localPath: String? = null,
    val progress: Int = 0,
    val status: DownloadStatus = DownloadStatus.PENDING
)

enum class DownloadStatus {
    PENDING, DOWNLOADING, COMPLETED, FAILED
}
