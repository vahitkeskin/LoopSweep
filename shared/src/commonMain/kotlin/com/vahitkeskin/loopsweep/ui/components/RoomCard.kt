package com.vahitkeskin.loopsweep.ui.components

import com.vahitkeskin.loopsweep.ui.theme.*
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
import androidx.compose.ui.text.style.TextOverflow
import com.vahitkeskin.loopsweep.domain.model.RoomItem

@Composable
fun RoomCard(
    room: RoomItem,
    currentRepeats: Int,
    onRoomClick: () -> Unit,
    onIncrementRepeats: () -> Unit,
    onDecrementRepeats: () -> Unit,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.04f))
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
                .size(80.dp)
                .clip(RoundedCornerShape(40.dp))
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            room.gradientColors.firstOrNull()?.copy(alpha = 0.22f) ?: Color.Transparent,
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Interactive Stepper Pill (Top-Right)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Black.copy(alpha = 0.5f))
                .border(
                    0.5.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(10.dp)
                )
        ) {
            // Minus Button
            Box(
                modifier = Modifier
                    .clickable(enabled = currentRepeats > 1) { onDecrementRepeats() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "−",
                    color = if (currentRepeats > 1) Color.White else Color.White.copy(alpha = 0.25f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Divider
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(12.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            
            // Text Indicator
            Text(
                text = "${currentRepeats}x",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp)
            )
            
            // Divider
            Box(
                modifier = Modifier
                    .width(0.5.dp)
                    .height(12.dp)
                    .background(Color.White.copy(alpha = 0.15f))
            )
            
            // Plus Button
            Box(
                modifier = Modifier
                    .clickable(enabled = currentRepeats < 3) { onIncrementRepeats() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "+",
                    color = if (currentRepeats < 3) Color.White else Color.White.copy(alpha = 0.25f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
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
            Spacer(modifier = Modifier.height(6.dp))
        }
        
        // Bottom Translucent Gradient Bar containing Name & Loop details
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp)
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
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    )
                    
                    // Compact Room ID / All Area badge
                    val idStr = room.id.toString()
                    val compactId = if (room.isAllAreas) "🏠 TÜM" else if (idStr.length > 5) "…${idStr.takeLast(4)}" else idStr
                    val badgeColor = room.gradientColors.firstOrNull() ?: MediumPurple
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .border(
                                0.5.dp,
                                badgeColor.copy(alpha = 0.45f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = compactId,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor.copy(alpha = 0.95f),
                            maxLines = 1,
                            overflow = TextOverflow.Clip
                        )
                    }
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
