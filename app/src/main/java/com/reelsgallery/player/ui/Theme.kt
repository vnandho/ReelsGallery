package com.reelsgallery.player.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ReelsDarkColors = darkColorScheme(
    primary = Color(0xFF9A8CFF),
    onPrimary = Color(0xFF171329),
    secondary = Color(0xFFB8B0FF),
    background = Color(0xFF090A0F),
    surface = Color(0xFF151720),
    surfaceVariant = Color(0xFF222532),
    onBackground = Color(0xFFF3F2FA),
    onSurface = Color(0xFFF3F2FA),
    onSurfaceVariant = Color(0xFFB8B9C7),
    outline = Color(0xFF454757)
)

private val ReelsLightColors = lightColorScheme(
    primary = Color(0xFF5545B8),
    onPrimary = Color.White,
    secondary = Color(0xFF6255A8),
    background = Color(0xFFF8F7FC),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEAE8F2),
    onBackground = Color(0xFF17151F),
    onSurface = Color(0xFF17151F),
    onSurfaceVariant = Color(0xFF555363),
    outline = Color(0xFF858292)
)

@Composable
fun ReelsGalleryTheme(appearance: String = "DARK", content: @Composable () -> Unit) {
    val dark = when (appearance) {
        "LIGHT" -> false
        "SYSTEM" -> isSystemInDarkTheme()
        else -> true
    }
    MaterialTheme(colorScheme = if (dark) ReelsDarkColors else ReelsLightColors, content = content)
}
