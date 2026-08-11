package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = VaultDarkPrimary,
    onPrimary = VaultDarkOnPrimary,
    primaryContainer = VaultDarkPrimaryContainer,
    secondary = VaultDarkSecondary,
    background = VaultDarkBackground,
    surface = VaultDarkSurface,
    surfaceVariant = VaultDarkSurfaceVariant,
    onBackground = VaultDarkOnSurface,
    onSurface = VaultDarkOnSurface,
    onSurfaceVariant = VaultDarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = VaultLightPrimary,
    onPrimary = VaultLightOnPrimary,
    primaryContainer = VaultLightPrimaryContainer,
    secondary = VaultLightSecondary,
    background = VaultLightBackground,
    surface = VaultLightSurface,
    surfaceVariant = VaultLightSurfaceVariant,
    onBackground = VaultLightOnSurface,
    onSurface = VaultLightOnSurface,
    onSurfaceVariant = VaultLightOnSurfaceVariant
)

@Composable
fun LSAuthTheme(
    themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

