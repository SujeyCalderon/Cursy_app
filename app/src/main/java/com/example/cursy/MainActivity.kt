package com.example.cursy

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.core.content.ContextCompat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.cursy.core.di.AuthManager
import com.example.cursy.core.network.CoursyApi
import com.example.cursy.core.network.FCMTokenRequest
import com.example.cursy.navigation.AppNavigation
import com.example.cursy.navigation.Screen
import com.example.cursy.ui.theme.CursyTheme
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    @Inject
    lateinit var authManager: AuthManager

    @Inject
    lateinit var api: CoursyApi

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted
        } else {
            // Permission denied
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        askNotificationPermission()

        val prefs = getSharedPreferences("cursy_prefs", Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        // Verificar si el usuario ya inició sesión
        val isUserLoggedIn = authManager.getAuthToken() != null
        val startDestination = if (isUserLoggedIn) Screen.Feed.route else Screen.Login.route

        // Enviar el token de Firebase al backend para vinculación
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                android.util.Log.d("FCM_TOKEN", "Token obtenido: $token")
                
                // Solo enviar si el usuario está logueado
                if (isUserLoggedIn) {
                    lifecycleScope.launch {
                        try {
                            api.updateFCMToken(FCMTokenRequest(token))
                            android.util.Log.d("FCM_TOKEN", "Token sincronizado con el servidor")
                        } catch (e: Exception) {
                            android.util.Log.e("FCM_TOKEN", "Error al sincronizar token: ${e.message}")
                        }
                    }
                }
            }
        }

        setContent {
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }

            val onToggleDarkMode: (Boolean) -> Unit = { newValue ->
                isDarkMode = newValue
                prefs.edit().putBoolean("dark_mode", newValue).apply()
            }

            CursyTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation(
                        startDestination = startDestination,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode
                    )
                }
            }
        }
    }

    private fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // Permission already granted
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }
}