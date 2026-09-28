package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun SparklineChart(
    dataPoints: List<Float>,
    lineColor: Color,
    modifier: Modifier = Modifier,
    minBound: Float = 0f,
    maxBound: Float = 100f,
    unit: String = "%",
    height: Dp = 80.dp,
    showGrid: Boolean = true,
    label: String? = null
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0B101E))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        val currentVal = dataPoints.lastOrNull() ?: 0f
        val maxVal = (dataPoints.maxOrNull() ?: maxBound).coerceAtLeast(minBound + 1f)
        val minVal = (dataPoints.minOrNull() ?: minBound).coerceAtLeast(0f)

        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val chartHeight = size.height

            if (showGrid) {
                // Top, Middle, Bottom dotted grid lines
                val gridY = floatArrayOf(0f, chartHeight * 0.5f, chartHeight)
                gridY.forEach { y ->
                    drawLine(
                        color = SurfaceBorder.copy(alpha = 0.5f),
                        start = Offset(0f, y),
                        end = Offset(width, y),
                        strokeWidth = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f)
                    )
                }
            }

            if (dataPoints.size < 2) return@Canvas

            val range = (maxBound - minBound).coerceAtLeast(1f)
            val stepX = width / (dataPoints.size - 1).toFloat()

            val strokePath = Path()
            val fillPath = Path()

            dataPoints.forEachIndexed { index, value ->
                val normY = 1f - ((value - minBound) / range).coerceIn(0f, 1f)
                val x = index * stepX
                val y = normY * chartHeight

                if (index == 0) {
                    strokePath.moveTo(x, y)
                    fillPath.moveTo(x, chartHeight)
                    fillPath.lineTo(x, y)
                } else {
                    val prevX = (index - 1) * stepX
                    val prevNormY = 1f - ((dataPoints[index - 1] - minBound) / range).coerceIn(0f, 1f)
                    val prevY = prevNormY * chartHeight

                    val cX1 = prevX + (x - prevX) / 2f
                    val cY1 = prevY
                    val cX2 = prevX + (x - prevX) / 2f
                    val cY2 = y

                    strokePath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                    fillPath.cubicTo(cX1, cY1, cX2, cY2, x, y)
                }

                if (index == dataPoints.size - 1) {
                    fillPath.lineTo(x, chartHeight)
                    fillPath.close()
                }
            }

            // Fill gradient under curve
            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        lineColor.copy(alpha = 0.35f),
                        lineColor.copy(alpha = 0.02f)
                    )
                )
            )

            // Glowing stroke
            drawPath(
                path = strokePath,
                color = lineColor,
                style = Stroke(
                    width = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )

            // Current value dot at the tip
            val lastX = width
            val lastNormY = 1f - ((currentVal - minBound) / range).coerceIn(0f, 1f)
            val lastY = lastNormY * chartHeight
            drawCircle(
                color = Color.White,
                radius = 3.5.dp.toPx(),
                center = Offset(lastX, lastY)
            )
            drawCircle(
                color = lineColor,
                radius = 5.5.dp.toPx(),
                center = Offset(lastX, lastY),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // Header info overlay
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            if (label != null) {
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = TextSecondary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${String.format("%.1f", currentVal)}$unit",
                fontSize = 11.sp,
                color = lineColor,
                fontFamily = FontFamily.Monospace
            )
        }

        // Scale bounds on bottom left & right
        Column(
            modifier = Modifier.align(Alignment.BottomStart)
        ) {
            Text(
                text = "${minBound.toInt()}$unit",
                fontSize = 8.sp,
                color = TextMuted,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
