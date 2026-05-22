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
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.ui.theme.EmeraldGreen
import com.vahitkeskin.loopsweep.ui.theme.SpaceDarkBg
import com.vahitkeskin.loopsweep.ui.theme.PlankColor1
import com.vahitkeskin.loopsweep.ui.theme.PlankColor2
import com.vahitkeskin.loopsweep.ui.theme.PlankLineColor
import com.vahitkeskin.loopsweep.ui.theme.DustDarkGray
import com.vahitkeskin.loopsweep.ui.theme.DustMediumGray
import com.vahitkeskin.loopsweep.ui.components.BrandingCard
import com.vahitkeskin.loopsweep.utils.LoopSweepPreview
import kotlinx.coroutines.delay
import loopsweep.shared.generated.resources.Res
import loopsweep.shared.generated.resources.robot_body
import loopsweep.shared.generated.resources.robot_brush
import org.jetbrains.compose.resources.painterResource

/**
 * Data class representing a single dust/dirt particle on the hardwood floor.
 */
data class DustParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val color: Color,
    val type: Int,      // 0: Speck (circle), 1: Crumb (square), 2: Lint (line)
    val angle: Float
)

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    isPreview: Boolean = false
) {
    var startAnimation by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(5800)
        onSplashFinished()
    }

    // Floor and text appearance animations
    val transition = updateTransition(targetState = startAnimation, label = "SplashTransition")

    // Card background and border fade in slowly after vacuum has completely gone (starts at 3200ms)
    val cardAlpha by transition.animateFloat(
        transitionSpec = { tween(1200, delayMillis = 3200, easing = FastOutSlowInEasing) },
        label = "cardAlpha"
    ) { if (it || isPreview) 1f else 0f }

    // Logo and title fade in shortly after card begins (starts at 3500ms)
    val logoAlpha by transition.animateFloat(
        transitionSpec = { tween(1000, delayMillis = 3500, easing = FastOutSlowInEasing) },
        label = "logoAlpha"
    ) { if (it || isPreview) 1f else 0f }

    // Subtitle fade in (starts at 3900ms)
    val subtitleAlpha by transition.animateFloat(
        transitionSpec = { tween(1000, delayMillis = 3900, easing = FastOutSlowInEasing) },
        label = "subtitleAlpha"
    ) { if (it || isPreview) 1f else 0f }

    // Vacuum movement (0f to 1f over 3.2 seconds)
    val vacuumProgress by transition.animateFloat(
        transitionSpec = { tween(3200, easing = LinearEasing) },
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

    // Power LED pulsing glow
    val powerLedAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // LiDAR sweep back and forth
    val lidarSweepAngle by infiniteTransition.animateFloat(
        initialValue = -35f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Card metallic shimmer sweep animation
    val shimmerTranslate by infiniteTransition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(3200, easing = LinearEasing),
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

        // Generate stable-seeded random dust particles scattered across the screen
        val dustParticles = remember(widthPx, heightPx) {
            if (widthPx <= 0f || heightPx <= 0f) {
                emptyList()
            } else {
                val random = kotlin.random.Random(1337) // Stable seed to persist dust coordinates
                List(800) {
                    val type = random.nextInt(3)
                    val x = random.nextFloat() * widthPx
                    val y = random.nextFloat() * heightPx
                    val size = 3f + random.nextFloat() * 4f
                    val color = when (random.nextInt(3)) {
                        0 -> Color.Black.copy(alpha = 0.85f)
                        1 -> DustDarkGray.copy(alpha = 0.75f)
                        else -> DustMediumGray.copy(alpha = 0.8f)
                    }
                    val angle = random.nextFloat() * 360f
                    DustParticle(x, y, size, color, type, angle)
                }
            }
        }

        // Vacuum coordinates calculation
        val startY = heightPx + 200f // Off-screen bottom
        val endY = -300f // Off-screen top

        val currentY = if (isPreview) {
            with(androidx.compose.ui.platform.LocalDensity.current) {
                (heightPx / 4f) - (vacuumSize.toPx() / 2f)
            }
        } else {
            startY + (endY - startY) * vacuumProgress
        }

        // 1. Background Content (Floor Canvas + Vacuum) which gets blurred dynamically as card appears
        Box(
            modifier = Modifier
                .fillMaxSize()
                .blur(radius = (cardAlpha * 16f).dp)
        ) {
            // Draw Hardwood Floor (Parke) and dynamic Dust
            Canvas(modifier = Modifier.fillMaxSize()) {
                val plankHeight = 50.dp.toPx()
                val plankWidth = 150.dp.toPx()
                val columns = (size.width / plankWidth).toInt() + 2
                val rows = (size.height / plankHeight).toInt() + 2

                // 1. Draw hardwood floor planks
                for (row in 0 until rows) {
                    val y = row * plankHeight
                    val offsetX = if (row % 2 == 0) 0f else -plankWidth / 2f
                    for (col in 0 until columns) {
                        val x = col * plankWidth + offsetX
                        val color = if ((row + col) % 2 == 0) PlankColor1 else PlankColor2

                        drawRect(
                            color = color,
                            topLeft = Offset(x, y),
                            size = Size(plankWidth, plankHeight)
                        )
                        drawRect(
                            color = PlankLineColor,
                            topLeft = Offset(x, y),
                            size = Size(plankWidth, plankHeight),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }

                // 2. Compute vacuum and side brush coordinates in pixels
                val vacuumSizePx = vacuumSize.toPx()
                val brushSizePx = vacuumSizePx * 0.39f
                val brushOffsetX = vacuumSizePx * 0.33f
                val brushOffsetY = -(vacuumSizePx * 0.353f)

                val cx = size.width / 2f
                val cy = currentY + (vacuumSizePx / 2f)

                val brushCx = cx + brushOffsetX
                val brushCy = cy + brushOffsetY

                val vacuumRadius = vacuumSizePx / 2f
                val brushRadius = brushSizePx / 2f

                // 3. Draw fresh polish/damp trail behind the vacuum (under the dust)
                if (cy < size.height) {
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.12f),
                                Color.White.copy(alpha = 0.04f),
                                Color.Transparent
                            ),
                            startY = cy,
                            endY = size.height
                        ),
                        topLeft = Offset(cx - vacuumRadius * 0.95f, cy),
                        size = Size(vacuumRadius * 1.9f, size.height - cy)
                    )
                }

                // 4. Draw soft neon control underglow beneath the vacuum
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldGreen.copy(alpha = 0.22f),
                            EmeraldGreen.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = vacuumRadius * 1.3f
                    ),
                    radius = vacuumRadius * 1.3f,
                    center = Offset(cx, cy)
                )

                // 5. Draw active LiDAR laser scanning cone ahead of the vacuum
                val lidarPath = Path().apply {
                    moveTo(cx, cy - vacuumRadius * 0.8f) // scanner turret center
                    val distance = 160.dp.toPx()
                    val leftAngleRad = (-90f + lidarSweepAngle - 18f) * kotlin.math.PI / 180.0
                    val rightAngleRad = (-90f + lidarSweepAngle + 18f) * kotlin.math.PI / 180.0

                    val lx = (cx + distance * kotlin.math.cos(leftAngleRad)).toFloat()
                    val ly =
                        (cy - vacuumRadius * 0.8f + distance * kotlin.math.sin(leftAngleRad)).toFloat()

                    val rx = (cx + distance * kotlin.math.cos(rightAngleRad)).toFloat()
                    val ry =
                        (cy - vacuumRadius * 0.8f + distance * kotlin.math.sin(rightAngleRad)).toFloat()

                    lineTo(lx, ly)
                    lineTo(rx, ry)
                    close()
                }
                drawPath(
                    path = lidarPath,
                    brush = Brush.radialGradient(
                        colors = listOf(
                            EmeraldGreen.copy(alpha = 0.25f),
                            EmeraldGreen.copy(alpha = 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy - vacuumRadius * 0.8f),
                        radius = 180.dp.toPx()
                    )
                )

                // 6. Draw dust particles with realistic suction physics & side brush sweeping rotation
                dustParticles.forEach { particle ->
                    val px = particle.x
                    val py = particle.y

                    // Path behind the vacuum: cleared if inside vertical width and below vacuum center
                    val inClearedPath = py > cy && kotlin.math.abs(px - cx) < (vacuumRadius * 0.95f)
                    if (inClearedPath) return@forEach

                    val dx = px - cx
                    val dy = py - cy
                    val distToVacuum = kotlin.math.hypot(dx, dy)

                    val bdx = px - brushCx
                    val bdy = py - brushCy
                    val distToBrush = kotlin.math.hypot(bdx, bdy)

                    if (distToVacuum < vacuumRadius) {
                        // Fully cleared under vacuum body
                        return@forEach
                    }
                    if (distToBrush < brushRadius) {
                        // Fully cleared under side brush
                        return@forEach
                    }

                    // Dust stays completely static and does not move before the robot comes on top of it.
                    val drawX = px
                    val drawY = py

                    val particleColor = particle.color
                    when (particle.type) {
                        0 -> { // Speck
                            drawCircle(
                                color = particleColor,
                                radius = particle.size.dp.toPx() / 2f,
                                center = Offset(drawX, drawY)
                            )
                        }

                        1 -> { // Crumb
                            val sizePx = particle.size.dp.toPx()
                            drawRect(
                                color = particleColor,
                                topLeft = Offset(drawX - sizePx / 2f, drawY - sizePx / 2f),
                                size = Size(sizePx, sizePx)
                            )
                        }

                        2 -> { // Lint
                            val sizePx = particle.size.dp.toPx()
                            val length = sizePx * 1.6f
                            val rad = particle.angle * kotlin.math.PI / 180.0
                            val endX = drawX + length * kotlin.math.cos(rad).toFloat()
                            val endY = drawY + length * kotlin.math.sin(rad).toFloat()
                            drawLine(
                                color = particleColor,
                                start = Offset(drawX, drawY),
                                end = Offset(endX, endY),
                                strokeWidth = 1.2.dp.toPx()
                            )
                        }
                    }
                }
            }

            // Vacuum overlay moving from bottom to top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(
                        y = with(androidx.compose.ui.platform.LocalDensity.current) { currentY.toDp() }
                    )
                    .size(vacuumSize),
                contentAlignment = Alignment.Center
            ) {
                // Drop Shadow
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(x = 10.dp, y = 20.dp)
                        .background(
                            Color.Black.copy(alpha = 0.4f),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                // Dynamic Side Brush
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

                // Robot Body
                Image(
                    painter = painterResource(Res.drawable.robot_body),
                    contentDescription = "Robot Body",
                    modifier = Modifier.fillMaxSize()
                )

                // Green Power status LED glow
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = w / 2f
                    val cy = h / 2f
                    val r = minOf(w, h) / 2f

                    drawCircle(
                        color = EmeraldGreen.copy(alpha = 0.6f * powerLedAlpha),
                        radius = 3.dp.toPx(),
                        center = Offset(cx, cy - r * 0.58f)
                    )
                }
            }
        }

        // 2. Technical Glassmorphic Branding Card
        BrandingCard(
            cardAlpha = cardAlpha,
            logoAlpha = logoAlpha,
            subtitleAlpha = subtitleAlpha,
            shimmerTranslate = shimmerTranslate,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

@LoopSweepPreview
@Composable
fun SplashScreenPreview() {
    SplashScreen(onSplashFinished = {}, isPreview = true)
}