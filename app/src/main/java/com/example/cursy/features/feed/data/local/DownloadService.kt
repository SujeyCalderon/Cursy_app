package com.example.cursy.features.feed.data.local

import android.app.Service
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.FileProvider
import com.example.cursy.features.feed.data.repositories.DownloadRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class DownloadService : Service() {

    @Inject lateinit var repository: DownloadRepository
    @Inject lateinit var okHttpClient: OkHttpClient
    private lateinit var notificationManager: DownloadNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null
    private var activeCall: Call? = null
    private val TAG = "DownloadService"

    private var lastNotificationUpdate = 0L
    private val NOTIFICATION_UPDATE_INTERVAL_MS = 500L

    companion object {
        const val ACTION_STOP = "STOP_DOWNLOAD"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager = DownloadNotificationManager(this)
        Log.d(TAG, "Servicio de descarga creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            val courseId = intent.getStringExtra("courseId")

            notificationManager.cancelNotification(NOTIFICATION_ID)
            stopForeground(STOP_FOREGROUND_REMOVE)

            activeCall?.cancel()
            activeCall = null
            downloadJob?.cancel()

            serviceScope.launch {
                if (courseId != null) {
                    repository.updateProgress(courseId, 0, DownloadStatus.PENDING)
                }
            }.invokeOnCompletion {
                stopSelf()
            }
            return START_NOT_STICKY
        }

        val courseId = intent?.getStringExtra("courseId") ?: return START_NOT_STICKY
        val title = intent.getStringExtra("title") ?: "Curso"
        val url = intent?.getStringExtra("url") ?: return START_NOT_STICKY

        Log.d(TAG, "Iniciando descarga de: $title | URL: $url")

        val initialNotification = notificationManager.getBaseNotification(title, courseId).build()
        startForeground(NOTIFICATION_ID, initialNotification)

        downloadJob = serviceScope.launch {
            downloadVideo(courseId, title, url)
        }

        return START_NOT_STICKY
    }

    private suspend fun downloadVideo(courseId: String, title: String, url: String) {
        var tempFile: File? = null

        try {
            repository.startDownload(courseId, title, url)

            val request = Request.Builder().url(url).build()
            val call = okHttpClient.newBuilder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build()
                .newCall(request)

            activeCall = call
            Log.d(TAG, "Ejecutando petición HTTP...")

            val response = withContext(Dispatchers.IO) { call.execute() }

            if (call.isCanceled()) {
                Log.d(TAG, "Petición cancelada antes de respuesta")
                return
            }

            if (!response.isSuccessful) {
                Log.e(TAG, "Error HTTP: ${response.code} - ${response.message}")
                throw Exception("Error HTTP ${response.code}")
            }

            val body = response.body ?: throw Exception("Cuerpo de respuesta nulo")
            val totalBytes = body.contentLength()
            Log.d(TAG, "Tamaño total del archivo: $totalBytes bytes")

            // Archivo temporal en cache (privado)
            val cacheDir = File(cacheDir, "downloads")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            tempFile = File(cacheDir, "$courseId.tmp")

            Log.d(TAG, "Empezando a escribir en archivo temporal: ${tempFile.absolutePath}")

            body.byteStream().use { inputStream ->
                FileOutputStream(tempFile).use { outputStream ->
                    val buffer = ByteArray(64 * 1024)
                    var bytesRead: Int
                    var totalRead = 0L
                    var lastLoggedProgress = -1

                    while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                        // Verificar si fue cancelado usando el Job
                        if (call.isCanceled() || downloadJob?.isCancelled == true) {
                            Log.d(TAG, "Descarga cancelada durante lectura")
                            return@use
                        }

                        outputStream.write(buffer, 0, bytesRead)
                        totalRead += bytesRead

                        if (totalBytes > 0) {
                            val progress = ((totalRead * 100) / totalBytes).toInt().coerceIn(0, 99)

                            if (progress != lastLoggedProgress && progress % 10 == 0) {
                                lastLoggedProgress = progress
                                repository.updateProgress(courseId, progress, DownloadStatus.DOWNLOADING)
                            }

                            val currentTime = System.currentTimeMillis()
                            if (currentTime - lastNotificationUpdate >= NOTIFICATION_UPDATE_INTERVAL_MS) {
                                lastNotificationUpdate = currentTime
                                notificationManager.updateProgress(NOTIFICATION_ID, title, progress)
                            }
                        }
                    }
                    outputStream.flush()
                }
            }

            // Verificar cancelación usando el Job
            if (call.isCanceled() || downloadJob?.isCancelled == true) {
                Log.d(TAG, "Descarga cancelada después de escritura")
                return
            }

            Log.d(TAG, "Descarga completada. Guardando en Galería pública...")

            // GUARDAR EN GALERÍA PÚBLICA
            val publicUri = saveToGallery(tempFile, title)

            if (publicUri != null) {
                repository.markAsCompleted(courseId, publicUri.toString())
                notificationManager.completeNotification(NOTIFICATION_ID, title, publicUri)
                Log.d(TAG, "Video guardado en Galería: $publicUri")
            } else {
                throw Exception("No se pudo guardar en la galería")
            }

        } catch (e: Exception) {
            if (activeCall?.isCanceled() == true || e is CancellationException) {
                Log.d(TAG, "Descarga detenida manualmente por el usuario")
            } else {
                Log.e(TAG, "ERROR FATAL en descarga: ${e.message}", e)
                repository.updateProgress(courseId, 0, DownloadStatus.FAILED)
                notificationManager.errorNotification(NOTIFICATION_ID, title)
            }
        } finally {
            activeCall = null
            // Limpiar archivo temporal
            if (tempFile?.exists() == true) {
                tempFile.delete()
                Log.d(TAG, "Archivo temporal eliminado")
            }
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            Log.d(TAG, "Servicio detenido")
        }
    }

    /**
     * Guarda el video en la Galería pública del dispositivo
     */
    private fun saveToGallery(tempFile: File, title: String): Uri? {
        val sanitizedTitle = title.replace(Regex("[^a-zA-Z0-9\\s]"), "_")
        val fileName = "Cursy_${sanitizedTitle}_${System.currentTimeMillis()}.mp4"

        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ (API 29+): Usar MediaStore
            saveUsingMediaStore(tempFile, fileName)
        } else {
            // Android 9 y anteriores: Guardar en directorio público
            saveUsingLegacyMethod(tempFile, fileName)
        }
    }

    /**
     * Para Android 10+: Usa MediaStore para guardar en Movies/Cursy
     */
    private fun saveUsingMediaStore(tempFile: File, fileName: String): Uri? {
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/Cursy")
            put(MediaStore.Video.Media.IS_PENDING, 1)
        }

        val resolver = contentResolver
        var uri: Uri? = null

        try {
            uri = resolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri == null) {
                Log.e(TAG, "MediaStore insert returned null")
                return null
            }

            // Copiar el archivo
            resolver.openOutputStream(uri)?.use { outputStream ->
                tempFile.inputStream().use { inputStream ->
                    val copied = inputStream.copyTo(outputStream)
                    Log.d(TAG, "Copiados $copied bytes a MediaStore")
                }
            }

            // Marcar como disponible
            contentValues.clear()
            contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            Log.d(TAG, "Video guardado en MediaStore: $uri")
            return uri

        } catch (e: Exception) {
            Log.e(TAG, "Error guardando en MediaStore: ${e.message}", e)
            uri?.let {
                try { resolver.delete(it, null, null) } catch (_: Exception) {}
            }
            return null
        }
    }

    /**
     * Para Android 9 y anteriores: Guarda en directorio público
     */
    @Suppress("DEPRECATION")
    private fun saveUsingLegacyMethod(tempFile: File, fileName: String): Uri? {
        return try {
            val moviesDir = android.os.Environment.getExternalStoragePublicDirectory(
                android.os.Environment.DIRECTORY_MOVIES
            )
            val cursyDir = File(moviesDir, "Cursy")
            if (!cursyDir.exists()) {
                cursyDir.mkdirs()
            }

            val destFile = File(cursyDir, fileName)
            tempFile.copyTo(destFile, overwrite = true)
            Log.d(TAG, "Archivo copiado a: ${destFile.absolutePath}")

            // Escanear para que aparezca en la galería
            val scanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
            scanIntent.data = Uri.fromFile(destFile)
            sendBroadcast(scanIntent)

            // Usar FileProvider para obtener URI segura
            FileProvider.getUriForFile(
                this,
                "${packageName}.provider",
                destFile
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error método legacy: ${e.message}", e)
            null
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Servicio destruido")
        activeCall?.cancel()
        downloadJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }
}