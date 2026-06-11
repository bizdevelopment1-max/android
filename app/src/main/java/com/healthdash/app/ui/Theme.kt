package com.healthdash.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

val LgBlue = Color(0xFF1428A0)

private val LightColors = lightColorScheme(
    primary = LgBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF000F5C),
    secondary = Color(0xFF3A4FC4),
    tertiary = Color(0xFF00897B)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFB9C3FF),
    onPrimary = Color(0xFF09218A),
    primaryContainer = Color(0xFF2C41B0),
    onPrimaryContainer = Color(0xFFDDE1FF),
    secondary = Color(0xFFA5B4FF),
    tertiary = Color(0xFF4DB6AC)
)

@Composable
fun HealthDashTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    val systemUi = rememberSystemUiController()
    SideEffect {
        systemUi.setStatusBarColor(if (dark) Color(0xFF0A1460) else LgBlue, darkIcons = false)
    }
    MaterialTheme(colorScheme = colors, content = content)
}
