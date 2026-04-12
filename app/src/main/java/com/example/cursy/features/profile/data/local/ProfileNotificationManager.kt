package com.example.cursy.features.profile.data.local

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.cursy.R

class ProfileNotificationManager(private val context: Context) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val channelId = "profile_updates_channel"
    private val notificationId = 1001

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Perfil",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de actualización de perfil"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showUploadingNotification() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Actualizando perfil")
            .setContentText("Subiendo tu foto...")
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    fun showSuccessNotification() {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_upload_done)
            .setContentTitle("¡Perfil actualizado!")
            .setContentText("Tu foto de perfil se ha subido correctamente.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    fun showErrorNotification(message: String? = null) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Error al actualizar perfil")
            .setContentText(message ?: "No se pudo subir la foto. Se reintentará pronto.")
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()
        notificationManager.notify(notificationId, notification)
    }

    fun dismissNotification() {
        notificationManager.cancel(notificationId)
    }
}
