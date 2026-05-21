package com.vahitkeskin.loopsweep.utils

import androidx.compose.ui.graphics.Color
import com.vahitkeskin.loopsweep.domain.model.RoomItem

object Constants {
    const val VACUUM_PORT = 54321

    // Default rooms used as fallback when device is offline or room mapping unavailable
    val DEFAULT_ROOMS = listOf(
        RoomItem(16L, "Salon",         "🛋️", listOf(Color(0xFFEC4899), Color(0xFFF43F5E))),
        RoomItem(17L, "Yatak Odası",   "🛏️", listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))),
        RoomItem(18L, "Mutfak",        "🍳", listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
        RoomItem(19L, "Banyo",         "🛁", listOf(Color(0xFF06B6D4), Color(0xFF0891B2))),
        RoomItem(20L, "Koridor",       "🧹", listOf(Color(0xFF10B981), Color(0xFF059669))),
        RoomItem(21L, "Çalışma Odası", "💻", listOf(Color(0xFF3B82F6), Color(0xFF2563EB)))
    )
}
