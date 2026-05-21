package com.vahitkeskin.loopsweep.utils

import androidx.compose.ui.graphics.Color
import com.vahitkeskin.loopsweep.domain.model.RoomItem

object Constants {
    const val VACUUM_PORT = 54321
    
    val DEFAULT_ROOMS = listOf(
        RoomItem(16, "Salon", "🛋️", listOf(Color(0xFFEC4899), Color(0xFFF43F5E))), // Pink-Rose
        RoomItem(17, "Yatak Odası", "🛏️", listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))), // Purple-Indigo
        RoomItem(18, "Mutfak", "🍳", listOf(Color(0xFFF59E0B), Color(0xFFD97706))), // Amber-Dark
        RoomItem(19, "Banyo", "🛁", listOf(Color(0xFF06B6D4), Color(0xFF0891B2))), // Cyan-Teal
        RoomItem(20, "Koridor", "🧹", listOf(Color(0xFF10B981), Color(0xFF059669))), // Emerald
        RoomItem(21, "Çalışma Odası", "💻", listOf(Color(0xFF3B82F6), Color(0xFF2563EB))) // Blue
    )
}
