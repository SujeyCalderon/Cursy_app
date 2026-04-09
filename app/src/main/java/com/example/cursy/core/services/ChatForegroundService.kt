package com.example.cursy.core.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.cursy.MainActivity
import com.example.cursy.R
import com.example.cursy.features.chat.domain.repositories.ChatRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Foreground Service que mantiene la conexión WebSocket del chat activa
 * incluso cuando la aplicación está en segundo plano o minimizada.
 */
@AndroidEntryPoint
class ChatForegroundService : Service() {

    @Inject
    lateinit var chatRepository: ChatRepository

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        Log.d(TAG, "ChatForegroundService creado")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Iniciando ChatForegroundService")
        
        // Android 14 requiere startForeground en los primeros segundos de onStartCommand
        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        // Iniciar la sesión de WebSocket a través del repositorio
        chatRepository.startSession()

        // START_STICKY asegura que el sistema intente recrear el servicio si es matado por falta de memoria
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        Log.d(TAG, "Destruyendo ChatForegroundService")
        // Cerramos la sesión al destruir el servicio (ej. Logout)
        chatRepository.endSession()
        super.onDestroy()
    }

    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = CHANNEL_ID
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Chat de Cursy activo")
            .setContentText("Conectado para recibir mensajes en tiempo real")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW) // Prioridad baja para que no sea intrusiva
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Estado del Chat",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene la conexión del chat en segundo plano"
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        private const val TAG = "ChatService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "chat_presence_channel"
        
        /**
         * Método de conveniencia para iniciar el servicio
         */
        fun start(context: Context) {
            val intent = Intent(context, ChatForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /**
         * Método de conveniencia para detener el servicio
         */
        fun stop(context: Context) {
            val intent = Intent(context, ChatForegroundService::class.java)
            context.stopService(intent)
        }
    }
}
