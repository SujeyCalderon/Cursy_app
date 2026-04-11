package com.example.cursy.features.feed.data.local

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.cursy.features.feed.data.repositories.DownloadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

// david: Servicio mejorado con soporte para cancelación y optimización de actualizaciones
@AndroidEntryPoint
class DownloadService : Service() {

    @Inject lateinit var repository: DownloadRepository
    @Inject lateinit var okHttpClient: OkHttpClient
    private lateinit var notificationManager: DownloadNotificationManager
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null

    companion object {
        const val ACTION_STOP = "STOP_DOWNLOAD"
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = DownloadNotificationManager(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            // david: Manejo de la acción de cancelar desde la notificación
            val courseId = intent.getStringExtra("courseId")
            serviceScope.launch {
                if (courseId != null) repository.updateProgress(courseId, 0, DownloadStatus.PENDING)
                stopSelf()
            }
            return START_NOT_STICKY
        }

        val courseId = intent?.getStringExtra("courseId") ?: return START_NOT_STICKY
        val title = intent.getStringExtra("title") ?: "Curso"
        val url = intent.getStringExtra("url") ?: return START_NOT_STICKY

        startForeground(1001, notificationManager.getBaseNotification(title, courseId).build())

        downloadJob = serviceScope.launch {
            downloadVideo(courseId, title, url)
        }

        return START_NOT_STICKY
    }

    private suspend fun downloadVideo(courseId: String, title: String, url: String) {
        val notificationBuilder = notificationManager.getBaseNotification(title, courseId)
        var lastUpdateProgress = -1
        
        try {
            val request = Request.Builder().url(url).build()
            val response = okHttpClient.newCall(request).execute()

            if (!response.isSuccessful) throw Exception("Error de red")

            val body = response.body ?: throw Exception("Cuerpo vacío")
            val totalBytes = body.contentLength()
            val file = File(getExternalFilesDir(null), "videos/$courseId.mp4")
            file.parentFile?.mkdirs()

            body.byteStream().use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    val buffer = ByteArray(64 * 1024) // david: Buffer más grande para mayor velocidad
                    var bytesRead: Int
                    var totalRead = 0L

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        // david: Verificar si el job fue cancelado para salir del bucle inmediatamente
                        if (!serviceScope.isActive) return@downloadVideo

                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead
                        
                        if (totalBytes > 0) {
                            val progress = ((totalRead * 100) / totalBytes).toInt()
                            
                            // david: Solo actualizar si el progreso cambió para no saturar Room
                            if (progress != lastUpdateProgress) {
                                lastUpdateProgress = progress
                                repository.updateProgress(courseId, progress, DownloadStatus.DOWNLOADING)
                                notificationManager.updateProgress(1001, notificationBuilder, progress)
                            }
                        }
                    }
                    outputStream.flush() // david: Asegurar que todos los bytes se escriban
                }
            }

            if (serviceScope.isActive) {
                repository.markAsCompleted(courseId, file.absolutePath)
                notificationManager.completeNotification(1001, title)
            }
            
        } catch (e: Exception) {
            if (serviceScope.isActive) {
                repository.updateProgress(courseId, 0, DownloadStatus.FAILED)
                notificationManager.errorNotification(1001, title)
            }
        } finally {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        downloadJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}
