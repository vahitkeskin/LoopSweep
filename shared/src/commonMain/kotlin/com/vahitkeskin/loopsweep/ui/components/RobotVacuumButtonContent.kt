package com.vahitkeskin.loopsweep.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.dp
import com.vahitkeskin.loopsweep.utils.LoopSweepPreview

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
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )
    } else {
        remember { mutableStateOf(0f) }
    }

    // Arm flex animation
    val armFlex by if (isCleaning) {
        infiniteTransition.animateFloat(
            initialValue = -0.2f,
            targetValue = -0.6f,
            animationSpec = infiniteRepeatable(
                animation = tween(200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        )
    } else {
        remember { mutableStateOf(-0.4f) }
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
                val armLen = 10.dp.toPx()
                val armEndX = brushHubX + armLen * kotlin.math.cos(angleRad)
                val armEndY = brushHubY + armLen * kotlin.math.sin(angleRad)
                
                // Curved arm shaft
                val path = Path().apply {
                    moveTo(brushHubX, brushHubY)
                    // quadratic bezier for curved arm (swirl effect)
                    val cpX = brushHubX + armLen * 0.6f * kotlin.math.cos(angleRad + armFlex)
                    val cpY = brushHubY + armLen * 0.6f * kotlin.math.sin(angleRad + armFlex)
                    quadraticBezierTo(cpX, cpY, armEndX, armEndY)
                }
                drawPath(
                    path = path,
                    color = Color(0xFF111111),
                    style = Stroke(width = 1.5f.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                )
                
                // Bristles fanning out
                for (j in -4..4) {
                    val brSpread = if (isCleaning) 0.12f else 0.08f
                    val brAngle = angleRad + j * brSpread + (armFlex + 0.4f) * 0.5f
                    val brLen = 4.dp.toPx()
                    val brEndX = armEndX + brLen * kotlin.math.cos(brAngle)
                    val brEndY = armEndY + brLen * kotlin.math.sin(brAngle)
                    drawLine(
                        color = Color(0xFF555555),
                        start = Offset(armEndX, armEndY),
                        end = Offset(brEndX, brEndY),
                        strokeWidth = 0.5f.dp.toPx(),
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )
                }
            }
            // Draw central hub
            drawCircle(
                color = Color(0xFF111111),
                radius = 1.5f.dp.toPx(),
                center = Offset(brushHubX, brushHubY)
            )
            // Draw yellow screw
            drawCircle(
                color = Color(0xFFE6C200),
                radius = 0.5f.dp.toPx(),
                center = Offset(brushHubX, brushHubY)
            )
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
        val btnPillW = capsuleW * 0.32f
        val btnPillH = capsuleH * 0.22f
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
        val pIconR = 1.0.dp.toPx()
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
            style = Stroke(width = 0.4.dp.toPx())
        )
        drawLine(
            color = powerColor,
            start = Offset(cx, powerY - pIconR * 0.9f),
            end = Offset(cx, powerY + pIconR * 0.1f),
            strokeWidth = 0.4.dp.toPx()
        )

        // Home Icon (bottom of button pill)
        val homeY = btnPillTop + btnPillH * 0.7f
        val homeW = 2.2f.dp.toPx()
        val homeH = 1.8f.dp.toPx()
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
            style = Stroke(width = 0.4.dp.toPx())
        )

        // --- DRAW LIDAR TURRET (Bottom of capsule) ---
        val lidarW = capsuleW * 0.4f
        val lidarH = capsuleW * 0.4f
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

@LoopSweepPreview
@Composable
fun RobotVacuumButtonContentPreview() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color(0xFF0A0F1D)),
        contentAlignment = Alignment.Center
    ) {
        RobotVacuumButtonContent(
            isCleaning = true,
            isCharging = false
        )
    }
}
