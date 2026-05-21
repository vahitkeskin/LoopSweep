package com.vahitkeskin.loopsweep.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun TelemetryDashboard(
    telemetry: VacuumTelemetry?,
    batteryHistory: List<Int>,
    areaHistory: List<Int>,
    eventLog: List<String>,
    distanceMeters: Double,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(0) } // 0: Harita & Telemetri, 1: Analiz Grafikleri, 2: Olay Günlüğü & Sarf Malzemeleri

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color.White.copy(alpha = 0.08f), Color.White.copy(alpha = 0.01f))
                ),
                RoundedCornerShape(24.dp)
            )
            .padding(16.dp)
    ) {
        // Dashboard Tab Selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val tabs = listOf("Harita & Radar", "Grafik Analiz", "Günlük & Sarf")
            tabs.forEachIndexed { index, title ->
                val selected = activeTab == index
                Button(
                    onClick = { activeTab = index },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selected) Color(0xFF6366F1) else Color.Transparent,
                        contentColor = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeTab) {
            0 -> TabMapAndTelemetry(telemetry, distanceMeters)
            1 -> TabCharts(batteryHistory, areaHistory)
            2 -> TabLogsAndConsumables(telemetry, eventLog)
        }
    }
}

@Composable
fun TabMapAndTelemetry(telemetry: VacuumTelemetry?, distanceMeters: Double) {
    val isCleaning = telemetry?.statusCode in listOf(5, 6, 7)

    // Telemetry Mini Metrics Grid
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard("Mesafe", "${distanceMeters.toString().take(5)} m", "🧭", Modifier.weight(1f))
        MetricCard("Çalışma Süresi", "${telemetry?.cleanTimeMinutes ?: 0} dk", "⏱️", Modifier.weight(1f))
        MetricCard("Alan", "${telemetry?.cleanAreaSqm ?: 0} m²", "📐", Modifier.weight(1f))
        MetricCard(
            "Emiş Gücü",
            when (telemetry?.suctionState) {
                0 -> "Sessiz"
                1 -> "Standart"
                2 -> "Orta"
                3 -> "Turbo"
                else -> "Otomatik"
            },
            "🌀",
            Modifier.weight(1.2f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Real-Time Canvas SLAM Map / Radar Sweep View
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070B16))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
    ) {
        // Simulated Radar Sweep Angle
        val infiniteTransition = rememberInfiniteTransition()
        val rotationAngle by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 360f,
            animationSpec = infiniteRepeatable(
                animation = tween(4000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        // Simulated clean path progress percentage
        val cleanPathAnimationPercent by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 100f,
            animationSpec = infiniteRepeatable(
                animation = tween(20000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            )
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f, height / 2f)

            // Draw grid lines
            val gridSize = 30f
            for (x in 0..(width / gridSize).toInt()) {
                val dx = x * gridSize
                drawLine(
                    color = Color(0xFF0F172A),
                    start = Offset(dx, 0f),
                    end = Offset(dx, height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(height / gridSize).toInt()) {
                val dy = y * gridSize
                drawLine(
                    color = Color(0xFF0F172A),
                    start = Offset(0f, dy),
                    end = Offset(width, dy),
                    strokeWidth = 1f
                )
            }

            // Draw concentric radar circles
            val maxRadius = minOf(width, height) * 0.45f
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.05f),
                center = center,
                radius = maxRadius,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.08f),
                center = center,
                radius = maxRadius * 0.66f,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = Color(0xFF10B981).copy(alpha = 0.1f),
                center = center,
                radius = maxRadius * 0.33f,
                style = Stroke(width = 1f)
            )

            // Draw Radar Sweep Line
            val rad = rotationAngle.toDouble() * kotlin.math.PI / 180.0
            val lineEnd = Offset(
                center.x + maxRadius * cos(rad).toFloat(),
                center.y + maxRadius * sin(rad).toFloat()
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF10B981).copy(alpha = 0.8f), Color.Transparent),
                    start = center,
                    end = lineEnd
                ),
                start = center,
                end = lineEnd,
                strokeWidth = 2.5f
            )

            // Draw Walls and Layout
            val wallPath = Path().apply {
                moveTo(center.x - 120f, center.y - 120f)
                lineTo(center.x + 120f, center.y - 120f)
                lineTo(center.x + 120f, center.y + 120f)
                lineTo(center.x - 120f, center.y + 120f)
                close()
                // Inner walls partitions
                moveTo(center.x - 120f, center.y)
                lineTo(center.x - 30f, center.y)
                moveTo(center.x + 30f, center.y)
                lineTo(center.x + 120f, center.y)
            }
            drawPath(
                path = wallPath,
                color = Color(0xFF06B6D4).copy(alpha = 0.3f),
                style = Stroke(width = 2.5f)
            )

            // Draw Obstacles (Red dots with glowing circles)
            val obstacles = listOf(
                Offset(center.x - 80f, center.y - 40f),
                Offset(center.x + 60f, center.y + 80f),
                Offset(center.x + 90f, center.y - 70f)
            )
            obstacles.forEach { obstacle ->
                drawCircle(
                    color = Color(0xFFEF4444).copy(alpha = 0.15f),
                    center = obstacle,
                    radius = 12f
                )
                drawCircle(
                    color = Color(0xFFEF4444),
                    center = obstacle,
                    radius = 4f
                )
            }

            // Draw simulated trajectory cleaning path line
            val points = listOf(
                Offset(center.x - 100f, center.y - 100f),
                Offset(center.x - 100f, center.y - 80f),
                Offset(center.x - 80f, center.y - 80f),
                Offset(center.x - 80f, center.y - 45f), // Obstacle bypass point
                Offset(center.x - 60f, center.y - 80f),
                Offset(center.x - 20f, center.y - 80f),
                Offset(center.x - 20f, center.y + 20f),
                Offset(center.x + 20f, center.y + 20f),
                Offset(center.x + 40f, center.y - 20f),
                Offset(center.x + 80f, center.y - 20f)
            )

            if (isCleaning || distanceMeters > 0) {
                val pathToShowCount = ((cleanPathAnimationPercent / 100f) * points.size).toInt().coerceIn(1, points.size)
                val cleanPath = Path()
                cleanPath.moveTo(points[0].x, points[0].y)
                for (i in 1 until pathToShowCount) {
                    cleanPath.lineTo(points[i].x, points[i].y)
                }

                // Draw path line in amber
                drawPath(
                    path = cleanPath,
                    color = Color(0xFFF59E0B).copy(alpha = 0.6f),
                    style = Stroke(width = 3f)
                )

                // Draw current robot position (vacuum indicator)
                val robotPos = points[pathToShowCount - 1]
                drawCircle(
                    color = Color(0xFFF59E0B).copy(alpha = 0.25f),
                    center = robotPos,
                    radius = 15f
                )
                drawCircle(
                    color = Color(0xFFF59E0B),
                    center = robotPos,
                    radius = 6f
                )
            } else {
                // Idle position at dock (center-bottom)
                val dockPos = Offset(center.x, center.y + 110f)
                drawCircle(
                    color = Color(0xFF3B82F6).copy(alpha = 0.3f),
                    center = dockPos,
                    radius = 12f
                )
                drawCircle(
                    color = Color(0xFF3B82F6),
                    center = dockPos,
                    radius = 5f
                )
            }
        }

        // Overlay status text on Canvas top-left
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(12.dp)
        ) {
            Text(
                text = "SLAM RADAR HARİTASI",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = if (isCleaning) "🔴 CANLI TARAMA AKTİF" else "⚪ HARİTA BEKLEMEDE",
                color = if (isCleaning) Color(0xFFEF4444) else Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Radar coordinate stats at bottom-right
        Text(
            text = "LDS v2.0 // Grid: 0.5m",
            color = Color.White.copy(alpha = 0.3f),
            fontSize = 9.sp,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
        )
    }
}

@Composable
fun TabCharts(batteryHistory: List<Int>, areaHistory: List<Int>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Line Chart: Battery Level Over Time
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "⚡ PİL TÜKETİM GRAFİĞİ (%)",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                if (batteryHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Veri bekleniyor...", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val pointsCount = batteryHistory.size
                        val stepX = w / (if (pointsCount > 1) pointsCount - 1 else 1)

                        // Draw Grid lines
                        for (i in 1..3) {
                            val y = h * (i / 4f)
                            drawLine(Color.White.copy(alpha = 0.05f), Offset(0f, y), Offset(w, y), 1f)
                        }

                        val linePath = Path()
                        val fillPath = Path()

                        batteryHistory.forEachIndexed { i, bat ->
                            // Map battery (0..100) to height (h..0)
                            val pct = bat / 100f
                            val cy = h - (pct * h)
                            val cx = i * stepX

                            if (i == 0) {
                                linePath.moveTo(cx, cy)
                                fillPath.moveTo(cx, h)
                                fillPath.lineTo(cx, cy)
                            } else {
                                linePath.lineTo(cx, cy)
                                fillPath.lineTo(cx, cy)
                            }

                            if (i == pointsCount - 1) {
                                fillPath.lineTo(cx, h)
                                fillPath.close()
                            }
                        }

                        // Fill under the line
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.2f), Color.Transparent)
                            )
                        )
                        // Draw line
                        drawPath(
                            path = linePath,
                            color = Color(0xFF10B981),
                            style = Stroke(width = 2.5f)
                        )
                    }
                }
            }
        }

        // Bar Chart: Area Progress Over Time
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "📐 TEMİZLEME HACMİ (m²)",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
            ) {
                if (areaHistory.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Veri bekleniyor...", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
                    }
                } else {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val maxVal = (areaHistory.maxOrNull() ?: 10).coerceAtLeast(10).toFloat()
                        val barCount = areaHistory.size
                        val barSpacing = 6f
                        val barWidth = (w - (barSpacing * (barCount - 1))) / barCount

                        areaHistory.forEachIndexed { i, area ->
                            val cy = (area / maxVal) * h
                            val rx = i * (barWidth + barSpacing)
                            val ry = h - cy

                            drawRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(Color(0xFF8B5CF6), Color(0xFF6366F1))
                                ),
                                topLeft = Offset(rx, ry),
                                size = Size(barWidth, cy)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TabLogsAndConsumables(telemetry: VacuumTelemetry?, eventLog: List<String>) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Consumables Rings Grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "⚙️ SARF MALZEMELERİ SAĞLIK DURUMU",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ConsumableRing("Yan Fırça", telemetry?.sideBrushLife ?: 95, Color(0xFF06B6D4))
                ConsumableRing("Ana Fırça", telemetry?.mainBrushLife ?: 88, Color(0xFF8B5CF6))
                ConsumableRing("Filtre", telemetry?.filterLife ?: 92, Color(0xFFF59E0B))
                ConsumableRing("Paspas", telemetry?.mopLife ?: 74, Color(0xFFEC4899))
            }
        }

        // Live Log Chronology View
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Text(
                text = "🛡️ REAL-TIME KARAR & ENGEL LOGU",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(eventLog) { logText ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "⚡",
                            color = if (logText.contains("UYARI")) Color(0xFFEF4444) else Color(0xFF6366F1),
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp, end = 6.dp)
                        )
                        Text(
                            text = logText,
                            color = if (logText.contains("UYARI")) Color(0xFFFCA5A5) else Color.White.copy(alpha = 0.7f),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ConsumableRing(label: String, pct: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(50.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    style = Stroke(width = 4.dp.toPx())
                )
                // Percentage arc
                val sweepAngle = (pct / 100f) * 360f
                drawArc(
                    color = if (pct < 20) Color(0xFFEF4444) else color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx())
                )
            }
            Text(
                text = "%$pct",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Normal
        )
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(12.dp))
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
