package com.example.cursy.features.feed.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cursy.R

class DownloadNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "downloads_channel"

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Descargas",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de descarga de cursos"
                setShowBadge(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getBaseNotification(courseTitle: String, courseId: String): NotificationCompat.Builder {
        val stopIntent = Intent(context, DownloadService::class.java).apply {
            action = DownloadService.ACTION_STOP
            putExtra("courseId", courseId)
        }
        val stopPendingIntent = PendingIntent.getService(
            context,
            courseId.hashCode(),
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Descargando $courseTitle")
            .setContentText("0%")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", stopPendingIntent)
    }

    fun updateProgress(notificationId: Int, courseTitle: String, progress: Int) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle("Descargando $courseTitle")
            .setContentText("$progress%")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, progress, false)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun completeNotification(notificationId: Int, courseTitle: String, videoUri: Uri) {
        // Intent para abrir el video
        val viewIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(videoUri, "video/mp4")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
        }

        val viewPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            viewIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Descarga completada")
            .setContentText("$courseTitle guardado en Galería")
            .setOngoing(false)
            .setAutoCancel(true)
            .setProgress(0, 0, false)
            .setContentIntent(viewPendingIntent)
            .addAction(android.R.drawable.ic_menu_view, "Ver", viewPendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun errorNotification(notificationId: Int, courseTitle: String) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Error en descarga")
            .setContentText("No se pudo descargar $courseTitle")
            .setOngoing(false)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }
}