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

    @Inject
    lateinit var notificationDao: NotificationDao

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")
        Log.d(TAG, "Data payload: ${remoteMessage.data}") // ✅ Log completo para debug

        // ✅ PRIORIDAD 1: Data payload (funciona en foreground y background)
        val title = remoteMessage.data["title"]
            ?: remoteMessage.notification?.title
            ?: "Nuevo curso en Cursy"

        val body = remoteMessage.data["body"]
            ?: remoteMessage.notification?.body
            ?: "¡Mira el nuevo contenido disponible!"

        val type = remoteMessage.data["type"]
        val targetId = remoteMessage.data["course_id"] // ✅ Backend envía "course_id", no "target_id"
            ?: remoteMessage.data["target_id"]

        Log.d(TAG, "Procesando: title=$title, body=$body, type=$type, targetId=$targetId")

        if (title.isNotBlank() && body.isNotBlank()) {
            handleNotificationDisplayAndSave(title, body, type, targetId)
        } else {
            Log.w(TAG, "Notificación ignorada: título o cuerpo vacío")
        }
    }

    private fun handleNotificationDisplayAndSave(title: String, messageBody: String, type: String?, targetId: String?) {
        sendNotification(title, messageBody, type, targetId)

        serviceScope.launch {
            try {
                notificationDao.insertNotification(
                    NotificationEntity(
                        title = title,
                        message = messageBody,
                        timestamp = System.currentTimeMillis(),
                        isRead = false
                    )
                )
                Log.d(TAG, "Notificación guardada en base de datos local")
            } catch (e: Exception) {
                Log.e(TAG, "Error guardando notificación en DB local", e)
            }
        }
    }

    @Inject
    lateinit var api: com.example.cursy.core.network.CoursyApi

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: ${token.take(20)}...")

        // ✅ VALIDACIÓN: No enviar token vacío
        if (token.isBlank()) {
            Log.e(TAG, "❌ Token recibido está vacío, no se sincroniza")
            return
        }

        serviceScope.launch {
            try {
                val response = api.updateFCMToken(com.example.cursy.core.network.FCMTokenRequest(token))
                Log.d(TAG, "✅ Token FCM sincronizado: $response")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error sincronizando token FCM", e)
                // ✅ Reintentar en 10 segundos si falla
                kotlinx.coroutines.delay(10000)
                onNewToken(token) // Reintentar con mismo token
            }
        }
    }

    private fun sendNotification(title: String, messageBody: String) {
        sendNotification(title, messageBody, null, null)
    }

    private fun sendNotification(title: String, messageBody: String, type: String?, targetId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("notification_type", type)
            putExtra("target_id", targetId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "new_courses_channel"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cursy Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
