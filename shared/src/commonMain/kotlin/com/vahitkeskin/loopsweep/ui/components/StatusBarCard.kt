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
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
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
                    .background(if (isLoading) Color(0xFFF59E0B) else Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = statusMessage,
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
