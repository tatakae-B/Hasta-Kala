package com.hastakala.shop.ui.theme

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

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = DarkPrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnPrimary,
    tertiary = SageGreen,
    background = DarkBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = DarkOnSurface.copy(alpha = 0.7f),
    outline = DarkOutline,
    error = ErrorRed
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimary.copy(alpha = 0.08f),
    onPrimaryContainer = LightPrimary,
    secondary = LightSecondary,
    onSecondary = LightOnPrimary,
    tertiary = SageGreen,
    background = LightBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = ArtisanOffWhite,
    onSurfaceVariant = LightOnSurface.copy(alpha = 0.6f),
    outline = LightOutline,
    error = ErrorRed
)

private fun getColorScheme(darkTheme: Boolean, themeName: String): androidx.compose.material3.ColorScheme {
    return if (darkTheme) {
        when (themeName) {
            "Terracotta" -> DarkColorScheme.copy(primary = TerracottaPrimaryDark, secondary = TerracottaSecondaryDark)
            "Olive" -> DarkColorScheme.copy(primary = OlivePrimaryDark, secondary = OliveSecondaryDark)
            "Midnight" -> DarkColorScheme.copy(primary = MidnightPrimaryDark, secondary = MidnightSecondaryDark)
            "Rosewood" -> DarkColorScheme.copy(primary = RosewoodPrimaryDark, secondary = RosewoodSecondaryDark)
            "Gold" -> DarkColorScheme.copy(primary = GoldPrimaryDark, secondary = GoldSecondaryDark)
            "Brown" -> DarkColorScheme.copy(primary = BrownPrimaryDark, secondary = BrownSecondaryDark)
            else -> DarkColorScheme
        }
    } else {
        when (themeName) {
            "Terracotta" -> LightColorScheme.copy(primary = TerracottaPrimaryLight, secondary = TerracottaSecondaryLight)
            "Olive" -> LightColorScheme.copy(primary = OlivePrimaryLight, secondary = OliveSecondaryLight)
            "Midnight" -> LightColorScheme.copy(primary = MidnightPrimaryLight, secondary = MidnightSecondaryLight)
            "Rosewood" -> LightColorScheme.copy(primary = RosewoodPrimaryLight, secondary = RosewoodSecondaryLight)
            "Gold" -> LightColorScheme.copy(primary = GoldPrimaryLight, secondary = GoldSecondaryLight)
            "Brown" -> LightColorScheme.copy(primary = BrownPrimaryLight, secondary = BrownSecondaryLight)
            else -> LightColorScheme
        }
    }
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeName: String = "Brown",
    fontSize: String = "Medium",
    content: @Composable () -> Unit
) {
    val scale = when (fontSize) {
        "Small" -> 0.85f
        "Large" -> 1.15f
        else -> 1.0f
    }
    
    val colorScheme = getColorScheme(darkTheme, themeName)


    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = getTypography(scale),
        content = content
    )
}
