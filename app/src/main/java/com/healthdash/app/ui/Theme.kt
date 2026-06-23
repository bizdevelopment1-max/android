package com.healthdash.app.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// MX AI Insights 브랜드 컬러 (블루)
val AiBlue = Color(0xFF1E40AF)
val AiBlueBright = Color(0xFF2563EB)
val AiCyan = Color(0xFF22D3EE)
val AiDeep = Color(0xFF0A1A6E)

private val LightColors = lightColorScheme(
    primary = AiBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDCE6FF),
    onPrimaryContainer = Color(0xFF001A57),
    secondary = Color(0xFF0E7490),
    tertiary = Color(0xFF2563EB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFAFC6FF),
    onPrimary = Color(0xFF002A78),
    primaryContainer = Color(0xFF1E40AF),
    onPrimaryContainer = Color(0xFFDCE6FF),
    secondary = Color(0xFF67E8F9),
    tertiary = Color(0xFF93C5FD)
)

/** darkTheme를 호출자가 결정(시스템/라이트/다크 설정 반영). */
@Composable
fun HealthDashTheme(darkTheme: Boolean, content: @Composable () -> Unit) {
    val colors = if (darkTheme) DarkColors else LightColors
    val systemUi = rememberSystemUiController()
    SideEffect {
        systemUi.setStatusBarColor(if (darkTheme) AiDeep else AiBlue, darkIcons = false)
    }
    MaterialTheme(colorScheme = colors, content = content)
}
