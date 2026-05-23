package com.vahitkeskin.loopsweep.ui.components

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.atan2

import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry

@Composable
fun RealisticRadarCard(
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    telemetry: VacuumTelemetry?,
    deviceStatusText: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(SlateNavy.copy(alpha = 0.35f)) // Glassmorphic background
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.12f),
                        Color.White.copy(alpha = 0.02f)
                    )
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        // Card Header / Trigger area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggleVisibility() }
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulse indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "LIDAR AKTİF RADAR TARAMASI",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    letterSpacing = 1.5.sp
                )
            }
            // Expand/Collapse text/arrow
            Text(
                text = if (isVisible) "RADARI GİZLE ▲" else "RADARI GÖSTER ▼",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen,
                letterSpacing = 1.sp
            )
        }

        // Radar Display Area (Animated Visibility)
        AnimatedVisibility(
            visible = isVisible,
            enter = expandVertically(animationSpec = tween(500, easing = FastOutSlowInEasing)),
            exit = shrinkVertically(animationSpec = tween(500, easing = FastOutSlowInEasing))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Spacer(modifier = Modifier.height(16.dp))
                RealisticRadarDisplay(telemetry, deviceStatusText)
            }
        }
    }
}

@Composable
fun RealisticRadarDisplay(
    telemetry: VacuumTelemetry?,
    deviceStatusText: String
) {
    val infiniteTransition = rememberInfiniteTransition()

    val isActive = telemetry?.statusCode in listOf(5, 6, 7)
    val sweepDuration = if (isActive) 2800 else 10000

    // 1. Rotation Angle for the sweep line
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(sweepDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 2. Pulse distance for Sonar wave
    val sonarPulseProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    // 3. Targets (Obstacles) defined by relative X/Y coordinate multipliers from center
    // Let's place 5 distinct targets representing walls/objects
    val targets = remember {
        listOf(
            RadarTarget(dx = -0.5f, dy = -0.3f, size = 6f, desc = "ENGEL-A"),
            RadarTarget(dx = 0.6f, dy = 0.5f, size = 5f, desc = "DUVAR-S1"),
            RadarTarget(dx = 0.7f, dy = -0.4f, size = 8f, desc = "MASA-AYAK"),
            RadarTarget(dx = -0.3f, dy = 0.6f, size = 7f, desc = "KOLTUK"),
            RadarTarget(dx = 0.1f, dy = -0.7f, size = 5f, desc = "KAPI-E")
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DeepNavyDark) // Deep space radar scope background
            .border(1.dp, SlateBlueGray, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2f)
            val maxRadius = minOf(width, height) * 0.46f

            // --- A. BACKGROUND GRID LINES (Tactical scope grid) ---
            val cellCount = 14
            val cellWidth = width / cellCount
            val cellHeight = height / cellCount
            for (i in 1 until cellCount) {
                // Vertical grid lines
                drawLine(
                    color = DarkGray.copy(alpha = 0.4f),
                    start = Offset(i * cellWidth, 0f),
                    end = Offset(i * cellWidth, height),
                    strokeWidth = 1f
                )
                // Horizontal grid lines
                drawLine(
                    color = DarkGray.copy(alpha = 0.4f),
                    start = Offset(0f, i * cellHeight),
                    end = Offset(width, i * cellHeight),
                    strokeWidth = 1f
                )
            }

            // --- B. CONCENTRIC RANGE RINGS ---
            val ringColors = listOf(0xFF10B981, 0xFF10B981, 0xFF10B981, 0xFF059669)
            val ringRatios = listOf(1.0f, 0.75f, 0.5f, 0.25f)
            ringRatios.forEachIndexed { idx, ratio ->
                val ringRadius = maxRadius * ratio
                drawCircle(
                    color = Color(ringColors.getOrElse(idx) { 0xFF10B981 }).copy(alpha = 0.15f),
                    center = center,
                    radius = ringRadius,
                    style = Stroke(
                        width = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 8f), 0f)
                    )
                )
            }

            // --- C. RADIAL AXES / CROSSHAIRS (Azimuth Axes) ---
            val axesAngles = listOf(0.0, 45.0, 90.0, 135.0, 180.0, 225.0, 270.0, 315.0)
            axesAngles.forEach { angle ->
                val rad = angle * kotlin.math.PI / 180.0
                val start = center
                val end = Offset(
                    center.x + maxRadius * cos(rad).toFloat(),
                    center.y + maxRadius * sin(rad).toFloat()
                )
                drawLine(
                    color = EmeraldGreen.copy(alpha = 0.08f),
                    start = start,
                    end = end,
                    strokeWidth = 1f
                )
            }

            // --- D. OUTER ANGLE INDICATORS & TICK MARKS ---
            for (degree in 0 until 360 step 10) {
                val rad = degree * kotlin.math.PI / 180.0
                val tickLength = if (degree % 30 == 0) 10f else 5f
                val innerOffset = Offset(
                    center.x + (maxRadius + 2f) * cos(rad).toFloat(),
                    center.y + (maxRadius + 2f) * sin(rad).toFloat()
                )
                val outerOffset = Offset(
                    center.x + (maxRadius + tickLength + 2f) * cos(rad).toFloat(),
                    center.y + (maxRadius + tickLength + 2f) * sin(rad).toFloat()
                )
                drawLine(
                    color = EmeraldGreen.copy(alpha = if (degree % 30 == 0) 0.5f else 0.25f),
                    start = innerOffset,
                    end = outerOffset,
                    strokeWidth = if (degree % 30 == 0) 1.5f else 1f
                )
            }

            // --- E. SONAR EXPANDING PING (Sonar pulse wave) ---
            val sonarRadius = maxRadius * sonarPulseProgress
            val sonarAlpha = 0.25f * (1.0f - sonarPulseProgress)
            drawCircle(
                color = ThemeCyan.copy(alpha = sonarAlpha),
                center = center,
                radius = sonarRadius,
                style = Stroke(width = 2f)
            )

            // --- F. SWEEP TRAIL (Fading arc gradient behind sweep line) ---
            // Draw a sweep arc trailing behind the sweep angle
            // SweepAngle goes 0 to 360. We trail backwards.
            val sweepTrailPath = Path().apply {
                moveTo(center.x, center.y)
                arcTo(
                    rect = androidx.compose.ui.geometry.Rect(
                        center.x - maxRadius,
                        center.y - maxRadius,
                        center.x + maxRadius,
                        center.y + maxRadius
                    ),
                    startAngleDegrees = sweepAngle - 45f,
                    sweepAngleDegrees = 45f,
                    forceMoveTo = false
                )
                close()
            }
            drawPath(
                path = sweepTrailPath,
                brush = Brush.sweepGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.75f to EmeraldGreen.copy(alpha = 0.01f),
                        0.95f to EmeraldGreen.copy(alpha = 0.15f),
                        1f to EmeraldGreen.copy(alpha = 0.45f)
                    ),
                    center = center
                )
            )

            // --- G. SWEEP RADAR LINE ---
            val sweepRad = sweepAngle * kotlin.math.PI / 180.0
            val lineEnd = Offset(
                center.x + maxRadius * cos(sweepRad).toFloat(),
                center.y + maxRadius * sin(sweepRad).toFloat()
            )
            // Sweep line glow
            drawLine(
                color = EmeraldGreen.copy(alpha = 0.8f),
                start = center,
                end = lineEnd,
                strokeWidth = 2.5f
            )
            // Center hub dot
            drawCircle(
                color = EmeraldGreen,
                center = center,
                radius = 4f
            )
            drawCircle(
                color = EmeraldGreen.copy(alpha = 0.3f),
                center = center,
                radius = 8f,
                style = Stroke(width = 1.5f)
            )

            // --- H. MATH-BASED BLIP FADE DECAY (High mathematics integration) ---
            targets.forEach { target ->
                val targetPos = Offset(
                    center.x + target.dx * maxRadius,
                    center.y + target.dy * maxRadius
                )

                // Calculate angle of this target relative to center
                val dx = targetPos.x - center.x
                val dy = targetPos.y - center.y
                
                // atan2 returns [-PI, PI], map it to [0, 2*PI] in radians, then convert to degrees
                var targetAngle = atan2(dy.toDouble(), dx.toDouble()) * 180.0 / kotlin.math.PI
                if (targetAngle < 0) targetAngle += 360.0

                // Distance in degrees from the sweep line.
                // The sweep moves clockwise (sweepAngle increases).
                // Subtract target angle from sweep angle and wrap to [0, 360).
                val angleDiff = (sweepAngle - targetAngle.toFloat() + 360f) % 360f

                // Exponential decay: blip is fully lit at 0, decays to 0 as angleDiff grows.
                // Since the sweep takes 360deg to return, a decay lambda of 0.015f leaves
                // a nice trailing glow for about 120-150 degrees before fading to black.
                val blipAlpha = kotlin.math.exp(-angleDiff * 0.018f)

                if (isActive && blipAlpha > 0.02f) {
                    // 1. Draw glowing outer halo
                    drawCircle(
                        color = AlertRed.copy(alpha = blipAlpha * 0.2f),
                        center = targetPos,
                        radius = target.size * 2.2f
                    )
                    // 2. Draw blip center dot
                    drawCircle(
                        color = AlertRed.copy(alpha = blipAlpha),
                        center = targetPos,
                        radius = target.size * 0.7f
                    )

                    // 3. Pulse lock reticle for the nearest / brightest target
                    // Target Mas Ayak (dx = 0.7, dy = -0.4) represents the primary obstacle target lock
                    if (target.desc == "MASA-AYAK" && blipAlpha > 0.4f) {
                        val lockRatio = (1.0f - blipAlpha) * 15f
                        val bracketSize = target.size * 2f + lockRatio
                        
                        drawCircle(
                            color = ThemeCyan.copy(alpha = blipAlpha * 0.7f),
                            center = targetPos,
                            radius = target.size * 1.5f + lockRatio,
                            style = Stroke(width = 1f)
                        )
                        
                        // Tactical Rotating sci-fi corner brackets
                        rotate(degrees = sweepAngle * 0.7f, pivot = targetPos) {
                            val lineLen = 6.dp.toPx()
                            // Top-left
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x - bracketSize, targetPos.y - bracketSize),
                                end = Offset(targetPos.x - bracketSize + lineLen, targetPos.y - bracketSize),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x - bracketSize, targetPos.y - bracketSize),
                                end = Offset(targetPos.x - bracketSize, targetPos.y - bracketSize + lineLen),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Top-right
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x + bracketSize, targetPos.y - bracketSize),
                                end = Offset(targetPos.x + bracketSize - lineLen, targetPos.y - bracketSize),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x + bracketSize, targetPos.y - bracketSize),
                                end = Offset(targetPos.x + bracketSize, targetPos.y - bracketSize + lineLen),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Bottom-left
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x - bracketSize, targetPos.y + bracketSize),
                                end = Offset(targetPos.x - bracketSize + lineLen, targetPos.y + bracketSize),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x - bracketSize, targetPos.y + bracketSize),
                                end = Offset(targetPos.x - bracketSize, targetPos.y + bracketSize - lineLen),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            // Bottom-right
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x + bracketSize, targetPos.y + bracketSize),
                                end = Offset(targetPos.x + bracketSize - lineLen, targetPos.y + bracketSize),
                                strokeWidth = 1.5.dp.toPx()
                            )
                            drawLine(
                                color = ThemeCyan.copy(alpha = blipAlpha),
                                start = Offset(targetPos.x + bracketSize, targetPos.y + bracketSize),
                                end = Offset(targetPos.x + bracketSize, targetPos.y + bracketSize - lineLen),
                                strokeWidth = 1.5.dp.toPx()
                            )
                        }
                    }
                }
            }
        }

        // --- I. TACTICAL HUD OVERLAY (Cyberpunk-styled tech info) ---
        // Left Column HUD Info
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            HudText("SYS: KMP.LIDAR.v2.3")
            val sweepRPM = if (isActive) "24.0 RPM" else "0.0 RPM (STANDBY)"
            HudText("SWEEP SPEED: $sweepRPM")
            val statusLabel = if (deviceStatusText.isNotEmpty()) deviceStatusText.uppercase() else "OFFLINE"
            HudText("SYS STATE: $statusLabel")
        }

        // Right Column HUD Info
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            horizontalAlignment = Alignment.End
        ) {
            val displayAngle = sweepAngle.toInt().toString().padStart(3, '0')
            HudText("AZIMUTH: $displayAngle°")
            val batteryLevel = telemetry?.batteryLevel ?: 0
            HudText("BATTERY: $batteryLevel%")
            val targetsCount = if (isActive) "5 ACTIVE" else "0 STANDBY"
            val targetsColor = if (isActive) AlertRed else EmeraldGreen.copy(alpha = 0.8f)
            HudText("OBSTACLES: $targetsCount", color = targetsColor)
        }

        // Bottom Overlay status
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(10.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            val bottomStatusText = when (telemetry?.statusCode) {
                5, 6, 7 -> "SWEEP SCANNING SYSTEM STATE: ACTIVE SCANNING..."
                4 -> "SWEEP SCANNING SYSTEM STATE: CHARGING // STANDBY"
                else -> "SWEEP SCANNING SYSTEM STATE: DOCKED // SECURE"
            }
            Text(
                text = bottomStatusText,
                color = EmeraldGreen.copy(alpha = 0.7f),
                fontSize = 9.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HudText(text: String, color: Color = EmeraldGreen.copy(alpha = 0.8f)) {
    Text(
        text = text,
        color = color,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(bottom = 2.dp)
    )
}

data class RadarTarget(
    val dx: Float,
    val dy: Float,
    val size: Float,
    val desc: String
)
