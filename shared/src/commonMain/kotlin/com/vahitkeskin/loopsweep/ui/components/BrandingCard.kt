package com.vahitkeskin.loopsweep.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vahitkeskin.loopsweep.ui.theme.ElectricCyan
import com.vahitkeskin.loopsweep.ui.theme.EmeraldGreen
import com.vahitkeskin.loopsweep.ui.theme.DeepSpaceNavy
import com.vahitkeskin.loopsweep.utils.LoopSweepPreview

/**
 * A highly professional, minimalist branding text and logo layout.
 * Displays the glowing logo, brand title, line divider, and subtitle
 * directly over a blurred background.
 */
@Composable
fun BrandingCard(
    cardAlpha: Float,
    logoAlpha: Float,
    subtitleAlpha: Float,
    shimmerTranslate: Float,
    modifier: Modifier = Modifier
) {
    if (logoAlpha <= 0f && subtitleAlpha <= 0f) return

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Minimalist Glowing Brand Logo
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .graphicsLayer { alpha = logoAlpha },
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val radius = size.minDimension / 2f

                    // Outer clean orbit loop
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        radius = radius * 0.85f,
                        style = Stroke(width = 1.5.dp.toPx())
                    )

                    // Active sweep laser arc
                    drawArc(
                        color = EmeraldGreen.copy(alpha = 0.9f),
                        startAngle = -90f,
                        sweepAngle = 280f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius * 0.85f, center.y - radius * 0.85f),
                        size = Size(radius * 1.7f, radius * 1.7f),
                        style = Stroke(width = 2.5.dp.toPx())
                    )

                    // Laser sweep endpoint dot
                    drawCircle(
                        color = EmeraldGreen,
                        radius = 3.5.dp.toPx(),
                        center = center
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Brand Title with Soft Glowing Text Shadow
            Text(
                text = "LOOPSWEEP",
                fontSize = 36.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                style = TextStyle(
                    shadow = Shadow(
                        color = EmeraldGreen.copy(alpha = 0.6f),
                        offset = Offset(0f, 0f),
                        blurRadius = 16f
                    )
                ),
                letterSpacing = 7.sp,
                modifier = Modifier.graphicsLayer { alpha = logoAlpha }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Thin Horizontal Divider with Glowing Center Accent
            Box(
                modifier = Modifier
                    .width(100.dp)
                    .height(6.dp)
                    .graphicsLayer { alpha = logoAlpha },
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(EmeraldGreen, shape = RoundedCornerShape(2.0.dp))
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Tracked Subtitle with Soft Cyber Cyan Shadow
            Text(
                text = "NEXT-GEN VACUUM CONTROLLER",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.7f),
                style = TextStyle(
                    shadow = Shadow(
                        color = ElectricCyan.copy(alpha = 0.4f),
                        offset = Offset(0f, 0f),
                        blurRadius = 8f
                    )
                ),
                letterSpacing = 3.5.sp,
                modifier = Modifier
                    .graphicsLayer { alpha = subtitleAlpha }
            )
        }
    }
}

/**
 * Preview function to inspect the BrandingCard styling inside Compose tooling.
 */
@LoopSweepPreview
@Composable
fun BrandingCardPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DeepSpaceNavy),
        contentAlignment = Alignment.Center
    ) {
        BrandingCard(
            cardAlpha = 1f,
            logoAlpha = 1f,
            subtitleAlpha = 1f,
            shimmerTranslate = 400f
        )
    }
}