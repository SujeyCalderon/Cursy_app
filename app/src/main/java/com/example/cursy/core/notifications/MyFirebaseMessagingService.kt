package com.example.cursy.core.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cursy.MainActivity
import com.example.cursy.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import com.example.cursy.features.notifications.data.local.NotificationDao
import com.example.cursy.features.notifications.data.local.NotificationEntity

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var notificationDao: NotificationDao
    @Inject lateinit var api: com.example.cursy.core.network.CoursyApi

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "✅ onMessageReceived llamado")
        Log.d(TAG, "Data: ${remoteMessage.data}")
        Log.d(TAG, "Notification: ${remoteMessage.notification}")

        // ✅ Leer siempre desde data payload primero
        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "Nuevo curso en Cursy"

        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: "¡Mira el nuevo contenido disponible!"

        val type = remoteMessage.data["type"] ?: "new_course"
        val courseId = remoteMessage.data["course_id"] ?: remoteMessage.data["target_id"]

        Log.d(TAG, "title=$title | body=$body | type=$type | courseId=$courseId")

        showNotification(title, body, type, courseId)
        saveNotificationLocally(title, body)
    }

    private fun showNotification(title: String, body: String, type: String?, courseId: String?) {
        val channelId = "new_courses_channel"

        // Intent que abre la app con los datos del curso
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            putExtra("type", type)
            putExtra("course_id", courseId)
            putExtra("notification_type", type)
            putExtra("target_id", courseId)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Crear canal si es necesario
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Nuevos Cursos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando alguien sube un nuevo curso"
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 500, 200, 500)
                setShowBadge(true)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notification)

        Log.d(TAG, "🔔 Notificación mostrada: $title")
    }

    private fun saveNotificationLocally(title: String, body: String) {
        serviceScope.launch {
            try {
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = title,
                        message = body,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando notificación", e)
            }
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isBlank()) return
        Log.d(TAG, "Nuevo token: ${token.take(20)}...")

        serviceScope.launch {
            try {
                api.updateFCMToken(com.example.cursy.core.network.FCMTokenRequest(token))
                Log.d(TAG, "✅ Token sincronizado")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sincronizando token: ${e.message}")
            }
        }
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}