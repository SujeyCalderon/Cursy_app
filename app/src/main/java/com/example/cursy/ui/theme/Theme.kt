package com.example.cursy.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    onPrimary = LightSurface,
    primaryContainer = GreenLight,
    onPrimaryContainer = GreenPrimaryDark,
    
    secondary = BluePrimary,
    onSecondary = LightSurface,
    
    background = LightBackground,
    onBackground = LightOnBackground,
    
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    
    error = ErrorRed,
    onError = LightSurface,
    
    outline = Divider
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimary,
    onPrimary = DarkSurface,
    primaryContainer = GreenPrimaryDark,
    onPrimaryContainer = GreenLight,
    
    secondary = BluePrimary,
    onSecondary = DarkSurface,
    
    background = DarkBackground,
    onBackground = DarkOnBackground,
    
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    
    error = ErrorRed,
    onError = DarkSurface,
    
    outline = DarkSurfaceVariant
)

@Composable
fun CursyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}