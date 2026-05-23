package com.vahitkeskin.loopsweep

import androidx.compose.animation.core.*
import androidx.compose.animation.*
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
import com.vahitkeskin.loopsweep.ui.components.GlassmorphicBottomNavigation
import com.vahitkeskin.loopsweep.ui.screen.vacuum.VacuumScreen
import com.vahitkeskin.loopsweep.ui.screen.cloud.XiaomiCloudScreen
import com.vahitkeskin.loopsweep.ui.screen.splash.SplashScreen
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
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Splash.route

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
                val isVisible = currentRoute != Screen.Splash.route
                AnimatedVisibility(
                    visible = isVisible,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(durationMillis = 800, easing = LinearOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(800)),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(durationMillis = 500)
                    ) + fadeOut(animationSpec = tween(500))
                ) {
                    GlassmorphicBottomNavigation(
                        currentRoute = currentRoute,
                        onRouteSelected = { route ->
                            navController.navigate(route) {
                                popUpTo(Screen.Dashboard.route) {
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
                }
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
                    startDestination = Screen.Splash.route
                ) {
                    // Cihaz ilk açıldığında gösterilen, marka logosu ve animasyonlu süpürme içeren giriş ekranı (Splash Screen)
                    composable(
                        route = Screen.Splash.route,
                        exitTransition = {
                            fadeOut(animationSpec = tween(800, easing = LinearOutSlowInEasing))
                        }
                    ) {
                        SplashScreen(
                            onSplashFinished = {
                                navController.navigate(Screen.Dashboard.route) {
                                    popUpTo(Screen.Splash.route) { inclusive = true }
                                }
                            }
                        )
                    }
                    // Süpürgenin radar haritası, oda kontrolleri, hızlı komutlar ve telemetri grafiklerini barındıran ana panel ekranı (Dashboard)
                    composable(
                        route = Screen.Dashboard.route,
                        enterTransition = {
                            fadeIn(animationSpec = tween(800, easing = LinearOutSlowInEasing))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(500, easing = LinearOutSlowInEasing))
                        }
                    ) {
                        VacuumScreen(viewModel = viewModel)
                    }
                    // Xiaomi Hesabına bağlanarak akıllı süpürge IP ve token bilgilerini otomatik alan bulut entegrasyon ekranı (Cloud)
                    composable(
                        route = Screen.Cloud.route,
                        enterTransition = {
                            fadeIn(animationSpec = tween(500, easing = LinearOutSlowInEasing))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(500, easing = LinearOutSlowInEasing))
                        }
                    ) {
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