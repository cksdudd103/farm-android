package com.smartfarm.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = FarmGreen40,
    onPrimary = White,
    primaryContainer = FarmGreen90,
    onPrimaryContainer = FarmGreen10,
    secondary = FarmEarth40,
    onSecondary = White,
    secondaryContainer = FarmEarth80,
    onSecondaryContainer = FarmGreen10,
    tertiary = FarmAmber50,
    onTertiary = White,
    background = FarmGreen95,
    onBackground = FarmGreen10,
    surface = White,
    onSurface = FarmGreen10,
    surfaceVariant = FarmGreen90,
    onSurfaceVariant = FarmGreen20,
    error = FarmRed50,
    onError = White,
    outline = FarmGreen70,
)

private val DarkColors = darkColorScheme(
    primary = FarmGreen70,
    onPrimary = FarmGreen10,
    primaryContainer = FarmGreen30,
    onPrimaryContainer = FarmGreen90,
    secondary = FarmEarth80,
    onSecondary = FarmGreen10,
    secondaryContainer = FarmEarth40,
    onSecondaryContainer = White,
    tertiary = FarmAmber80,
    onTertiary = FarmGreen10,
    background = FarmGreen10,
    onBackground = FarmGreen95,
    surface = FarmGreen20,
    onSurface = FarmGreen95,
    surfaceVariant = FarmGreen20,
    onSurfaceVariant = FarmGreen80,
    error = FarmRed80,
    onError = FarmGreen10,
    outline = FarmGreen60,
)

@Composable
fun SmartFarmTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
