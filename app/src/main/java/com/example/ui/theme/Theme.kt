package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

fun getDarkColorScheme(themeColor: AppThemeColor): androidx.compose.material3.ColorScheme {
    return darkColorScheme(
        primary = themeColor.light,
        onPrimary = Color.White,
        primaryContainer = themeColor.dark,
        onPrimaryContainer = Color(0xFFE0E7FF),
        secondary = ServexaTealLight,
        onSecondary = Color.Black,
        secondaryContainer = ServexaTealDark,
        onSecondaryContainer = Color(0xFFCCFBF1),
        tertiary = ServexaAmber,
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF78350F),
        onTertiaryContainer = ServexaAmberLight,
        background = DarkBg,
        onBackground = TextPrimaryDark,
        surface = DarkSurface,
        onSurface = TextPrimaryDark,
        surfaceVariant = DarkSurfaceCard,
        onSurfaceVariant = TextSecondaryDark,
        outline = DarkSurfaceBorder,
        error = ServexaRose,
        onError = Color.White
    )
}

fun getLightColorScheme(themeColor: AppThemeColor): androidx.compose.material3.ColorScheme {
    return lightColorScheme(
        primary = themeColor.primary,
        onPrimary = Color.White,
        primaryContainer = themeColor.light.copy(alpha = 0.18f),
        onPrimaryContainer = themeColor.dark,
        secondary = ServexaTeal,
        onSecondary = Color.White,
        secondaryContainer = Color(0xFFF0FDFA),
        onSecondaryContainer = ServexaTealDark,
        tertiary = ServexaAmber,
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFFFEF3C7),
        onTertiaryContainer = Color(0xFF92400E),
        background = LightBg,
        onBackground = TextPrimaryLight,
        surface = LightSurface,
        onSurface = TextPrimaryLight,
        surfaceVariant = LightSurfaceCard,
        onSurfaceVariant = TextSecondaryLight,
        outline = LightSurfaceBorder,
        error = ServexaRose,
        onError = Color.White
    )
}

@Composable
fun ServexaTheme(
    themeMode: AppThemeMode = AppThemeMode.LIGHT,
    themeColor: AppThemeColor = AppThemeColor.ROYAL_BLUE,
    content: @Composable () -> Unit
) {
    val isSystemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        AppThemeMode.LIGHT -> false
        AppThemeMode.DARK -> true
        AppThemeMode.SYSTEM -> isSystemDark
    }

    val colorScheme = if (isDark) getDarkColorScheme(themeColor) else getLightColorScheme(themeColor)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Backward compatibility alias
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    ServexaTheme(
        themeMode = if (darkTheme) AppThemeMode.DARK else AppThemeMode.LIGHT,
        content = content
    )
}


