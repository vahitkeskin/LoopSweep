package com.vahitkeskin.loopsweep.domain.model

import androidx.compose.ui.graphics.Color

data class RoomItem(
    val id: Int,
    val name: String,
    val icon: String,
    val gradientColors: List<Color>
)
