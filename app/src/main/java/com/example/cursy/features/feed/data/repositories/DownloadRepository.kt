package com.example.cursy.features.feed.data.repositories

import android.util.Log
import com.example.cursy.features.feed.data.local.DownloadDao
import com.example.cursy.features.feed.data.local.DownloadEntity
import com.example.cursy.features.feed.data.local.DownloadStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

// david: Repositorio con logs añadidos para monitorear el estado de las descargas en Logcat
@Singleton
class DownloadRepository @Inject constructor(
    private val downloadDao: DownloadDao
) {
    private val TAG = "DownloadRepo"

    val allDownloads: Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()

    suspend fun getDownload(courseId: String) = downloadDao.getDownloadById(courseId)

    suspend fun startDownload(courseId: String, title: String, url: String) {
        Log.d(TAG, "DB -> Iniciando registro para: $title ($courseId)")
        val entity = DownloadEntity(
            courseId = courseId,
            title = title,
            videoUrl = url,
            status = DownloadStatus.DOWNLOADING
        )
        downloadDao.insertOrUpdate(entity)
    }

    suspend fun updateProgress(courseId: String, progress: Int, status: DownloadStatus) {
        // Logueamos solo cada 10% para evitar ruido excesivo
        if (progress % 10 == 0) {
            Log.d(TAG, "DB -> Progreso curso $courseId: $progress% | Estado: $status")
        }
        downloadDao.updateProgress(courseId, progress, status)
    }

    suspend fun markAsCompleted(courseId: String, localPath: String) {
        Log.d(TAG, "DB -> Marcando como COMPLETADO: $courseId")
        downloadDao.updateProgress(courseId, 100, DownloadStatus.COMPLETED)
        val current = downloadDao.getDownloadById(courseId)
        current?.let {
            downloadDao.insertOrUpdate(it.copy(
                status = DownloadStatus.COMPLETED,
                progress = 100,
                localPath = localPath
            ))
            Log.d(TAG, "DB -> Éxito: Registro finalizado para $courseId en $localPath")
        } ?: Log.e(TAG, "DB ERROR -> No se encontró el registro para $courseId al finalizar")
    }

    suspend fun deleteDownload(courseId: String) {
        Log.d(TAG, "DB -> Eliminando registro: $courseId")
        downloadDao.deleteDownload(courseId)
    }
}
