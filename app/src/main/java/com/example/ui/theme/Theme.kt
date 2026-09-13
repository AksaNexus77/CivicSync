package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color


private val CivicSyncColorScheme =
  darkColorScheme(
    primary = Emerald400,
    onPrimary = Slate900,
    primaryContainer = Emerald900,
    onPrimaryContainer = Emerald400,
    secondary = Indigo400,
    onSecondary = Slate900,
    secondaryContainer = Indigo900,
    onSecondaryContainer = Slate100,
    tertiary = Amber400,
    onTertiary = Slate900,
    tertiaryContainer = Amber900,
    onTertiaryContainer = Amber400,
    background = Slate900,
    onBackground = Slate100,
    surface = Slate800,
    onSurface = Slate100,
    surfaceVariant = Slate700,
    onSurfaceVariant = Slate300,
    outline = Slate600,
    outlineVariant = Slate700,
    error = Rose400,
    onError = Slate900
  )

@Composable
fun MyApplicationTheme(
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = CivicSyncColorScheme,
    typography = Typography,
    content = content
  )
}

