package com.healthdash.app.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// MX AI Insights 브랜드 컬러
val AiViolet = Color(0xFF6D28D9)
val AiCyan = Color(0xFF22D3EE)
val AiMagenta = Color(0xFFEC4899)
val AiIndigo = Color(0xFF1A1048)

private val LightColors = lightColorScheme(
    primary = AiViolet,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFEDE4FF),
    onPrimaryContainer = Color(0xFF22074E),
    secondary = Color(0xFF0E7490),
    tertiary = AiMagenta
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFCBB6FF),
    onPrimary = Color(0xFF2C0E6B),
    primaryContainer = Color(0xFF4B1FA8),
    onPrimaryContainer = Color(0xFFEDE4FF),
    secondary = Color(0xFF67E8F9),
    tertiary = Color(0xFFF9A8D4)
)

@Composable
fun HealthDashTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val colors = if (dark) DarkColors else LightColors
    val systemUi = rememberSystemUiController()
    SideEffect {
        systemUi.setStatusBarColor(if (dark) AiIndigo else AiViolet, darkIcons = false)
    }
    MaterialTheme(colorScheme = colors, content = content)
}
