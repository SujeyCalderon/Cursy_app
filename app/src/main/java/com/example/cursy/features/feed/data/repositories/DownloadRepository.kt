package com.example.cursy.features.feed.data.repositories

import com.example.cursy.features.feed.data.local.DownloadDao
import com.example.cursy.features.feed.data.local.DownloadEntity
import com.example.cursy.features.feed.data.local.DownloadStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// david: Repositorio para gestionar la persistencia y estado de las descargas
@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun getDownload(courseId: String) = downloadDao.getDownloadById(courseId)

    suspend fun startDownload(courseId: String, title: String, url: String) {
        val entity = DownloadEntity(
            courseId = courseId,
            title = title,
            videoUrl = url,
            status = DownloadStatus.DOWNLOADING
        )
        downloadDao.insertOrUpdate(entity)
    }

    suspend fun updateProgress(courseId: String, progress: Int, status: DownloadStatus) {
        downloadDao.updateProgress(courseId, progress, status)
    }

    suspend fun markAsCompleted(courseId: String, localPath: String) {
        val current = downloadDao.getDownloadById(courseId)
        current?.let {
            downloadDao.insertOrUpdate(it.copy(
                status = DownloadStatus.COMPLETED,
                progress = 100,
                localPath = localPath
            ))
        }
    }

    suspend fun deleteDownload(courseId: String) {
        downloadDao.deleteDownload(courseId)
    }
}
