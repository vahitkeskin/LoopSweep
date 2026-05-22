package com.vahitkeskin.loopsweep

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.vahitkeskin.loopsweep.navigation.Screen
import com.vahitkeskin.loopsweep.ui.components.BottomNavItem
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.vahitkeskin.loopsweep.di.AppContainer
import com.vahitkeskin.loopsweep.presentation.VacuumViewModel
import com.vahitkeskin.loopsweep.presentation.XiaomiCloudViewModel
import com.vahitkeskin.loopsweep.ui.components.RobotVacuumButtonContent
import com.vahitkeskin.loopsweep.ui.screen.VacuumScreen
import com.vahitkeskin.loopsweep.ui.screen.XiaomiCloudScreen
import com.vahitkeskin.loopsweep.ui.theme.AlertRed
import com.vahitkeskin.loopsweep.ui.theme.AmberYellow
import com.vahitkeskin.loopsweep.ui.theme.BlueGray
import com.vahitkeskin.loopsweep.ui.theme.DarkBrown
import com.vahitkeskin.loopsweep.ui.theme.DarkNavy
import com.vahitkeskin.loopsweep.ui.theme.DarkRed
import com.vahitkeskin.loopsweep.ui.theme.DeepGreen
import com.vahitkeskin.loopsweep.ui.theme.DeepOrange
import com.vahitkeskin.loopsweep.ui.theme.EmeraldGreen
import com.vahitkeskin.loopsweep.ui.theme.SpaceDarkBg
import com.vahitkeskin.loopsweep.ui.theme.SystemGray

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

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Dashboard.route

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
            .background(SpaceDarkBg)
    ) {
        Scaffold(
            bottomBar = {
                GlassmorphicBottomNavigation(
                    currentRoute = currentRoute,
                    onRouteSelected = { route ->
                        navController.navigate(route) {
                            // TODO: Avoid multiple copies of the same destination when reselecting the same item
                            popUpTo(navController.graph.startDestinationRoute ?: Screen.Dashboard.route) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
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
                NavHost(
                    navController = navController,
                    startDestination = Screen.Dashboard.route
                ) {
                    composable(Screen.Dashboard.route) {
                        VacuumScreen(viewModel = viewModel)
                    }
                    composable(Screen.Cloud.route) {
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GlassmorphicBottomNavigation(
    currentRoute: String,
    onRouteSelected: (String) -> Unit,
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
                color = SpaceDarkBg
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
            val navItems = listOf(
                BottomNavItem.Dashboard,
                null, // Placeholder for the Middle "Start" button
                BottomNavItem.Cloud
            )

            navItems.forEach { item ->
                if (item != null) {
                    val isSelected = currentRoute == item.screen.route
                    val activeColor = if (isSelected) AmberYellow else SystemGray
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                indication = null,
                                onClick = { onRouteSelected(item.screen.route) }
                            )
                            .padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Box(
                            modifier = Modifier.height(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            item.icon(activeColor)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = activeColor
                        )
                    }
                } else {
                    // Middle Column (Start button label placeholder)
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
                            color = SystemGray
                        )
                    }
                }
            }
        }

        // 3. The Robot Vacuum Button - positioned absolutely over the cutout
        var showMenu by remember { mutableStateOf(false) }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 16.dp) // Slightly higher padding for visible gap from cutout
                .size(buttonSize)
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = { showMenu = !showMenu }
                ),
            contentAlignment = Alignment.Center
        ) {
            RobotVacuumButtonContent(
                isCleaning = isCleaning,
                isCharging = isCharging
            )

            // Click balloon popup
            val transition = updateTransition(targetState = showMenu, label = "BalloonTransition")
            val alpha by transition.animateFloat(
                transitionSpec = { tween(durationMillis = 150, easing = LinearOutSlowInEasing) },
                label = "alpha"
            ) { state ->
                if (state) 1f else 0f
            }
            val scale by transition.animateFloat(
                transitionSpec = { tween(durationMillis = 150, easing = LinearOutSlowInEasing) },
                label = "scale"
            ) { state ->
                if (state) 1f else 0.8f
            }

            if (showMenu || transition.currentState) {
                VacuumControlBalloon(
                    buttonSizePx = with(LocalDensity.current) { buttonSize.roundToPx() },
                    alpha = alpha,
                    scale = scale,
                    isCleaning = isCleaning,
                    onDismiss = { showMenu = false },
                    onCleanClicked = { showMenu = false; onCleanClicked() },
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
    alpha: Float,
    scale: Float,
    isCleaning: Boolean,
    onDismiss: () -> Unit,
    onCleanClicked: () -> Unit,
    onStopClicked: () -> Unit,
    onDockClicked: () -> Unit
) {
    val arrowHeightDp = 14.dp
    val balloonColor = DarkNavy
    val borderColor = BlueGray

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
                .graphicsLayer {
                    this.alpha = alpha
                    this.scaleX = scale
                    this.scaleY = scale
                    this.transformOrigin = TransformOrigin(0.5f, 1f)
                }
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
                if (isCleaning) {
                    // ⏹️ Durdur button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(DarkRed, AlertRed)
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
                } else {
                    // ▶️ Başlat button
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(DeepGreen, EmeraldGreen)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .clickable { onCleanClicked() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "▶️  Başlat",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                // 🏠 Şarj İstasyonuna Dön button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(DeepOrange, AmberYellow)
                            ),
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { onDockClicked() }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "🏠  Şarj İstasyonuna Dön",
                        color = DarkBrown,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}