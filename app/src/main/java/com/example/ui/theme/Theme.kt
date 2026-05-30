package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val PikPakDarkColorScheme = darkColorScheme(
    primary = NeonCherry,
    secondary = NeonCyan,
    tertiary = NeonGold,
    background = PremiumBlack,
    surface = DeepGrey,
    onPrimary = WhitePure,
    onSecondary = PremiumBlack,
    onTertiary = PremiumBlack,
    onBackground = WhitePure,
    onSurface = WhitePure
)

private val PikPakLightColorScheme = lightColorScheme(
    primary = NeonCherry,
    secondary = NeonCyan,
    tertiary = NeonGold,
    background = WhitePure,
    surface = androidx.compose.ui.graphics.Color(0xFFF1F3F5),
    onPrimary = WhitePure,
    onSecondary = PremiumBlack,
    onTertiary = PremiumBlack,
    onBackground = PremiumBlack,
    onSurface = PremiumBlack
)

@Composable
fun PikPakTheme(
    darkTheme: Boolean = true, // We default to dark mode for cinema feeling!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) PikPakDarkColorScheme else PikPakLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
