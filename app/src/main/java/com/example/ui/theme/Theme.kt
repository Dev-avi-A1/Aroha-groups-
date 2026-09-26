package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ArohaDarkColorScheme = darkColorScheme(
    primary = ArohaTeal,
    onPrimary = Color(0xFF031C1A),
    primaryContainer = ArohaSurfaceHighlight,
    onPrimaryContainer = ArohaEmeraldLight,
    secondary = ArohaEmerald,
    onSecondary = Color(0xFF022117),
    secondaryContainer = ArohaSurfaceElevated,
    onSecondaryContainer = ArohaEmeraldLight,
    tertiary = ArohaGold,
    onTertiary = Color(0xFF261502),
    tertiaryContainer = Color(0xFF332005),
    onTertiaryContainer = ArohaGoldLight,
    background = ArohaDeepBackground,
    onBackground = ArohaTextPrimary,
    surface = ArohaDarkSurface,
    onSurface = ArohaTextPrimary,
    surfaceVariant = ArohaSurfaceElevated,
    onSurfaceVariant = ArohaTextSecondary,
    outline = ArohaCardBorder,
    error = ArohaError,
    onError = Color.White
)

private val ArohaLightColorScheme = lightColorScheme(
    primary = ArohaTealDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCCFBF1),
    onPrimaryContainer = Color(0xFF115E59),
    secondary = ArohaEmerald,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD1FAE5),
    onSecondaryContainer = Color(0xFF065F46),
    tertiary = ArohaGoldDark,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF92400E),
    background = Color(0xFFF8FAFC),
    onBackground = Color(0xFF0F172A),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    outline = Color(0xFFCBD5E1),
    error = ArohaError,
    onError = Color.White
)

@Composable
fun ArohaTheme(
    darkTheme: Boolean = true, // Default to Aroha's signature dark aesthetic
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ArohaDarkColorScheme else ArohaLightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
                WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = ArohaTypography,
        shapes = ArohaShapes,
        content = content
    )
}
