package com.vahitkeskin.loopsweep.ui.components

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.utils.LoopSweepPreview
import loopsweep.shared.generated.resources.Res
import loopsweep.shared.generated.resources.robot_body
import loopsweep.shared.generated.resources.robot_brush
import org.jetbrains.compose.resources.painterResource

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
        remember { mutableStateOf(0f) }
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
        remember { mutableStateOf(0f) }
    }

    androidx.compose.foundation.layout.BoxWithConstraints(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val sizePx = maxWidth

        // Draw the Side Brush
        // Original viewport is 1024. Brush is at (850, 150), so DeltaX = +338, DeltaY = -362
        // Brush vector viewport is 400x400. 400 / 1024 = 0.39f
        Image(
            painter = painterResource(Res.drawable.robot_brush),
            contentDescription = "Side Brush",
            modifier = Modifier
                .size(sizePx * 0.39f)
                .offset(x = sizePx * 0.33f, y = -(sizePx * 0.353f))
                .rotate(brushAngle)
                .scale(1f + (armFlex + 0.4f) * 0.1f)
        )

        // Draw the Robot Body
        Image(
            painter = painterResource(Res.drawable.robot_body),
            contentDescription = "Robot Body",
            modifier = Modifier.fillMaxSize()
        )

        // Overlay the glowing LEDs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val r = minOf(w, h) / 2f

            if (isCleaning) {
                drawCircle(
                    color = EmeraldGreen.copy(alpha = 0.6f * powerLedAlpha),
                    radius = 2.dp.toPx(),
                    center = Offset(cx, cy - r * 0.58f)
                )
            }
            if (isCharging) {
                drawCircle(
                    color = AmberYellow.copy(alpha = 0.6f * chargeLedAlpha),
                    radius = 2.dp.toPx(),
                    center = Offset(cx, cy - r * 0.45f)
                )
            }
        }

        Text(
            text = "LOOP SWEEP",
            fontSize = 3.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.5f),
            letterSpacing = 0.5.sp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 10.dp)
        )
    }
}

@LoopSweepPreview
@Composable
fun RobotVacuumButtonContentPreview() {
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(SpaceDarkBg),
        contentAlignment = Alignment.Center
    ) {
        RobotVacuumButtonContent(
            isCleaning = true,
            isCharging = true
        )
    }
}
