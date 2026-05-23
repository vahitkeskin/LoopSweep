package com.vahitkeskin.loopsweep.ui.components

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeaderCard(
    ipAddress: String,
    onIpChange: (String) -> Unit,
    token: String,
    onTokenChange: (String) -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.15f), Color.White.copy(alpha = 0.02f))
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "⚡ LoopSweep",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    style = TextStyle(
                        shadow = Shadow(
                            color = MediumPurple.copy(alpha = 0.8f),
                            offset = Offset(0f, 0f),
                            blurRadius = 10f
                        )
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Panel",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White.copy(alpha = 0.6f)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = ThemeCyan,
                        strokeWidth = 2.dp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(18.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Glassmorphic Input for IP Address
                OutlinedTextField(
                    value = ipAddress,
                    onValueChange = onIpChange,
                    label = { Text("Vacuum IP Adresi") },
                    leadingIcon = { WifiLeadingIcon(color = MediumPurple) },
                    trailingIcon = {
                        if (ipAddress.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable { onIpChange("") }
                                    .padding(8.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedBorderColor = MediumPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedLabelColor = MediumPurple,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                // Glassmorphic Input for Token
                OutlinedTextField(
                    value = token,
                    onValueChange = onTokenChange,
                    label = { Text("32 Karakterli Hex Token") },
                    leadingIcon = { KeyLeadingIcon(color = MediumPurple) },
                    trailingIcon = {
                        if (token.isNotEmpty()) {
                            Text(
                                text = "✕",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                modifier = Modifier
                                    .clickable { onTokenChange("") }
                                    .padding(8.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                        focusedBorderColor = MediumPurple,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        focusedLabelColor = MediumPurple,
                        unfocusedLabelColor = Color.White.copy(alpha = 0.4f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

@Composable
fun WifiLeadingIcon(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h * 0.8f
        
        drawCircle(color = color, radius = 2.dp.toPx(), center = Offset(cx, cy))
        
        drawArc(
            color = color,
            startAngle = -140f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - 6.dp.toPx(), cy - 6.dp.toPx()),
            size = Size(12.dp.toPx(), 12.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
        
        drawArc(
            color = color,
            startAngle = -140f,
            sweepAngle = 100f,
            useCenter = false,
            topLeft = Offset(cx - 12.dp.toPx(), cy - 12.dp.toPx()),
            size = Size(24.dp.toPx(), 24.dp.toPx()),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun KeyLeadingIcon(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val cx = w * 0.35f
        val cy = h * 0.5f
        
        // Draw key head
        drawCircle(
            color = color,
            radius = 3.5.dp.toPx(),
            center = Offset(cx, cy),
            style = Stroke(width = 1.5.dp.toPx())
        )
        
        // Draw key shaft
        drawLine(
            color = color,
            start = Offset(cx + 3.5.dp.toPx(), cy),
            end = Offset(w * 0.85f, cy),
            strokeWidth = 1.5.dp.toPx()
        )
        
        // Draw key teeth
        drawLine(
            color = color,
            start = Offset(w * 0.65f, cy),
            end = Offset(w * 0.65f, cy + 3.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = color,
            start = Offset(w * 0.78f, cy),
            end = Offset(w * 0.78f, cy + 3.dp.toPx()),
            strokeWidth = 1.5.dp.toPx()
        )
    }
}
