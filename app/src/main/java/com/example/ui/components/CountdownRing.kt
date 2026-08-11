package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CodeUrgentAmber
import com.example.ui.theme.CodeUrgentRed

/**
 * Continuous circular countdown ring driven by real elapsed time (§2.3).
 * Color shifts smoothly from Primary -> Amber -> Red in the final 5 seconds.
 */
@Composable
fun CountdownRing(
    remainingSeconds: Int,
    periodProgress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 36.dp,
    strokeWidth: Dp = 3.dp,
    showText: Boolean = true
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.surfaceVariant

    val ringColor = when {
        remainingSeconds <= 3 -> CodeUrgentRed
        remainingSeconds <= 6 -> CodeUrgentAmber
        else -> primaryColor
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = periodProgress * 360f
            val stroke = strokeWidth.toPx()
            val arcSize = Size(this.size.width - stroke, this.size.height - stroke)

            // Background Track
            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
            )

            // Depleting Arc
            drawArc(
                color = ringColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                size = arcSize,
                topLeft = androidx.compose.ui.geometry.Offset(stroke / 2, stroke / 2)
            )
        }

        if (showText) {
            Text(
                text = "$remainingSeconds",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    color = ringColor
                )
            )
        }
    }
}
