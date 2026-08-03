package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = PillPrimary,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF00522B),
    onPrimaryContainer = Color(0xFF8BFFC1),
    secondary = PillPrimaryDark,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00497A),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = PillSecondary,
    background = DarkCanvas,
    onBackground = Color(0xFFF1F5F9),
    surface = DarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = DarkSurfacePill,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF008744),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB9F6CA),
    onPrimaryContainer = Color(0xFF00210B),
    secondary = Color(0xFF0077C2),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE1F5FE),
    onSecondaryContainer = Color(0xFF001D33),
    tertiary = PillSecondary,
    background = LightCanvas,
    onBackground = Color(0xFF0F172A),
    surface = LightSurface,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = LightSurfacePill,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun ExpenseTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true, // Adapts to the phone system theme on Android 12+
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

