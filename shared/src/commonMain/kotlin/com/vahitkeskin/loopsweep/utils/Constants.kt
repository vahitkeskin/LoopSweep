package com.vahitkeskin.loopsweep.utils

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.ui.graphics.Color
import com.vahitkeskin.loopsweep.domain.model.RoomItem

object Constants {
    const val VACUUM_PORT = 54321

    // Default rooms used as fallback when device is offline or room mapping unavailable
    val DEFAULT_ROOMS = listOf(
        RoomItem(16L, "Salon",         "🛋️", listOf(ThemePink, ThemeRose)),
        RoomItem(17L, "Yatak Odası",   "🛏️", listOf(MediumPurple, ThemeIndigo)),
        RoomItem(18L, "Mutfak",        "🍳", listOf(ThemeOrange, DarkAmber)),
        RoomItem(19L, "Banyo",         "🛁", listOf(ThemeCyan, DarkCyan)),
        RoomItem(20L, "Koridor",       "🧹", listOf(EmeraldGreen, DarkGreen)),
        RoomItem(21L, "Çalışma Odası", "💻", listOf(ThemeBlue, DarkBlue))
    )
}
