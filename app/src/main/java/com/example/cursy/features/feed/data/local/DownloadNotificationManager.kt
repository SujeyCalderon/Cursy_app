package com.example.cursy.features.feed.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cursy.R

// david: Manager actualizado con acción de cancelación
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
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun getBaseNotification(courseTitle: String, courseId: String): NotificationCompat.Builder {
        // david: Intent para cancelar la descarga desde la notificación
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
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setProgress(100, 0, false)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Cancelar", stopPendingIntent)
    }

    fun updateProgress(notificationId: Int, builder: NotificationCompat.Builder, progress: Int) {
        builder.setProgress(100, progress, false)
            .setContentText("$progress%")
        notificationManager.notify(notificationId, builder.build())
    }

    fun completeNotification(notificationId: Int, courseTitle: String) {
        val doneNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Descarga completada")
            .setContentText(courseTitle)
            .setOngoing(false)
            .build()
        notificationManager.notify(notificationId, doneNotification)
    }

    fun errorNotification(notificationId: Int, courseTitle: String) {
        val errorNotification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Error en descarga")
            .setContentText("No se pudo descargar $courseTitle")
            .setOngoing(false)
            .build()
        notificationManager.notify(notificationId, errorNotification)
    }
}
