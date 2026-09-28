package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Hardware Monitor HUD Color Palette
val CyberCyan = Color(0xFF00E5FF)       // CPU primary
val ElectricPurple = Color(0xFFBD00FF)   // GPU primary
val AmberGold = Color(0xFFFFB300)        // RAM primary
val EmeraldGreen = Color(0xFF00E676)     // Health OK / Cool
val ThermalHot = Color(0xFFFF9100)       // Thermal warning
val CrimsonDanger = Color(0xFFFF1744)    // Thermal throttling / Critical

// Backgrounds & Surfaces
val SpaceDark = Color(0xFF080C14)
val SurfaceCard = Color(0xFF0F172A)
val SurfaceCardLight = Color(0xFF162038)
val SurfaceBorder = Color(0xFF1E2D4A)

// Neutral Text
val TextPrimary = Color(0xFFF1F5F9)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)

// Helper function for dynamic temperature coloring
fun getTemperatureColor(tempCelsius: Float): Color {
    return when {
        tempCelsius >= 90f -> CrimsonDanger
        tempCelsius >= 80f -> ThermalHot
        tempCelsius >= 68f -> AmberGold
        tempCelsius >= 50f -> CyberCyan
        else -> EmeraldGreen
    }
}

fun formatTemp(celsius: Float, useFahrenheit: Boolean): String {
    return if (useFahrenheit) {
        val fahrenheit = celsius * 9f / 5f + 32f
        "${String.format("%.1f", fahrenheit)}°F"
    } else {
        "${String.format("%.1f", celsius)}°C"
    }
}
