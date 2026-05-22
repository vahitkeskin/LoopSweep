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
        label = "Statistics",
        icon = { color -> StatisticsIcon(color = color) }
    )
    data object Cloud : BottomNavItem(
        screen = Screen.Cloud,
        label = "Purchase",
        icon = { color -> PurchaseIcon(color = color) }
    )
}

@Composable
fun BarcodeIcon(color: Color = Color.Black) {
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        val strokeWidth = 2.dp.toPx()
        drawLine(
            color,
            Offset(w * 0.15f, h * 0.2f),
            Offset(w * 0.15f, h * 0.8f),
            strokeWidth * 1.5f
        )
        drawLine(color, Offset(w * 0.3f, h * 0.2f), Offset(w * 0.3f, h * 0.8f), strokeWidth * 0.5f)
        drawLine(
            color,
            Offset(w * 0.42f, h * 0.2f),
            Offset(w * 0.42f, h * 0.8f),
            strokeWidth * 1.2f
        )
        drawLine(
            color,
            Offset(w * 0.55f, h * 0.2f),
            Offset(w * 0.55f, h * 0.8f),
            strokeWidth * 0.8f
        )
        drawLine(color, Offset(w * 0.7f, h * 0.2f), Offset(w * 0.7f, h * 0.8f), strokeWidth * 1.6f)
        drawLine(
            color,
            Offset(w * 0.85f, h * 0.2f),
            Offset(w * 0.85f, h * 0.8f),
            strokeWidth * 0.5f
        )
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
