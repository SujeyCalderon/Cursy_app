package com.example.cursy

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.cursy.core.di.AppContainer
import com.example.cursy.core.network.LoginRequest
import com.example.cursy.features.Register.presenstation.screens.FormRegister
import com.example.cursy.navigation.AppNavigation
import com.example.cursy.ui.theme.CursyTheme
import kotlinx.coroutines.launch

private val GreenPrimary = Color(0xFF2ECC71)

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer()

        val prefs = getSharedPreferences("cursy_prefs", Context.MODE_PRIVATE)
        val savedDarkMode = prefs.getBoolean("dark_mode", false)

        setContent {
            /*
            var isDarkMode by remember { mutableStateOf(savedDarkMode) }
            var isLoggedIn by remember { mutableStateOf(false) }
            var isLoading by remember { mutableStateOf(true) }
            var error by remember { mutableStateOf<String?>(null) }
            val scope = rememberCoroutineScope()

            val onToggleDarkMode: (Boolean) -> Unit = { newValue ->
                isDarkMode = newValue
                prefs.edit().putBoolean("dark_mode", newValue).apply()
            }

            LaunchedEffect(Unit) {
                scope.launch {
                    try {
                        Log.d("MainActivity", "Iniciando login automático...")
                        val response = appContainer.coursyApi.login(
                            LoginRequest(
                                email = "usuario_prueba@test.com",
                                password = "Test123456"
                            )
                        )
                        Log.d("MainActivity", "Login exitoso: ${response.message}")
                        appContainer.setAuthToken(response.token)
                        isLoggedIn = true
                    } catch (e: Exception) {
                        Log.e("MainActivity", "Error en login: ${e.message}", e)
                        error = "Error de conexión: ${e.message}"
                    } finally {
                        isLoading = false
                    }
                }
            }

            CursyTheme(darkTheme = isDarkMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        isLoading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    CircularProgressIndicator(
                                        color = GreenPrimary
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        "Conectando con el servidor...",
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                            }
                        }
                        error != null -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.padding(32.dp)
                                ) {
                                    Text(
                                        text = "😕",
                                        style = MaterialTheme.typography.displayLarge
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(
                                        text = error!!,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            isLoading = true
                                            error = null
                                            scope.launch {
                                                try {
                                                    val response = appContainer.coursyApi.login(
                                                        LoginRequest(
                                                            email = "usuario_prueba@test.com",
                                                            password = "Test123456"
                                                        )
                                                    )
                                                    appContainer.setAuthToken(response.token)
                                                    isLoggedIn = true
                                                } catch (e: Exception) {
                                                    error = "Error de conexión: ${e.message}"
                                                } finally {
                                                    isLoading = false
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = GreenPrimary
                                        )
                                    ) {
                                        Text("Reintentar")
                                    }
                                }
                            }
                        }
                        isLoggedIn -> {
                            // App principal
                            AppNavigation(
                                appContainer = appContainer,
                                isDarkMode = isDarkMode,
                                onToggleDarkMode = onToggleDarkMode
                            )
                        }
                    }
                }
            }
            */
            FormRegister(appContainer = appContainer)
        }
    }
}