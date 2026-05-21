package com.vahitkeskin.loopsweep.domain.model

import androidx.compose.ui.graphics.Color

data class RoomItem(
    val id: Long,          // Real room ID from device (e.g. 1763994619) — Long for safety
    val name: String,
    val icon: String,
    val gradientColors: List<Color>,
    val isAllAreas: Boolean = false  // true = "Tüm ev" card (clean all), false = specific area
)
