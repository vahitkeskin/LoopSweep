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
                    text = "Scan",
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

            Text(
                text = "LOOP SWEEP",
                fontSize = 5.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.5f),
                letterSpacing = 0.5.sp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 9.dp)
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
                    // Cover the full bottom border line so the triangle base has no horizontal line.
                    // The corners are 14dp from edge so starting at 0f is safe (corner curves end there).
                    drawRect(
                        color = balloonColor,
                        topLeft = Offset(0f, bodyH - 1.dp.toPx()),
                        size = Size(size.width, 1.dp.toPx() + 1f)
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

@Composable
fun RobotVacuumButtonContent(
    isCleaning: Boolean,
    isCharging: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition()

    // Side brush rotation angle
    val brushAngle by if (isCleaning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(1200, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // LiDAR turret rotation angle
    val lidarAngle by if (isCleaning) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Power LED pulsing glow
    val powerLedAlpha by if (isCleaning) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(0.7f) }
    }

    // Charge LED pulsing glow
    val chargeLedAlpha by if (isCharging) {
        infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1.0f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(0.7f) }
    }

    // Pre-generate horizontal brushed lines for titanium metal look
    val brushedLines = remember {
        val lines = mutableListOf<Pair<Float, Color>>()
        val random = kotlin.random.Random(1337)
        var y = -1.0f
        val step = 0.012f // fine normalized step for thin lines
        while (y <= 1.0f) {
            if (random.nextFloat() > 0.45f) {
                val alpha = random.nextFloat() * 0.06f + 0.01f
                val isDark = random.nextBoolean()
                val color = if (isDark) Color(0xFF000000) else Color(0xFFFFFFFF)
                lines.add(Pair(y, color.copy(alpha = alpha)))
            }
            y += step
        }
        lines
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val cx = w / 2f
        val cy = h / 2f
        val r = minOf(w, h) / 2f - 4.dp.toPx() // Keep a small margin for side brush to extend outside the main body!

        // --- DRAW SIDE BRUSH UNDER THE BODY ---
        val brushHubX = cx + r * 0.76f
        val brushHubY = cy - r * 0.52f
        
        rotate(degrees = brushAngle, pivot = Offset(brushHubX, brushHubY)) {
            for (i in 0 until 3) {
                val angleRad = (i * 120f * 3.14159f / 180f)
                val armLen = 12.dp.toPx()
                val armEndX = brushHubX + armLen * kotlin.math.cos(angleRad)
                val armEndY = brushHubY + armLen * kotlin.math.sin(angleRad)
                
                // Draw arm shaft
                drawLine(
                    color = Color(0xFF141517),
                    start = Offset(brushHubX, brushHubY),
                    end = Offset(armEndX, armEndY),
                    strokeWidth = 2.dp.toPx()
                )
                
                // Bristles fanning out
                for (j in -1..1) {
                    val brAngle = angleRad + j * 0.22f
                    val brLen = 5.dp.toPx()
                    val brEndX = armEndX + brLen * kotlin.math.cos(brAngle)
                    val brEndY = armEndY + brLen * kotlin.math.sin(brAngle)
                    drawLine(
                        color = Color(0xFF6B7280),
                        start = Offset(armEndX, armEndY),
                        end = Offset(brEndX, brEndY),
                        strokeWidth = 1.dp.toPx()
                    )
                }
            }
        }

        // --- DRAW MAIN BODY ---
        // 1. Outer dark bumper shadow/edge
        drawCircle(
            color = Color(0xFF111315),
            radius = r + 1.dp.toPx(),
            center = Offset(cx, cy)
        )

        // 2. Base plate dark metal base with gradient
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFF32353A), Color(0xFF1A1C1F)),
                center = Offset(cx, cy),
                radius = r
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // 3. Horizontal Brushed Lines
        brushedLines.forEach { (normY, color) ->
            val dy = normY * r
            val rSq = r * r - dy * dy
            if (rSq > 0) {
                val halfW = kotlin.math.sqrt(rSq)
                val ly = cy + dy
                drawLine(
                    color = color,
                    start = Offset(cx - halfW, ly),
                    end = Offset(cx + halfW, ly),
                    strokeWidth = 1f
                )
            }
        }

        // 4. Subtle overall light highlight arc across the body (left to right)
        drawCircle(
            brush = Brush.linearGradient(
                colors = listOf(Color.White.copy(alpha = 0.15f), Color.Transparent),
                start = Offset(cx - r, cy - r),
                end = Offset(cx + r, cy + r)
            ),
            radius = r,
            center = Offset(cx, cy)
        )

        // 5. Outer edge bevel/groove
        drawCircle(
            color = Color(0xFF111315),
            radius = r * 0.97f,
            center = Offset(cx, cy),
            style = Stroke(width = 1.dp.toPx())
        )

        // 6. Split lines on the bumper (left & right)
        val splitLen = r * 0.05f
        drawLine(
            color = Color(0xFF111315),
            start = Offset(cx - r, cy),
            end = Offset(cx - r + splitLen, cy),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            color = Color(0xFF111315),
            start = Offset(cx + r - splitLen, cy),
            end = Offset(cx + r, cy),
            strokeWidth = 1.5.dp.toPx()
        )

        // --- DRAW TOP-CENTER CAPSULE (Control Panel) ---
        val capsuleW = r * 0.44f
        val capsuleH = r * 0.88f
        val capsuleLeft = cx - capsuleW / 2f
        val capsuleTop = cy - r * 0.72f
        val capsuleRadius = capsuleW / 2f

        // Draw capsule background (matte plastic look)
        drawRoundRect(
            color = Color(0xFF1B1D20),
            topLeft = Offset(capsuleLeft, capsuleTop),
            size = Size(capsuleW, capsuleH),
            cornerRadius = CornerRadius(capsuleRadius, capsuleRadius)
        )
        // Capsule border
        drawRoundRect(
            color = Color(0xFF0F1113),
            topLeft = Offset(capsuleLeft, capsuleTop),
            size = Size(capsuleW, capsuleH),
            cornerRadius = CornerRadius(capsuleRadius, capsuleRadius),
            style = Stroke(width = 1.dp.toPx())
        )

        // --- DRAW BUTTON PILL (Power & Home) ---
        val btnPillW = capsuleW * 0.64f
        val btnPillH = capsuleH * 0.42f
        val btnPillLeft = cx - btnPillW / 2f
        val btnPillTop = capsuleTop + capsuleH * 0.08f
        val btnPillRadius = btnPillW / 2f

        // Button Pill base
        drawRoundRect(
            color = Color(0xFF101214),
            topLeft = Offset(btnPillLeft, btnPillTop),
            size = Size(btnPillW, btnPillH),
            cornerRadius = CornerRadius(btnPillRadius, btnPillRadius)
        )
        // Button Pill chrome/grey border
        drawRoundRect(
            color = Color(0xFF3F444D),
            topLeft = Offset(btnPillLeft, btnPillTop),
            size = Size(btnPillW, btnPillH),
            cornerRadius = CornerRadius(btnPillRadius, btnPillRadius),
            style = Stroke(width = 0.8.dp.toPx())
        )

        // Power Icon (top of button pill)
        val powerY = btnPillTop + btnPillH * 0.3f
        val pIconR = 3.2.dp.toPx()
        val powerColor = if (isCleaning) Color(0xFF10B981) else Color(0xFFD1D5DB)
        
        if (isCleaning) {
            // green glow animation
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.2f * powerLedAlpha),
                radius = pIconR * 2.2f,
                center = Offset(cx, powerY)
            )
        }
        drawArc(
            color = powerColor,
            startAngle = -60f,
            sweepAngle = 300f,
            useCenter = false,
            topLeft = Offset(cx - pIconR, powerY - pIconR),
            size = Size(pIconR * 2, pIconR * 2),
            style = Stroke(width = 1.2.dp.toPx())
        )
        drawLine(
            color = powerColor,
            start = Offset(cx, powerY - pIconR * 0.9f),
            end = Offset(cx, powerY + pIconR * 0.1f),
            strokeWidth = 1.2.dp.toPx()
        )

        // Home Icon (bottom of button pill)
        val homeY = btnPillTop + btnPillH * 0.7f
        val homeW = 6.dp.toPx()
        val homeH = 5.dp.toPx()
        val homeColor = if (isCharging) Color(0xFFFBBF24) else Color(0xFFD1D5DB)

        if (isCharging) {
            // gold/yellow glow animation
            drawCircle(
                color = Color(0xFFFBBF24).copy(alpha = 0.2f * chargeLedAlpha),
                radius = homeW * 1.5f,
                center = Offset(cx, homeY)
            )
        }
        val pathHouse = Path().apply {
            moveTo(cx, homeY - homeH * 0.6f)
            lineTo(cx - homeW * 0.5f, homeY - homeH * 0.1f)
            lineTo(cx - homeW * 0.5f, homeY + homeH * 0.5f)
            lineTo(cx - homeW * 0.15f, homeY + homeH * 0.5f)
            lineTo(cx - homeW * 0.15f, homeY + homeH * 0.15f)
            lineTo(cx + homeW * 0.15f, homeY + homeH * 0.15f)
            lineTo(cx + homeW * 0.15f, homeY + homeH * 0.5f)
            lineTo(cx + homeW * 0.5f, homeY + homeH * 0.5f)
            lineTo(cx + homeW * 0.5f, homeY - homeH * 0.1f)
            close()
        }
        drawPath(
            path = pathHouse,
            color = homeColor,
            style = Stroke(width = 1.2.dp.toPx())
        )

        // --- DRAW LIDAR TURRET (Bottom of capsule) ---
        val lidarW = capsuleW * 0.82f
        val lidarH = capsuleW * 0.82f
        val lidarCR = lidarW * 0.3f
        val lidarY = capsuleTop + capsuleH * 0.7f

        // 1. Chrome Bezel
        val bezelW = lidarW + 1.6.dp.toPx()
        val bezelH = lidarH + 1.6.dp.toPx()
        val chromeBrush = Brush.linearGradient(
            colors = listOf(
                Color(0xFFE5E7EB),
                Color(0xFF9CA3AF),
                Color(0xFF374151),
                Color(0xFFF3F4F6),
                Color(0xFF1F2937),
                Color(0xFFD1D5DB)
            ),
            start = Offset(cx - bezelW/2, lidarY - bezelH/2),
            end = Offset(cx + bezelW/2, lidarY + bezelH/2)
        )
        drawRoundRect(
            brush = chromeBrush,
            topLeft = Offset(cx - bezelW/2, lidarY - bezelH/2),
            size = Size(bezelW, bezelH),
            cornerRadius = CornerRadius(lidarCR + 0.8.dp.toPx(), lidarCR + 0.8.dp.toPx())
        )

        // 2. Inner Black Face
        drawRoundRect(
            color = Color(0xFF141517),
            topLeft = Offset(cx - lidarW/2, lidarY - lidarH/2),
            size = Size(lidarW, lidarH),
            cornerRadius = CornerRadius(lidarCR, lidarCR)
        )

        // 3. Spinning Concentric Dial
        val dialR = lidarW * 0.42f
        val sweepColors = listOf(
            Color(0xFF4B5563),
            Color(0xFF1F2937),
            Color(0xFF9CA3AF),
            Color(0xFF111827),
            Color(0xFFE5E7EB),
            Color(0xFF1F2937),
            Color(0xFF4B5563)
        )
        rotate(degrees = lidarAngle, pivot = Offset(cx, lidarY)) {
            drawCircle(
                brush = Brush.sweepGradient(
                    colors = sweepColors,
                    center = Offset(cx, lidarY)
                ),
                radius = dialR,
                center = Offset(cx, lidarY)
            )
        }

        // Dial Chrome Border Groove
        drawCircle(
            color = Color(0xFF9CA3AF).copy(alpha = 0.5f),
            radius = dialR,
            center = Offset(cx, lidarY),
            style = Stroke(width = 0.5.dp.toPx())
        )
        drawCircle(
            color = Color.Black,
            radius = dialR + 0.5.dp.toPx(),
            center = Offset(cx, lidarY),
            style = Stroke(width = 1.dp.toPx())
        )
    }
}
