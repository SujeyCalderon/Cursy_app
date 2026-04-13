package com.example.cursy

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.Player
import androidx.media3.ui.PlayerView
import com.example.cursy.core.di.AuthManager
import com.example.cursy.core.network.CoursyApi
import com.example.cursy.core.network.FCMTokenRequest
import com.example.cursy.navigation.AppNavigation
import com.example.cursy.navigation.Screen
import com.example.cursy.core.services.ChatForegroundService
import com.example.cursy.core.services.PersistentVideoService
import com.example.cursy.ui.theme.CursyTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.work.*
import com.example.cursy.features.feed.data.worker.SyncWorker
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var api: CoursyApi

    private var videoServiceState = mutableStateOf<PersistentVideoService?>(null)
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as PersistentVideoService.VideoBinder
            videoServiceState.value = binder.getService()
            isBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            isBound = false
            videoServiceState.value = null
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
        } else {
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        createNotificationChannel()
        askNotificationPermission()
        scheduleSync()

        Intent(this, PersistentVideoService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        val prefs = getSharedPreferences("cursy_prefs", Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        val isUserLoggedIn = authManager.getAuthToken() != null
        
        val notificationType = intent.getStringExtra("notification_type")
        val targetId = intent.getStringExtra("target_id")
        
        val startDestination = if (isUserLoggedIn) {
            when (notificationType) {
                "new_course", "new_comment" -> {
                    if (targetId != null) Screen.CourseDetail.createRoute(targetId) else Screen.Feed.route
                }
                else -> Screen.Feed.route
            }
        } else {
            Screen.Login.route
        }

        if (isUserLoggedIn) {
            ChatForegroundService.start(this)
        }

        if (isUserLoggedIn) {
            syncFCMToken()
        }

        setContent {
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }
            val currentVideoService by videoServiceState

            // Estado global para saber si estamos en la pantalla de detalle
            var isDetailScreen by remember { mutableStateOf(false) }

            val onToggleDarkMode: (Boolean) -> Unit = { newValue ->
                isDarkMode = newValue
                prefs.edit().putBoolean("dark_mode", newValue).apply()
            }

            CompositionLocalProvider(
                LocalVideoService provides currentVideoService,
                LocalDetailScreenState provides { isDetailScreen = it }
            ) {
                CursyTheme(darkTheme = isDarkMode) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            AppNavigation(
                                startDestination = startDestination,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = onToggleDarkMode,
                                onLoginSuccess = {
                                    syncFCMToken()
                                }
                            )
                        }

                        // Mini Player Global flotante - SOLO si no estamos en detalle
                        currentVideoService?.let { service ->
                            val player = service.getPlayer()
                            if (player != null && !isDetailScreen) {
                                var playbackState by remember { mutableStateOf(player.playbackState) }
                                
                                DisposableEffect(player) {
                                    val listener = object : Player.Listener {
                                        override fun onPlaybackStateChanged(state: Int) {
                                            playbackState = state
                                        }
                                    }
                                    player.addListener(listener)
                                    onDispose {
                                        player.removeListener(listener)
                                    }
                                }

                                if (playbackState != Player.STATE_IDLE && playbackState != Player.STATE_ENDED) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(bottom = 100.dp, end = 16.dp)
                                    ) {
                                        AppMiniPlayer(player = player, onDismiss = { service.stopVideo() })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    private fun syncFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                lifecycleScope.launch {
                    try {
                        api.updateFCMToken(FCMTokenRequest(token))
                    } catch (e: Exception) {
                        android.util.Log.e("FCM_TOKEN", "Error al sincronizar token: ${e.message}")
                    }
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelId = "cursy_notifications"
            val channelName = "Cursy Notifications"
            val channelDescription = "Notificaciones de mensajes y cursos"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, channelName, importance).apply {
                description = channelDescription
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun scheduleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(1, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "FeedSyncWorker",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

@Composable
fun AppMiniPlayer(player: Player, onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .width(200.dp) // Un poco más ancho para los controles
            .height(150.dp)
            .shadow(12.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = player
                        useController = true // ACTIVAMOS CONTROLES (pausa, atrasar, etc)
                        setBackgroundColor(android.graphics.Color.BLACK)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
            
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(28.dp)
                    .padding(4.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

val LocalVideoService = staticCompositionLocalOf<PersistentVideoService?> { null }
val LocalDetailScreenState = staticCompositionLocalOf<(Boolean) -> Unit> { {} }
