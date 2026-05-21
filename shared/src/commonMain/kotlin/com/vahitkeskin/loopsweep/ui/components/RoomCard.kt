package com.vahitkeskin.loopsweep.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import com.vahitkeskin.loopsweep.domain.model.RoomItem

@Composable
fun RoomCard(
    room: RoomItem,
    currentRepeats: Int,
    onRoomClick: () -> Unit,
    onStepperClick: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.12f), Color.White.copy(alpha = 0.01f))
                ),
                RoundedCornerShape(24.dp)
            )
            .clickable(enabled = !isLoading) {
                onRoomClick()
            }
    ) {
        // Ambient glowing color circle in the card background
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .size(70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            room.gradientColors[0].copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Repeat Counter Stepper Button (Top-Right)
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(
                    0.5.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(12.dp)
                )
                .clickable {
                    onStepperClick()
                }
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🔄 ${currentRepeats}x",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        // Large Room Icon & Name Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = room.icon,
                fontSize = 42.sp
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        
        // Bottom Translucent Gradient Bar containing Name & Loop details
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.75f))
                    )
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = room.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "ID: ${room.id}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.4f)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Döngü sayısı: $currentRepeats",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.5f)
                )
            }
        }
    }
}
