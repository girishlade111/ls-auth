package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp

@Composable
fun QrReticleOverlay(
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val cornerPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val reticleSize = width.coerceAtMost(height) * 0.72f
        val left = (width - reticleSize) / 2
        val top = (height - reticleSize) / 2
        val cornerRadius = 24.dp.toPx()
        val strokeWidth = 5.dp.toPx()
        val cornerLength = 36.dp.toPx()

        val reticleRect = Rect(left, top, left + reticleSize, top + reticleSize)
        val cutoutPath = Path().apply {
            addRoundRect(
                RoundRect(
                    rect = reticleRect,
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            )
        }

        // Dark dim background outside reticle
        clipPath(cutoutPath, clipOp = ClipOp.Difference) {
            drawRect(color = Color.Black.copy(alpha = 0.65f))
        }

        // Draw Reticle Border
        drawRoundRect(
            color = Color.White.copy(alpha = 0.3f),
            topLeft = Offset(left, top),
            size = Size(reticleSize, reticleSize),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius),
            style = Stroke(width = 2.dp.toPx())
        )

        // Draw Animated Corner Accents
        val accentColor = primaryColor.copy(alpha = cornerPulseAlpha)

        // Top-Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left, top + cornerLength)
                lineTo(left, top + cornerRadius)
                quadraticTo(left, top, left + cornerRadius, top)
                lineTo(left + cornerLength, top)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Top-Right Corner
        drawPath(
            path = Path().apply {
                moveTo(left + reticleSize - cornerLength, top)
                lineTo(left + reticleSize - cornerRadius, top)
                quadraticTo(left + reticleSize, top, left + reticleSize, top + cornerRadius)
                lineTo(left + reticleSize, top + cornerLength)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom-Right Corner
        drawPath(
            path = Path().apply {
                moveTo(left + reticleSize, top + reticleSize - cornerLength)
                lineTo(left + reticleSize, top + reticleSize - cornerRadius)
                quadraticTo(left + reticleSize, top + reticleSize, left + reticleSize - cornerRadius, top + reticleSize)
                lineTo(left + reticleSize - cornerLength, top + reticleSize)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Bottom-Left Corner
        drawPath(
            path = Path().apply {
                moveTo(left + cornerLength, top + reticleSize)
                lineTo(left + cornerRadius, top + reticleSize)
                quadraticTo(left, top + reticleSize, left, top + reticleSize - cornerRadius)
                lineTo(left, top + reticleSize - cornerLength)
            },
            color = accentColor,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )
    }
}
