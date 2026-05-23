package com.vahitkeskin.loopsweep.ui.components

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
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
    var activeTab by remember { mutableStateOf(0) } // 0: Radar Harita, 1: Detaylar, 2: Grafikler, 3: Sarf & Log, 4: Ham JSON

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
        // High-Tech Custom Segmented Scrollable Tab Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White.copy(alpha = 0.04f))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            val tabs = listOf("Radar Harita", "Detaylı Veriler", "Grafik Analiz", "Sarf & Günlük", "Ham JSON")
            tabs.forEachIndexed { index, title ->
                val selected = activeTab == index
                
                // Spring bounce scale on tab click
                val scale by animateFloatAsState(
                    targetValue = if (selected) 1.04f else 1.0f,
                    animationSpec = spring(stiffness = Spring.StiffnessLow),
                    label = "TabActiveScale"
                )
                
                val tabBg = if (selected) {
                    Brush.horizontalGradient(listOf(ThemeIndigo, MediumPurple))
                } else {
                    Brush.horizontalGradient(listOf(Color.Transparent, Color.Transparent))
                }
                
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                        }
                        .background(
                            brush = tabBg,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .border(
                            width = 0.5.dp,
                            color = if (selected) Color.White.copy(alpha = 0.15f) else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                        .clickable { activeTab = index }
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (selected) Color.White else Color.White.copy(alpha = 0.55f),
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
            1 -> TabDetailedTelemetry(telemetry, distanceMeters)
            2 -> TabCharts(batteryHistory, areaHistory)
            3 -> TabLogsAndConsumables(telemetry, eventLog)
            4 -> TabRawJson(telemetry)
        }
    }
}

@Composable
fun TabMapAndTelemetry(telemetry: VacuumTelemetry?, distanceMeters: Double) {
    val isCleaning = telemetry?.statusCode in listOf(5, 6, 7)

    // Telemetry Mini Metrics Row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MetricCard("Mesafe", "${distanceMeters.toString().take(5)} m", "🧭", Modifier.weight(1f))
        MetricCard("Çalışma Süresi", "${telemetry?.cleanTimeMinutes ?: 0} dk", "⏱️", Modifier.weight(1f))
        MetricCard("Alan", "${telemetry?.cleanAreaSqm ?: 0} m²", "📐", Modifier.weight(1f))
    }

    Spacer(modifier = Modifier.height(16.dp))

    // Real-Time Canvas SLAM Map / Radar Sweep View
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(DeepSpaceNavy)
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
                    color = SlateNavy,
                    start = Offset(dx, 0f),
                    end = Offset(dx, height),
                    strokeWidth = 1f
                )
            }
            for (y in 0..(height / gridSize).toInt()) {
                val dy = y * gridSize
                drawLine(
                    color = SlateNavy,
                    start = Offset(0f, dy),
                    end = Offset(width, dy),
                    strokeWidth = 1f
                )
            }

            // Draw concentric radar circles
            val maxRadius = minOf(width, height) * 0.45f
            drawCircle(
                color = EmeraldGreen.copy(alpha = 0.05f),
                center = center,
                radius = maxRadius,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = EmeraldGreen.copy(alpha = 0.08f),
                center = center,
                radius = maxRadius * 0.66f,
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = EmeraldGreen.copy(alpha = 0.1f),
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
                    colors = listOf(EmeraldGreen.copy(alpha = 0.8f), Color.Transparent),
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
                color = ThemeCyan.copy(alpha = 0.3f),
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
                    color = AlertRed.copy(alpha = 0.15f),
                    center = obstacle,
                    radius = 12f
                )
                drawCircle(
                    color = AlertRed,
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
                    color = ThemeOrange.copy(alpha = 0.6f),
                    style = Stroke(width = 3f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Draw current robot position (vacuum indicator)
                val robotPos = points[pathToShowCount - 1]
                drawCircle(
                    color = ThemeOrange.copy(alpha = 0.25f),
                    center = robotPos,
                    radius = 15f
                )
                drawCircle(
                    color = ThemeOrange,
                    center = robotPos,
                    radius = 6f
                )
            } else {
                // Idle position at dock
                val dockPos = Offset(center.x, center.y + 110f)
                drawCircle(
                    color = ThemeBlue.copy(alpha = 0.3f),
                    center = dockPos,
                    radius = 12f
                )
                drawCircle(
                    color = ThemeBlue,
                    center = dockPos,
                    radius = 5f
                )
            }
        }

        // Overlay status text
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
                color = if (isCleaning) AlertRed else Color.White.copy(alpha = 0.7f),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

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
fun TabDetailedTelemetry(telemetry: VacuumTelemetry?, distanceMeters: Double) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        val fault = telemetry?.faultCode ?: 0
        if (fault != 0) {
            FaultAlertCard(faultCode = fault)
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricCard("Mesafe", "${distanceMeters.toString().take(5)} m", "🧭", Modifier.weight(1f))
                MetricCard("Çalışma Süresi", "${telemetry?.cleanTimeMinutes ?: 0} dk", "⏱️", Modifier.weight(1f))
                MetricCard("Alan", "${telemetry?.cleanAreaSqm ?: 0} m²", "📐", Modifier.weight(1f))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val isCharging = telemetry?.statusCode == 4
                MetricCard("Pil Seviyesi", "%${telemetry?.batteryLevel ?: 0}", if (isCharging) "⚡" else "🔋", Modifier.weight(1f))
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
                    Modifier.weight(1f)
                )
                MetricCard(
                    "Su Akışı",
                    when (telemetry?.waterState) {
                        0 -> "Düşük"
                        1 -> "Orta"
                        2 -> "Yüksek"
                        else -> "Kapalı"
                    },
                    "💧",
                    Modifier.weight(1f)
                )
            }
        }

        // Hardware Level Segmented Gauges
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "📊 PARAMETRİK SEVİYE GÖSTERGELERİ",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )

            val suctionVal = (telemetry?.suctionState ?: 1) + 1
            SegmentedBarGauge(
                label = "Emiş Gücü Kademesi (Vakum Motor Hızı)",
                activeSegments = suctionVal.coerceIn(1, 4),
                totalSegments = 4,
                activeColor = MediumPurple
            )

            val waterVal = (telemetry?.waterState ?: 1) + 1
            SegmentedBarGauge(
                label = "Su Pompası Debisi (Paspas Islaklığı)",
                activeSegments = waterVal.coerceIn(1, 3),
                totalSegments = 3,
                activeColor = ThemeCyan
            )
        }
    }
}

@Composable
fun SegmentedBarGauge(label: String, activeSegments: Int, totalSegments: Int, activeColor: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$activeSegments / $totalSegments",
                color = activeColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            for (i in 1..totalSegments) {
                val isActive = i <= activeSegments
                val segmentColor = if (isActive) activeColor else Color.White.copy(alpha = 0.05f)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(segmentColor)
                        .border(0.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(4.dp))
                )
            }
        }
    }
}

@Composable
fun FaultAlertCard(faultCode: Int) {
    val faultText = when (faultCode) {
        1 -> "Lazer Mesafe Sensörü (LDS) engellendi veya takıldı."
        2 -> "Çarpışma tamponu sıkıştı. Lütfen temizleyin."
        3 -> "Tekerlek havada kaldı. Düz zemine koyun."
        4 -> "Düşme sensörlerini temizleyin."
        5 -> "Ana fırça dolandı. Temizleyin."
        6 -> "Yan fırça dolandı. Temizleyin."
        7 -> "Tahrik tekerleği sıkıştı."
        8 -> "Cihaz bir alanda sıkıştı."
        9 -> "Toz haznesi takılı değil."
        10 -> "Su tankı takılı değil."
        11 -> "Mop braketi takılı değil."
        else -> "Hata tespit edildi (Hata Kodu: $faultCode)"
    }
    
    val infiniteTransition = rememberInfiniteTransition()
    val warningAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        )
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AlertRed.copy(alpha = 0.15f * warningAlpha))
            .border(1.dp, AlertRed.copy(alpha = 0.4f * warningAlpha), RoundedCornerShape(12.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚠️",
            fontSize = 18.sp,
            modifier = Modifier.padding(end = 10.dp)
        )
        Column {
            Text(
                text = "CİHAZ UYARISI / HATA BİLDİRİMİ",
                color = LightRed,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = faultText,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
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
                    .height(96.dp)
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
                            val pct = bat.coerceIn(0, 100) / 100f
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

                        // Fill under the line with visual gradient
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(EmeraldGreen.copy(alpha = 0.22f), Color.Transparent)
                            )
                        )
                        // Draw line with rounded joints
                        drawPath(
                            path = linePath,
                            color = EmeraldGreen,
                            style = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        
                        // Draw a glowing point at the last item
                        if (pointsCount > 0) {
                            val lastPct = (batteryHistory.lastOrNull() ?: 100) / 100f
                            val lastCx = (pointsCount - 1) * stepX
                            val lastCy = h - (lastPct * h)
                            
                            drawCircle(
                                color = EmeraldGreen.copy(alpha = 0.35f),
                                radius = 6.dp.toPx(),
                                center = Offset(lastCx, lastCy)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = 2.5.dp.toPx(),
                                center = Offset(lastCx, lastCy)
                            )
                        }
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
                    .height(96.dp)
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
                        val barSpacing = 8f
                        val barWidth = (w - (barSpacing * (barCount - 1))) / barCount

                        areaHistory.forEachIndexed { i, area ->
                            val cy = (area / maxVal) * h
                            val rx = i * (barWidth + barSpacing)
                            val ry = h - cy

                            // Draw rounded bars at top end
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(MediumPurple, ThemeIndigo)
                                ),
                                topLeft = Offset(rx, ry),
                                size = Size(barWidth, cy),
                                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
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
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                ConsumableRing("Yan Fırça", telemetry?.sideBrushLife ?: 95, ThemeCyan)
                ConsumableRing("Ana Fırça", telemetry?.mainBrushLife ?: 88, MediumPurple)
                ConsumableRing("Filtre", telemetry?.filterLife ?: 92, ThemeOrange)
                ConsumableRing("Paspas", telemetry?.mopLife ?: 74, ThemePink)
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
                            color = if (logText.contains("UYARI")) AlertRed else ThemeIndigo,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(top = 2.dp, end = 6.dp)
                        )
                        Text(
                            text = logText,
                            color = if (logText.contains("UYARI")) LightRed else Color.White.copy(alpha = 0.7f),
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
fun TabRawJson(telemetry: VacuumTelemetry?) {
    val rawJson = telemetry?.rawJson
    
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🔌 CANLI UDP JSON RESPONSE PAYLOAD",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = if (rawJson != null) "GÜNCEL" else "ÇEVRİMDIŞI",
                color = if (rawJson != null) EmeraldGreen else Color.White.copy(alpha = 0.4f),
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (rawJson != null) EmeraldGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.05f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(JetBlack) // Terminal jet black
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            if (rawJson == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📡", fontSize = 24.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Robot süpürgeden gelen UDP paketleri bekleniyor...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                val formattedJson = remember(rawJson) {
                    formatAndHighlightJson(minifyJson(rawJson))
                }
                
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Text(
                        text = formattedJson,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }
        
        Text(
            text = "Not: Cihaz MIoT protokolü üzerinden UDP paketleri gönderir. Bu veriler anlık olarak çözülüp yukarıda sunulmaktadır.",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 9.sp,
            lineHeight = 13.sp
        )
    }
}

@Composable
fun ConsumableRing(label: String, pct: Int, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(54.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background circle
                drawCircle(
                    color = Color.White.copy(alpha = 0.05f),
                    style = Stroke(width = 4.dp.toPx())
                )
                // Percentage arc with Rounded Caps
                val sweepAngle = (pct / 100f) * 360f
                drawArc(
                    color = if (pct < 20) AlertRed else color,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = 4.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            Text(
                text = "%$pct",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.6f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun MetricCard(label: String, value: String, icon: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.04f))
            .border(0.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(icon, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

// Helper methods for formatting and syntax highlighting JSON safely in KMP

fun minifyJson(json: String): String {
    val sb = StringBuilder()
    var inString = false
    var i = 0
    val len = json.length
    while (i < len) {
        val char = json[i]
        if (char == '"' && (i == 0 || json[i - 1] != '\\')) {
            inString = !inString
            sb.append(char)
        } else if (inString) {
            sb.append(char)
        } else {
            if (!char.isWhitespace()) {
                sb.append(char)
            }
        }
        i++
    }
    return sb.toString()
}

fun formatAndHighlightJson(json: String): AnnotatedString {
    return buildAnnotatedString {
        var indent = 0
        var inString = false
        var isKey = false
        val len = json.length
        var i = 0
        
        while (i < len) {
            val char = json[i]
            
            if (char == '"' && (i == 0 || json[i - 1] != '\\')) {
                inString = !inString
                withStyle(style = SpanStyle(color = if (isKey) ElectricCyan else LightGreen)) {
                    append(char)
                }
                i++
                continue
            }
            
            if (inString) {
                withStyle(style = SpanStyle(color = if (isKey) ElectricCyan else LightGreen)) {
                    append(char)
                }
                i++
                continue
            }
            
            if (char.isWhitespace()) {
                append(char)
                i++
                continue
            }
            
            when (char) {
                '{', '[' -> {
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                        append(char)
                    }
                    indent++
                    append("\n" + "  ".repeat(indent))
                    isKey = true
                }
                '}', ']' -> {
                    indent = (indent - 1).coerceAtLeast(0)
                    append("\n" + "  ".repeat(indent))
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                        append(char)
                    }
                }
                ',' -> {
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.8f))) {
                        append(char)
                    }
                    append("\n" + "  ".repeat(indent))
                    isKey = true
                }
                ':' -> {
                    withStyle(style = SpanStyle(color = Color.White.copy(alpha = 0.6f))) {
                        append(" : ")
                    }
                    isKey = false
                }
                else -> {
                    if (char.isDigit() || char == '-' || char == '.') {
                        var numStr = ""
                        while (i < len && (json[i].isDigit() || json[i] == '.' || json[i] == '-' || json[i] == '+' || json[i] == 'e' || json[i] == 'E')) {
                            numStr += json[i]
                            i++
                        }
                        withStyle(style = SpanStyle(color = LightOrange)) {
                            append(numStr)
                        }
                        continue
                    } else if (json.startsWith("true", i)) {
                        withStyle(style = SpanStyle(color = LightBlue)) {
                            append("true")
                        }
                        i += 4
                        continue
                    } else if (json.startsWith("false", i)) {
                        withStyle(style = SpanStyle(color = CoralRed)) {
                            append("false")
                        }
                        i += 5
                        continue
                    } else if (json.startsWith("null", i)) {
                        withStyle(style = SpanStyle(color = LightPurple)) {
                            append("null")
                        }
                        i += 4
                        continue
                    } else {
                        append(char)
                    }
                }
            }
            i++
        }
    }
}
