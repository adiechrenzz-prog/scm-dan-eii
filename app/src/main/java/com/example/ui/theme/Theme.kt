package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF0F264A),
    primaryContainer = Color(0xFF1E4484),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = EmeraldBright,
    onSecondary = Color(0xFF033228),
    tertiary = AmberBadge,
    background = DarkBackground,
    onBackground = Color(0xFFF1F6FD),
    surface = DarkSurface,
    onSurface = Color(0xFFF1F6FD),
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFC2D4EE),
    outline = DarkOutline,
    error = RoseAlert
)

private val LightColorScheme = lightColorScheme(
    primary = CobaltPrimary,
    onPrimary = Color.White,
    primaryContainer = CobaltContainer,
    onPrimaryContainer = CobaltPrimary,
    secondary = EmeraldSuccess,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSurface,
    onSecondaryContainer = EmeraldSuccess,
    tertiary = AmberWarning,
    onTertiary = Color.White,
    tertiaryContainer = AmberSurface,
    onTertiaryContainer = AmberWarning,
    background = SlateCanvas,
    onBackground = SlateTextPrimary,
    surface = SlateSurface,
    onSurface = SlateTextPrimary,
    surfaceVariant = SlateSurfaceVariant,
    onSurfaceVariant = SlateTextSecondary,
    outline = SlateBorder,
    error = RoseAlert,
    onError = Color.White
)

@Composable
fun EiTeamTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
