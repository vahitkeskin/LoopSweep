package com.vahitkeskin.loopsweep.ui.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.ui.theme.SpaceDarkBg
import com.vahitkeskin.loopsweep.utils.LoopSweepPreview
import kotlinx.coroutines.delay
import loopsweep.shared.generated.resources.Res
import loopsweep.shared.generated.resources.robot_body
import loopsweep.shared.generated.resources.robot_brush
import org.jetbrains.compose.resources.painterResource

// Premium dark hardwood floor colors
private val PlankColor1 = Color(0xFF2D1B15)
private val PlankColor2 = Color(0xFF38231C)
private val LineColor = Color(0xFF1A0F0C)

@Composable
fun SplashScreen(onSplashFinished: () -> Unit, isPreview: Boolean = false) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(4000)
        onSplashFinished()
    }

    // Floor and text appearance animations
    val transition = updateTransition(targetState = startAnimation, label = "SplashTransition")

    // Logo fade in after the vacuum passes
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(800, delayMillis = 2000, easing = LinearEasing) },
        label = "logoAlpha"
    ) { if (it || isPreview) 1f else 0f }

    // Subtitle fade in
    val subtitleAlpha by transition.animateFloat(
        transitionSpec = { tween(800, delayMillis = 2400, easing = LinearEasing) },
        label = "subtitleAlpha"
    ) { if (it || isPreview) 1f else 0f }

    // Vacuum movement (0f to 1f over 3.5 seconds)
    val vacuumProgress by transition.animateFloat(
        transitionSpec = { tween(3500, easing = LinearEasing) },
        label = "vacuumProgress"
    ) { if (it) 1f else 0f }

    // Infinite rotation for the brush
    val infiniteTransition = rememberInfiniteTransition()
    val brushAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceDarkBg)
    ) {
        val widthPx = constraints.maxWidth.toFloat()
        val heightPx = constraints.maxHeight.toFloat()
        val vacuumSize = 140.dp

        // Draw Hardwood Floor (Parke)
        Canvas(modifier = Modifier.fillMaxSize()) {
            val plankHeight = 50.dp.toPx()
            val plankWidth = 150.dp.toPx()
            val columns = (size.width / plankWidth).toInt() + 2
            val rows = (size.height / plankHeight).toInt() + 2

            for (row in 0 until rows) {
                val y = row * plankHeight
                // Stagger every other row
                val offsetX = if (row % 2 == 0) 0f else -plankWidth / 2f
                for (col in 0 until columns) {
                    val x = col * plankWidth + offsetX
                    val color = if ((row + col) % 2 == 0) PlankColor1 else PlankColor2

                    drawRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(plankWidth, plankHeight)
                    )
                    // Plank borders
                    drawRect(
                        color = LineColor,
                        topLeft = Offset(x, y),
                        size = Size(plankWidth, plankHeight),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
            }
        }

        // Branding
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "LoopSweep",
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 2.sp,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha }
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Next-Gen Vacuum Controller",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
                modifier = Modifier.graphicsLayer { alpha = subtitleAlpha }
            )
        }

        // Vacuum moving from bottom to top in the center
        val startY = heightPx + 200f // Off-screen bottom
        val endY = -300f // Off-screen top

        val currentY = if (isPreview) {
            with(androidx.compose.ui.platform.LocalDensity.current) {
                (heightPx / 4f) - (vacuumSize.toPx() / 2f)
            }
        } else {
            startY + (endY - startY) * vacuumProgress
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(
                    y = with(androidx.compose.ui.platform.LocalDensity.current) { currentY.toDp() }
                )
                .size(vacuumSize),
            contentAlignment = Alignment.Center
        ) {
            // Shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(x = 10.dp, y = 20.dp)
                    .background(
                        Color.Black.copy(alpha = 0.4f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )

            // Draw the Side Brush
            val brushSize = vacuumSize * 0.39f
            val brushOffsetX = vacuumSize * 0.33f
            val brushOffsetY = -(vacuumSize * 0.353f)

            Image(
                painter = painterResource(Res.drawable.robot_brush),
                contentDescription = "Side Brush",
                modifier = Modifier
                    .size(brushSize)
                    .offset(x = brushOffsetX, y = brushOffsetY)
                    .rotate(brushAngle)
            )

            // Draw the Robot Body
            Image(
                painter = painterResource(Res.drawable.robot_body),
                contentDescription = "Robot Body",
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@LoopSweepPreview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onSplashFinished = {}, isPreview = true)
}
