package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Primary Brand - Majestic Royal Blue & Vibrant Accents
val ServexaRoyalBlue = Color(0xFF1D4ED8)
val ServexaRoyalBlueDark = Color(0xFF172554)
val ServexaRoyalBlueDeepNavy = Color(0xFF0F172A)
val ServexaRoyalBlueLight = Color(0xFF3B82F6)
val ServexaRoyalBlueVibrant = Color(0xFF2563EB)
val ServexaRoyalBlueSoft = Color(0xFF93C5FD)
val ServexaRoyalBlueIce = Color(0xFFEFF6FF)
val ServexaRoyalBlueBorder = Color(0xFFBFDBFE)

// Backward compatible aliases mapped directly to Royal Blue
val ServexaIndigo = ServexaRoyalBlue
val ServexaIndigoDark = ServexaRoyalBlueDark
val ServexaIndigoLight = ServexaRoyalBlueLight
val ServexaTeal = Color(0xFF0284C7) // Royal Cyan / Sapphire
val ServexaTealLight = Color(0xFF38BDF8)
val ServexaTealDark = Color(0xFF0369A1)

// Theme Palette Variants
val ThemeEmerald = Color(0xFF059669)
val ThemeEmeraldLight = Color(0xFF34D399)
val ThemeEmeraldDark = Color(0xFF065F46)

val ThemeOcean = Color(0xFF0284C7)
val ThemeOceanLight = Color(0xFF38BDF8)
val ThemeOceanDark = Color(0xFF0369A1)

val ThemeAmberCoral = Color(0xFFEA580C)
val ThemeAmberCoralLight = Color(0xFFFB923C)
val ThemeAmberCoralDark = Color(0xFF9A3412)

val ThemeViolet = Color(0xFF7C3AED)
val ThemeVioletLight = Color(0xFFA78BFA)
val ThemeVioletDark = Color(0xFF5B21B6)

// Accents & Badges
val ServexaAmber = Color(0xFFF59E0B)
val ServexaAmberLight = Color(0xFFFDE68A)
val ServexaRose = Color(0xFFE11D48)
val ServexaGreen = Color(0xFF10B981)
val ServexaSky = Color(0xFF0284C7)

// Neutral & Background
val DarkBg = Color(0xFF0B0F19)
val DarkSurface = Color(0xFF131B2E)
val DarkSurfaceCard = Color(0xFF1E293B)
val DarkSurfaceBorder = Color(0xFF334155)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)

val LightBg = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceCard = Color(0xFFF1F5F9)
val LightSurfaceBorder = Color(0xFFE2E8F0)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)

enum class AppThemeColor(val displayName: String, val primary: Color, val light: Color, val dark: Color) {
    ROYAL_BLUE("Royal Blue", ServexaRoyalBlue, ServexaRoyalBlueLight, ServexaRoyalBlueDark),
    INDIGO("Royal Blue", ServexaRoyalBlue, ServexaRoyalBlueLight, ServexaRoyalBlueDark),
    OCEAN("Ocean Sky", ThemeOcean, ThemeOceanLight, ThemeOceanDark),
    TEAL("Fresh Teal", ServexaTeal, ServexaTealLight, ServexaTealDark),
    EMERALD("Emerald Green", ThemeEmerald, ThemeEmeraldLight, ThemeEmeraldDark),
    CORAL("Sunset Coral", ThemeAmberCoral, ThemeAmberCoralLight, ThemeAmberCoralDark),
    VIOLET("Royal Violet", ThemeViolet, ThemeVioletLight, ThemeVioletDark)
}

enum class AppThemeMode(val displayName: String) {
    LIGHT("Light"),
    DARK("Dark"),
    SYSTEM("System Default")
}


