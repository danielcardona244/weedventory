package com.example.weedventory.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4CAF50),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF42A5F5),
    onSecondary = Color.White,
    tertiary = Color(0xFFAB47BC),
    onTertiary = Color.White,
    error = Color(0xFFEF5350),
    onError = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = Color(0xFF2196F3),
    onSecondary = Color.White,
    tertiary = Color(0xFF9C27B0),
    onTertiary = Color.White,
    error = Color(0xFFB00020),
    onError = Color.White,
    background = Color(0xFFFAFAFA),
    onBackground = Color(0xFF212121),
    surface = Color.White,
    onSurface = Color(0xFF212121)
)

@Composable
fun WeedventoryTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
