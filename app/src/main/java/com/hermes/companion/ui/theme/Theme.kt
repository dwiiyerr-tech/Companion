package com.hermes.companion.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = HermesPurple,
    onPrimary = DarkOnBackground,
    primaryContainer = HermesPurpleDark,
    onPrimaryContainer = Purple80,
    secondary = HermesBlue,
    onSecondary = DarkOnBackground,
    secondaryContainer = HermesBlueDark,
    onSecondaryContainer = Purple80,
    tertiary = StatusCyan,
    onTertiary = DarkOnBackground,
    tertiaryContainer = HermesPurpleSurface,
    onTertiaryContainer = Purple80,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOnSurfaceVariant,
    error = StatusRed,
    onError = DarkOnBackground,
    errorContainer = StatusRed,
    onErrorContainer = DarkOnBackground
)

@Composable
fun HermesCompanionTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = DarkBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            window.navigationBarColor = DarkSurface.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = HermesTypography,
        shapes = HermesShapes,
        content = content
    )
}
