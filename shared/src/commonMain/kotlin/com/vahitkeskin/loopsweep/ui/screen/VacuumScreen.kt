package com.vahitkeskin.loopsweep.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.vahitkeskin.loopsweep.ui.components.TelemetryDashboard
import com.vahitkeskin.loopsweep.ui.components.RealisticRadarCard
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun VacuumScreen(viewModel: VacuumViewModel) {
    val ipState by viewModel.ipAddress.collectAsState()
    val tokenState by viewModel.token.collectAsState()
    val isLoadingState by viewModel.isLoading.collectAsState()
    val statusMsgState by viewModel.statusMessage.collectAsState()
    val batteryLevelState by viewModel.batteryLevel.collectAsState()
    val deviceStatusState by viewModel.deviceStatusText.collectAsState()
    val isChargingState by viewModel.isCharging.collectAsState()
    
    // Telemetry Dashboard Flow States
    val telemetryState by viewModel.telemetry.collectAsState()
    val batteryHistoryState by viewModel.batteryHistory.collectAsState()
    val areaHistoryState by viewModel.areaHistory.collectAsState()
    val eventLogState by viewModel.eventLog.collectAsState()
    val distanceState by viewModel.distanceMeters.collectAsState()
    
    // Dynamic rooms from ViewModel (starts as DEFAULT_ROOMS, updates when device responds)
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val isRadarVisibleState by viewModel.isRadarVisible.collectAsState()
    val scrollState = rememberScrollState()

    // Track repeat counts locally per room index: starts at 1, goes up to 3
    // Re-initialized when rooms list size changes
    val repeatsState = remember(rooms.size) {
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
                .verticalScroll(scrollState)
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
                deviceStatusText = deviceStatusState,
                batteryLevel = batteryLevelState,
                isCharging = isChargingState,
                isLoading = isLoadingState
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            RealisticRadarCard(
                isVisible = isRadarVisibleState,
                onToggleVisibility = { viewModel.toggleRadarVisibility() },
                telemetry = telemetryState,
                deviceStatusText = deviceStatusState
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
            
            // Rooms grid rendered sequentially in Rows to allow scroll inside the Column container
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (i in rooms.indices step 2) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Room 1
                        Box(modifier = Modifier.weight(1f)) {
                            val room = rooms[i]
                            val currentRepeats = repeatsState[i]
                            RoomCard(
                                room = room,
                                currentRepeats = currentRepeats,
                                onRoomClick = {
                                    viewModel.cleanRoom(room.id, currentRepeats, room.isAllAreas)
                                },
                                onStepperClick = {
                                    // Increment repeats count: 1 -> 2 -> 3 -> 1
                                    repeatsState[i] = if (currentRepeats >= 3) 1 else currentRepeats + 1
                                },
                                isLoading = isLoadingState
                            )
                        }

                        // Room 2
                        Box(modifier = Modifier.weight(1f)) {
                            if (i + 1 < rooms.size) {
                                val room = rooms[i + 1]
                                val currentRepeats = repeatsState[i + 1]
                                RoomCard(
                                    room = room,
                                    currentRepeats = currentRepeats,
                                    onRoomClick = {
                                        viewModel.cleanRoom(room.id, currentRepeats, room.isAllAreas)
                                    },
                                    onStepperClick = {
                                        repeatsState[i + 1] = if (currentRepeats >= 3) 1 else currentRepeats + 1
                                    },
                                    isLoading = isLoadingState
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "TANI & TELEMETRİ PANELİ",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
            )

            // Diagnostic Telemetry Dashboard
            TelemetryDashboard(
                telemetry = telemetryState,
                batteryHistory = batteryHistoryState,
                areaHistory = areaHistoryState,
                eventLog = eventLogState,
                distanceMeters = distanceState
            )

            Spacer(modifier = Modifier.height(136.dp))
        }
    }
}
