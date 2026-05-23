package com.vahitkeskin.loopsweep.ui.screen

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    
    // Telemetry Flow States
    val telemetryState by viewModel.telemetry.collectAsState()
    val batteryHistoryState by viewModel.batteryHistory.collectAsState()
    val areaHistoryState by viewModel.areaHistory.collectAsState()
    val eventLogState by viewModel.eventLog.collectAsState()
    val distanceState by viewModel.distanceMeters.collectAsState()
    val isCleaning = remember(telemetryState) {
        val sc = telemetryState?.statusCode
        sc == 5 || sc == 6 || sc == 7 || sc == 3
    }
    
    // Dynamic rooms from ViewModel
    val rooms by viewModel.rooms.collectAsStateWithLifecycle()
    val isRadarVisibleState by viewModel.isRadarVisible.collectAsState()
    val scrollState = rememberScrollState()

    // Track repeat counts locally per room index
    val repeatsState = remember(rooms.size) {
        mutableStateListOf<Int>().apply {
            addAll(List(rooms.size) { 1 })
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
    ) {
        // Glowing background blobs for glassmorphic light refraction
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(MediumPurple.copy(alpha = 0.12f), Color.Transparent),
                    center = Offset(size.width * 0.15f, size.height * 0.25f),
                    radius = size.width * 0.7f
                )
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(ThemeCyan.copy(alpha = 0.12f), Color.Transparent),
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

            // Quick control buttons - visible when vacuum is active
            if (isCleaning) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Durdur button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFE11D48), Color(0xFFFB7185))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.stopCleaning() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⏹️  Durdur",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    // Şarj İstasyonuna Dön button
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(Color(0xFFD97706), Color(0xFFFBBF24))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { viewModel.returnToDock() }
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🏠  Şarj İstasyonu",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

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
            
            // Rooms grid rendered sequentially in Rows
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
                                onIncrementRepeats = {
                                    repeatsState[i] = (currentRepeats + 1).coerceAtMost(3)
                                },
                                onDecrementRepeats = {
                                    repeatsState[i] = (currentRepeats - 1).coerceAtLeast(1)
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
                                    onIncrementRepeats = {
                                        repeatsState[i + 1] = (currentRepeats + 1).coerceAtMost(3)
                                    },
                                    onDecrementRepeats = {
                                        repeatsState[i + 1] = (currentRepeats - 1).coerceAtLeast(1)
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
