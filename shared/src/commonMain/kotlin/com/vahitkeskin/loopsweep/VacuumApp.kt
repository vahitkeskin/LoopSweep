package com.vahitkeskin.loopsweep

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.animation.core.*
import com.vahitkeskin.loopsweep.di.AppContainer
import com.vahitkeskin.loopsweep.presentation.VacuumViewModel
import com.vahitkeskin.loopsweep.presentation.XiaomiCloudViewModel
import com.vahitkeskin.loopsweep.ui.screen.VacuumScreen
import com.vahitkeskin.loopsweep.ui.screen.XiaomiCloudScreen
import com.vahitkeskin.loopsweep.ui.components.RobotVacuumButtonContent

enum class Screen {
    Dashboard, Cloud
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacuumApp() {
    val appContainer = remember { AppContainer() }
    
    val viewModel = remember { 
        VacuumViewModel(
            cleanRoomUseCase = appContainer.cleanRoomUseCase,
            getVacuumPropertiesUseCase = appContainer.getVacuumPropertiesUseCase,
            getVacuumTelemetryUseCase = appContainer.getVacuumTelemetryUseCase,
            getRoomsUseCase = appContainer.getRoomsUseCase,
            stopVacuumUseCase = appContainer.stopVacuumUseCase,
            dockVacuumUseCase = appContainer.dockVacuumUseCase,
            dataStore = appContainer.dataStore
        )
    }

    val cloudViewModel = remember {
        XiaomiCloudViewModel(
            loginXiaomiCloudUseCase = appContainer.loginXiaomiCloudUseCase,
            getXiaomiDevicesUseCase = appContainer.getXiaomiDevicesUseCase
        )
    }

    var currentScreen by remember { mutableStateOf(Screen.Dashboard) }

    val activeIp by viewModel.ipAddress.collectAsState()
    val activeToken by viewModel.token.collectAsState()

    val telemetry by viewModel.telemetry.collectAsState()
    val isCharging by viewModel.isCharging.collectAsState()
    val isCleaning = remember(telemetry) {
        val sc = telemetry?.statusCode
        sc == 5 || sc == 6 || sc == 7
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
    ) {
        Scaffold(
            bottomBar = {
                GlassmorphicBottomNavigation(
                    currentScreen = currentScreen,
                    onScreenSelected = { currentScreen = it },
                    isCleaning = isCleaning,
                    isCharging = isCharging,
                    onCleanClicked = {
                        viewModel.cleanRoom(99, 1, isAllAreas = true)
                    },
                    onStopClicked = {
                        viewModel.stopCleaning()
                    },
                    onDockClicked = {
                        viewModel.returnToDock()
                    }
                )
            },
            containerColor = Color.Transparent
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = paddingValues.calculateTopPadding())
            ) {
                when (currentScreen) {
                    Screen.Dashboard -> {
                        VacuumScreen(viewModel = viewModel)
                    }
                    Screen.Cloud -> {
                        XiaomiCloudScreen(
                            viewModel = cloudViewModel,
                            activeIp = activeIp,
                            activeToken = activeToken,
                            onSaveConnection = { ip, token ->
                                viewModel.updateConnection(ip, token)
                            }
                        )
                    }
                }
            }
        }
    }
}

class CutoutShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            val cutoutW = with(density) { 120.dp.toPx() }
            val cutoutH = with(density) { 36.dp.toPx() }
            val center = width / 2f
            
            val leftStart = center - cutoutW / 2f
            val rightEnd = center + cutoutW / 2f
            
            moveTo(0f, 0f)
            lineTo(leftStart, 0f)
            
            cubicTo(
                x1 = leftStart + cutoutW * 0.3f, y1 = 0f,
                x2 = center - cutoutW * 0.25f, y2 = cutoutH,
                x3 = center, y3 = cutoutH
            )
            cubicTo(
                x1 = center + cutoutW * 0.25f, y1 = cutoutH,
                x2 = rightEnd - cutoutW * 0.3f, y2 = 0f,
                x3 = rightEnd, y3 = 0f
            )
            
            lineTo(width, 0f)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun BarcodeIcon(color: Color = Color.Black) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        drawLine(color, Offset(w * 0.15f, h * 0.2f), Offset(w * 0.15f, h * 0.8f), strokeWidth * 1.5f)
        drawLine(color, Offset(w * 0.3f, h * 0.2f), Offset(w * 0.3f, h * 0.8f), strokeWidth * 0.5f)
        drawLine(color, Offset(w * 0.42f, h * 0.2f), Offset(w * 0.42f, h * 0.8f), strokeWidth * 1.2f)
        drawLine(color, Offset(w * 0.55f, h * 0.2f), Offset(w * 0.55f, h * 0.8f), strokeWidth * 0.8f)
        drawLine(color, Offset(w * 0.7f, h * 0.2f), Offset(w * 0.7f, h * 0.8f), strokeWidth * 1.6f)
        drawLine(color, Offset(w * 0.85f, h * 0.2f), Offset(w * 0.85f, h * 0.8f), strokeWidth * 0.5f)
    }
}

@Composable
fun StatisticsIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val barWidth = w / 4f
        val spacing = w / 8f
        
        drawRoundRect(
            color = color,
            topLeft = Offset(spacing, h * 0.5f),
            size = Size(barWidth, h * 0.5f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(spacing * 2 + barWidth, h * 0.2f),
            size = Size(barWidth, h * 0.8f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(spacing * 3 + barWidth * 2, h * 0.6f),
            size = Size(barWidth, h * 0.4f),
            cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
    }
}

@Composable
fun PurchaseIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        
        val pathHandle = Path().apply {
            moveTo(w * 0.25f, h * 0.5f)
            quadraticTo(w * 0.5f, h * 0.1f, w * 0.75f, h * 0.5f)
        }
        drawPath(
            path = pathHandle,
            color = color,
            style = Stroke(width = strokeWidth)
        )
        
        val pathBasket = Path().apply {
            moveTo(w * 0.15f, h * 0.5f)
            lineTo(w * 0.85f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.9f)
            lineTo(w * 0.25f, h * 0.9f)
            close()
        }
        drawPath(
            path = pathBasket,
            color = color
        )
        
        drawCircle(
            color = Color.White,
            radius = 2.dp.toPx(),
            center = Offset(w * 0.5f, h * 0.7f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassmorphicBottomNavigation(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    isCleaning: Boolean,
    isCharging: Boolean,
    onCleanClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onDockClicked: () -> Unit
) {
    val barHeight = 80.dp
    val buttonSize = 58.dp
    val outerHeight = 124.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(outerHeight),
        contentAlignment = Alignment.BottomCenter
    ) {
        // 1. Bottom bar - opaque background with CutoutShape, grey line on top profile
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight)
        ) {
            val width = size.width
            val height = size.height
            val cutoutW = 120.dp.toPx()
            val cutoutH = 36.dp.toPx()
            val center = width / 2f
            val leftStart = center - cutoutW / 2f
            val rightEnd = center + cutoutW / 2f
            val strokeWidth = 1.dp.toPx()
            val halfStroke = strokeWidth / 2f

            // Draw opaque background clipped to CutoutShape
            val bgPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, 0f)
                lineTo(leftStart, 0f)
                cubicTo(
                    x1 = leftStart + cutoutW * 0.3f, y1 = 0f,
                    x2 = center - cutoutW * 0.25f, y2 = cutoutH,
                    x3 = center, y3 = cutoutH
                )
                cubicTo(
                    x1 = center + cutoutW * 0.25f, y1 = cutoutH,
                    x2 = rightEnd - cutoutW * 0.3f, y2 = 0f,
                    x3 = rightEnd, y3 = 0f
                )
                lineTo(width, 0f)
                lineTo(width, height)
                lineTo(0f, height)
                close()
            }
            drawPath(
                path = bgPath,
                color = Color(0xFF0A0F1D)
            )

            // Draw grey line along the top profile of the cutout
            val topPath = androidx.compose.ui.graphics.Path().apply {
                moveTo(0f, halfStroke)
                lineTo(leftStart, halfStroke)
                cubicTo(
                    x1 = leftStart + cutoutW * 0.3f, y1 = halfStroke,
                    x2 = center - cutoutW * 0.25f, y2 = cutoutH + halfStroke,
                    x3 = center, y3 = cutoutH + halfStroke
                )
                cubicTo(
                    x1 = center + cutoutW * 0.25f, y1 = cutoutH + halfStroke,
                    x2 = rightEnd - cutoutW * 0.3f, y2 = halfStroke,
                    x3 = rightEnd, y3 = halfStroke
                )
                lineTo(width, halfStroke)
            }
            drawPath(
                path = topPath,
                color = Color.DarkGray,
                style = Stroke(width = strokeWidth)
            )
        }

        // 2. The Row containing three columns aligned to the bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            verticalAlignment = Alignment.Bottom
        ) {
            // Left Column (Statistics)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { onScreenSelected(Screen.Dashboard) }
                    )
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StatisticsIcon(color = if (currentScreen == Screen.Dashboard) Color(0xFFFBBF24) else Color(0xFF787880))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Statistics",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentScreen == Screen.Dashboard) Color(0xFFFBBF24) else Color(0xFF787880)
                )
            }

            // Middle Column (Scan text placeholder - aligned with other columns)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Spacer(modifier = Modifier.height(32.dp))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Start",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF787880)
                )
            }

            // Right Column (Purchase)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { onScreenSelected(Screen.Cloud) }
                    )
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Box(
                    modifier = Modifier.height(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    PurchaseIcon(color = if (currentScreen == Screen.Cloud) Color(0xFFFBBF24) else Color(0xFF787880))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Purchase",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentScreen == Screen.Cloud) Color(0xFFFBBF24) else Color(0xFF787880)
                )
            }
        }

        // 3. The Robot Vacuum Button - positioned absolutely over the cutout
        var showMenu by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp) // Slightly higher padding for visible gap from cutout
                .size(buttonSize)
                .combinedClickable(
                    onClick = { onCleanClicked() },
                    onLongClick = { showMenu = true }
                ),
            contentAlignment = Alignment.Center
        ) {
            RobotVacuumButtonContent(
                isCleaning = isCleaning,
                isCharging = isCharging
            )

            // Long-press balloon popup
            if (showMenu) {
                VacuumControlBalloon(
                    buttonSizePx = with(LocalDensity.current) { buttonSize.roundToPx() },
                    onDismiss = { showMenu = false },
                    onStopClicked = { showMenu = false; onStopClicked() },
                    onDockClicked = { showMenu = false; onDockClicked() }
                )
            }
        }
    }
}

@Composable
fun VacuumControlBalloon(
    buttonSizePx: Int,
    onDismiss: () -> Unit,
    onStopClicked: () -> Unit,
    onDockClicked: () -> Unit
) {
    val arrowHeightDp = 14.dp
    val balloonColor = Color(0xFF141C30)
    val borderColor = Color(0xFF3D4A6B)

    Popup(
        alignment = Alignment.BottomCenter,
        offset = IntOffset(0, -(buttonSizePx + with(LocalDensity.current) { 6.dp.roundToPx() })),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .width(230.dp)
                .wrapContentHeight()
                .drawBehind {
                    val arrowH = arrowHeightDp.toPx()
                    val bodyH = size.height - arrowH
                    val cornerR = CornerRadius(14.dp.toPx())
                    val cx = size.width / 2f
                    val arrowW = 24.dp.toPx()

                    // Drop shadow
                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.35f),
                        topLeft = Offset(3.dp.toPx(), 3.dp.toPx()),
                        size = Size(size.width, bodyH),
                        cornerRadius = cornerR
                    )
                    // Body fill
                    drawRoundRect(
                        color = balloonColor,
                        size = Size(size.width, bodyH),
                        cornerRadius = cornerR
                    )
                    // Body border
                    drawRoundRect(
                        color = borderColor,
                        size = Size(size.width, bodyH),
                        cornerRadius = cornerR,
                        style = Stroke(width = 1.dp.toPx())
                    )
                    // Cover only the portion of the bottom border where the arrow connects,
                    // so the rest of the bottom border remains visible.
                    drawRect(
                        color = balloonColor,
                        topLeft = Offset(cx - arrowW / 2f, bodyH - 1.dp.toPx()),
                        size = Size(arrowW, 1.dp.toPx() + 1f)
                    )
                    // Arrow fill (triangle pointing down)
                    val arrowFillPath = Path().apply {
                        moveTo(cx - arrowW / 2f, bodyH)
                        lineTo(cx + arrowW / 2f, bodyH)
                        lineTo(cx, bodyH + arrowH)
                        close()
                    }
                    drawPath(arrowFillPath, balloonColor)
                    // Arrow border (only the two slanted edges — no base line)
                    val arrowBorderPath = Path().apply {
                        moveTo(cx - arrowW / 2f, bodyH)
                        lineTo(cx, bodyH + arrowH)
                        lineTo(cx + arrowW / 2f, bodyH)
                    }
                    drawPath(
                        path = arrowBorderPath,
                        color = borderColor,
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                .padding(bottom = arrowHeightDp)
                .padding(10.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // ⏹️ Durdur button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF7F1D1D), Color(0xFFEF4444))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onStopClicked() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⏹️  Durdur",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                // 🏠 Şarj İstasyonuna Dön button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF92400E), Color(0xFFFBBF24))
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDockClicked() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏠  Şarj İstasyonuna Dön",
                        color = Color(0xFF1A0A00),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

