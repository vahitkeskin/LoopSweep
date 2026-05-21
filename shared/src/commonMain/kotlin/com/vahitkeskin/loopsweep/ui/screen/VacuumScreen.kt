package com.vahitkeskin.loopsweep.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.presentation.VacuumViewModel
import com.vahitkeskin.loopsweep.ui.components.HeaderCard
import com.vahitkeskin.loopsweep.ui.components.RoomCard
import com.vahitkeskin.loopsweep.ui.components.StatusBarCard
import com.vahitkeskin.loopsweep.utils.Constants

@Composable
fun VacuumScreen(viewModel: VacuumViewModel) {
    val ipState by viewModel.ipAddress.collectAsState()
    val tokenState by viewModel.token.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()
    val statusMsgState by viewModel.statusMessage.collectAsState()
    
    val rooms = remember { Constants.DEFAULT_ROOMS }
    
    // Track repeat counts locally per room index: starts at 1, goes up to 3
    val repeatsState = remember { 
        mutableStateListOf<Int>().apply { 
            addAll(List(rooms.size) { 1 }) 
        } 
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D)) // Extremely premium space dark background
    ) {
        // Glowing background blobs for glassmorphic light refraction
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF8B5CF6).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.25f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF06B6D4).copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.85f, size.height * 0.75f),
                    radius = size.width * 0.7f
                )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding()
                .padding(16.dp)
        ) {
            // Glassmorphic Header Card
            HeaderCard(
                ipAddress = ipState,
                onIpChange = { viewModel.ipAddress.value = it },
                token = tokenState,
                onTokenChange = { viewModel.token.value = it },
                isLoading = isLoadingState
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Glassmorphic Status Bar Card
            StatusBarCard(
                statusMessage = statusMsgState,
                isLoading = isLoadingState
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Text(
                text = "ODALAR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )
            
            // Rooms Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(rooms) { index, room ->
                    val currentRepeats = repeatsState[index]
                    
                    RoomCard(
                        room = room,
                        currentRepeats = currentRepeats,
                        onRoomClick = {
                            viewModel.cleanRoom(room.id, currentRepeats)
                        },
                        onStepperClick = {
                            // Increment repeats count: 1 -> 2 -> 3 -> 1
                            repeatsState[index] = if (currentRepeats >= 3) 1 else currentRepeats + 1
                        },
                        isLoading = isLoadingState
                    )
                }
            }
        }
    }
}
