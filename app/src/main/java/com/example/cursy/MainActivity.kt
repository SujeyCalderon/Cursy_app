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
                        appContainer = appContainer,
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = onToggleDarkMode
                    )
                }
            }
        }
    }
}