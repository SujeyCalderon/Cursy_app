package com.example.cursy

import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.work.*
import com.example.cursy.features.feed.data.worker.SyncWorker
import javax.inject.Inject
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject lateinit var authManager: AuthManager
    @Inject lateinit var api: CoursyApi

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
            // Reintentar obtener token si se concedió permiso
            if (authManager.getAuthToken() != null) {
                syncFCMToken()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ INICIALIZAR FIREBASE PRIMERO
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        enableEdgeToEdge()
        createNotificationChannels()
        askNotificationPermission()
        scheduleSync()

        Intent(this, PersistentVideoService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }

        val prefs = getSharedPreferences("cursy_prefs", Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean("dark_mode", false)
        val isUserLoggedIn = authManager.getAuthToken() != null

        // Manejar notificación que abrió la app
        handleNotificationIntent(intent)

        val startDestination = if (isUserLoggedIn) Screen.Feed.route else Screen.Login.route

        if (isUserLoggedIn) {
            ChatForegroundService.start(this)
        }

        setContent {
            // ... tu código de UI sin cambios
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }
            val currentVideoService by videoServiceState
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
                                    ChatForegroundService.start(this@MainActivity)
                                    syncFCMToken() // ✅ Sincronizar después de login
                                }
                            )
                        }

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
                                    onDispose { player.removeListener(listener) }
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

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleNotificationIntent(it) }
    }

    private fun handleNotificationIntent(intent: Intent) {
        val type = intent.getStringExtra("type")
            ?: intent.getStringExtra("notification_type")
        val courseId = intent.getStringExtra("course_id")
            ?: intent.getStringExtra("target_id")

        Log.d("MainActivity", "Notification intent: type=$type, courseId=$courseId")

        if (type == "new_course" && !courseId.isNullOrEmpty()) {
            // Guardar en prefs para que AppNavigation lo lea
            getSharedPreferences("pending_navigation", Context.MODE_PRIVATE)
                .edit()
                .putString("course_id", courseId)
                .apply()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isBound) {
            unbindService(connection)
            isBound = false
        }
    }

    // ✅ VERSIÓN CORREGIDA DE SYNCFCMTOKEN
    private fun syncFCMToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                android.util.Log.w("FCM", "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            android.util.Log.d("FCM", "Token obtenido: ${token.take(20)}...")

            if (token.isNullOrEmpty()) {
                android.util.Log.e("FCM", "Token es null o vacío")
                return@addOnCompleteListener
            }

            // Guardar localmente para debugging
            getSharedPreferences("fcm_debug", Context.MODE_PRIVATE)
                .edit()
                .putString("last_token", token)
                .putLong("token_timestamp", System.currentTimeMillis())
                .apply()

            lifecycleScope.launch {
                try {
                    val response = api.updateFCMToken(FCMTokenRequest(token))
                    android.util.Log.d("FCM", "Token sincronizado exitosamente: $response")
                } catch (e: Exception) {
                    android.util.Log.e("FCM", "Error al sincronizar token: ${e.message}", e)
                    // Reintentar en 5 segundos
                    kotlinx.coroutines.delay(5000)
                    syncFCMToken()
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                        PackageManager.PERMISSION_GRANTED -> {
                    // Ya tiene permiso, sincronizar token
                    if (authManager.getAuthToken() != null) {
                        syncFCMToken()
                    }
                }
                shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                    // Mostrar UI explicando por qué necesitas el permiso
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        } else {
            // Android 12 o menor, no se necesita permiso explícito
            if (authManager.getAuthToken() != null) {
                syncFCMToken()
            }
        }
    }

    // ✅ CREAR AMBOS CANALES NECESARIOS
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Canal para nuevos cursos (CRÍTICO)
            val coursesChannel = NotificationChannel(
                "new_courses_channel",
                "Nuevos Cursos",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones cuando alguien sube un nuevo curso"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }

            notificationManager.createNotificationChannels(listOf(coursesChannel))
            Log.d("MainActivity", "✅ Canal new_courses_channel creado")
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
            .width(200.dp)
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
                        useController = true
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