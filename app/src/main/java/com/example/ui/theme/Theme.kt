package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PerformAIDarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = PitchBlack,
    primaryContainer = GoldAccentGlow,
    onPrimaryContainer = GoldAccent,
    secondary = GoldAccentDim,
    onSecondary = PitchBlack,
    secondaryContainer = ObsidianSurfaceElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = ObsidianSurfaceHighlight,
    onTertiary = TextPrimary,
    tertiaryContainer = ObsidianSurfaceElevated,
    onTertiaryContainer = TextSecondary,
    background = PitchBlack,
    onBackground = TextPrimary,
    surface = ObsidianSurface,
    onSurface = TextPrimary,
    surfaceVariant = ObsidianSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = ObsidianBorder,
    outlineVariant = ObsidianDivider
)

@Composable
fun PerformAIEvolutionTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PerformAIDarkColorScheme,
        typography = Typography,
        content = content
    )
}
