package com.vahitkeskin.loopsweep.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.vahitkeskin.loopsweep.navigation.Screen

/**
 * Represents each navigation tab in the bottom bar with its screen target, text label, and icon representation.
 */
sealed class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: @Composable (Color) -> Unit
) {
    data object Dashboard : BottomNavItem(
        screen = Screen.Dashboard,
        label = "Dashboard",
        icon = { color -> StatisticsIcon(color = color) }
    )
    data object Cloud : BottomNavItem(
        screen = Screen.Cloud,
        label = "Mi Cloud",
        icon = { color -> CloudIcon(color = color) }
    )
}

@Composable
fun StatisticsIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val barWidth = w * 0.22f
        val spacing = w * 0.08f
        val strokeWidth = 1.5.dp.toPx()

        // Draw 3 glowing columns with rounded tops
        // Bar 1 (Left)
        drawRoundRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(spacing, h * 0.5f),
            size = Size(barWidth, h * 0.4f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(spacing, h * 0.5f),
            size = Size(barWidth, h * 0.4f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Bar 2 (Middle, Tall)
        drawRoundRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(spacing * 2 + barWidth, h * 0.25f),
            size = Size(barWidth, h * 0.65f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(spacing * 2 + barWidth, h * 0.25f),
            size = Size(barWidth, h * 0.65f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Bar 3 (Right)
        drawRoundRect(
            color = color.copy(alpha = 0.25f),
            topLeft = Offset(spacing * 3 + barWidth * 2, h * 0.4f),
            size = Size(barWidth, h * 0.5f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        drawRoundRect(
            color = color,
            topLeft = Offset(spacing * 3 + barWidth * 2, h * 0.4f),
            size = Size(barWidth, h * 0.5f),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            style = Stroke(width = strokeWidth)
        )

        // Subtle diagonal trend line passing over the bars for a high-tech finish
        val trendPath = Path().apply {
            moveTo(spacing + barWidth / 2f, h * 0.65f)
            lineTo(spacing * 2 + barWidth * 1.5f, h * 0.45f)
            lineTo(spacing * 3 + barWidth * 2.5f, h * 0.55f)
        }
        drawPath(
            path = trendPath,
            color = color.copy(alpha = 0.7f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun CloudIcon(color: Color) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()

        // Clean modern vector cloud shape
        val cloudPath = Path().apply {
            moveTo(w * 0.3f, h * 0.7f)
            // Left small bump
            cubicTo(w * 0.12f, h * 0.7f, w * 0.12f, h * 0.48f, w * 0.32f, h * 0.48f)
            // Top main dome
            cubicTo(w * 0.32f, h * 0.22f, w * 0.68f, h * 0.22f, w * 0.68f, h * 0.48f)
            // Right medium bump
            cubicTo(w * 0.88f, h * 0.48f, w * 0.88f, h * 0.7f, w * 0.7f, h * 0.7f)
            close()
        }

        // Semi-transparent fill
        drawPath(
            path = cloudPath,
            color = color.copy(alpha = 0.2f)
        )
        // Solid outline
        drawPath(
            path = cloudPath,
            color = color,
            style = Stroke(width = strokeWidth)
        )

        // Draw a tiny sync indicator inside the cloud (like a revolving circle/dots)
        drawCircle(
            color = color.copy(alpha = 0.8f),
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.42f, h * 0.55f)
        )
        drawCircle(
            color = color.copy(alpha = 0.8f),
            radius = 1.5.dp.toPx(),
            center = Offset(w * 0.58f, h * 0.55f)
        )
    }
}
