package com.vahitkeskin.loopsweep.ui.components

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
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
        isLoading -> ThemeOrange
        deviceStatusText == "Şarj Ediliyor" || deviceStatusText == "Dolu" -> EmeraldGreen
        deviceStatusText == "Süpürüyor" || deviceStatusText == "Paspas Yapıyor" || deviceStatusText.contains("Süpürüyor") -> ThemeBlue
        deviceStatusText == "Beklemede" -> Color.White.copy(alpha = 0.4f)
        else -> EmeraldGreen
    }

    // Pulse animation for status dot
    val infiniteTransition = rememberInfiniteTransition()
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

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
            // Pulse Glowing Dot
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(dotColor.copy(alpha = pulseAlpha))
                    .border(1.dp, dotColor, RoundedCornerShape(5.dp))
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
                // Visual Premium Battery Gauge
                BatteryGauge(
                    level = batteryLevel,
                    isCharging = isCharging,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(0.5.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
fun BatteryGauge(
    level: Int,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val batteryColor = when {
        level <= 20 -> AlertRed
        level <= 45 -> ThemeOrange
        else -> EmeraldGreen
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val chargeAlpha by if (isCharging) {
        infiniteTransition.animateFloat(
            initialValue = 0.5f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = "%$level",
            color = if (isCharging) EmeraldGreen else Color.White.copy(alpha = 0.9f),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(end = 6.dp)
        )
        Canvas(modifier = Modifier.size(width = 24.dp, height = 12.dp)) {
            val w = size.width
            val h = size.height
            val strokeW = 1.dp.toPx()
            val capWidth = 2.dp.toPx()
            
            // 1. Draw battery body outline
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(0f, 0f),
                size = Size(w - capWidth - 2.dp.toPx(), h),
                cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx()),
                style = Stroke(width = strokeW)
            )
            
            // 2. Draw battery cap on right side
            drawRoundRect(
                color = Color.White.copy(alpha = 0.35f),
                topLeft = Offset(w - capWidth - 1.dp.toPx(), h * 0.25f),
                size = Size(capWidth, h * 0.5f),
                cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
            )
            
            // 3. Draw fill level
            val maxFillW = w - capWidth - 4.dp.toPx() - strokeW * 2
            val fillW = maxFillW * (level.coerceIn(0, 100) / 100f)
            if (fillW > 0) {
                drawRoundRect(
                    color = batteryColor.copy(alpha = chargeAlpha),
                    topLeft = Offset(strokeW + 1.dp.toPx(), strokeW + 1.dp.toPx()),
                    size = Size(fillW, h - strokeW * 2 - 2.dp.toPx()),
                    cornerRadius = CornerRadius(1.dp.toPx(), 1.dp.toPx())
                )
            }
            
            // 4. Draw charging bolt overlay
            if (isCharging) {
                val cx = w / 2f - 1.dp.toPx()
                val cy = h / 2f
                val boltPath = Path().apply {
                    moveTo(cx + 1.5.dp.toPx(), cy - 4.dp.toPx())
                    lineTo(cx - 2.dp.toPx(), cy + 0.5.dp.toPx())
                    lineTo(cx + 0.5.dp.toPx(), cy + 0.5.dp.toPx())
                    lineTo(cx - 1.5.dp.toPx(), cy + 4.dp.toPx())
                    lineTo(cx + 2.dp.toPx(), cy - 0.5.dp.toPx())
                    lineTo(cx - 0.5.dp.toPx(), cy - 0.5.dp.toPx())
                    close()
                }
                drawPath(path = boltPath, color = Color.White)
            }
        }
    }
}
