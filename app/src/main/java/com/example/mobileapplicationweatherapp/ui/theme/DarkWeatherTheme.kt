package com.example.mobileapplicationweatherapp.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// Define custom colors for the dark theme
val DarkNavy = Color(0xFF1E2530)
val DarkBlue = Color(0xFF252D3A)
val DarkCard = Color(0xFF2A3341)
val AccentBlue = Color(0xFF3D5AFE)

// Create a custom dark color scheme
val DarkWeatherColorScheme = darkColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = DarkNavy,
    onPrimaryContainer = Color.White,
    secondary = AccentBlue,
    onSecondary = Color.White,
    secondaryContainer = DarkCard,
    onSecondaryContainer = Color.White,
    tertiary = AccentBlue,
    background = DarkBlue,
    surface = DarkCard,
    onSurface = Color.White,
    surfaceVariant = DarkCard,
    onSurfaceVariant = Color.LightGray,
    error = Color(0xFFFF5252)
)