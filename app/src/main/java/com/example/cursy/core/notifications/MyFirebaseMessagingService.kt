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

        // Primero, intentamos leer desde el Data Payload (más fiable en background)
        if (remoteMessage.data.isNotEmpty()) {
            val title = remoteMessage.data["title"] ?: "Cursy"
            val body = remoteMessage.data["body"] ?: ""
            Log.d(TAG, "Message Data Body: $body")
            
            handleNotificationDisplayAndSave(title, body)
        } else {
            // Fallback: Si por alguna razón viene como Notificación (iOS / Consola Firebase)
            remoteMessage.notification?.let {
                Log.d(TAG, "Message Notification Body: ${it.body}")
                handleNotificationDisplayAndSave(it.title ?: "Cursy", it.body ?: "")
            }
        }
    }

    private fun handleNotificationDisplayAndSave(title: String, messageBody: String) {
        sendNotification(title, messageBody)

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

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        // Aquí se enviaría el token al servidor del backend si fuera necesario
    }

    private fun sendNotification(title: String, messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT
        )

        val channelId = "cursy_notifications"
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher) // Icono de la app
            .setContentTitle(title)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Canal necesario para Android Oreo (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Cursy Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0, notificationBuilder.build())
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
