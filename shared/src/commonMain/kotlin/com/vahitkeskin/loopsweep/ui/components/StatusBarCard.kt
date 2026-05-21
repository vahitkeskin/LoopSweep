package com.vahitkeskin.loopsweep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun StatusBarCard(
    statusMessage: String,
    deviceStatusText: String,
    batteryLevel: Int?,
    isCharging: Boolean,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = when {
        isLoading -> Color(0xFFF59E0B) // Orange
        deviceStatusText == "Şarj Ediliyor" -> Color(0xFF10B981) // Green
        deviceStatusText == "Süpürüyor" || deviceStatusText == "Paspas Yapıyor" || deviceStatusText.contains("Süpürüyor") -> Color(0xFF3B82F6) // Blue
        deviceStatusText == "Beklemede" -> Color.White.copy(alpha = 0.4f) // Muted white
        else -> Color(0xFF10B981) // Default Green
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.1f), Color.White.copy(alpha = 0.01f))
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Süpürge: $deviceStatusText",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = statusMessage,
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }
            
            if (batteryLevel != null) {
                Spacer(modifier = Modifier.width(12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.06f))
                        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isCharging) "⚡ %$batteryLevel" else "🔋 %$batteryLevel",
                        color = if (isCharging) Color(0xFF10B981) else Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
