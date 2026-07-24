package com.hermes.companion.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Circular gauge composable for CPU/RAM/battery display.
 *
 * Shows a circular progress arc with a label and percentage in the center.
 */
@Composable
fun PerformanceGauge(
    value: Float,          // 0..100
    label: String,
    unit: String = "%",
    color: Color = Color(0xFF2196F3),
    trackColor: Color = Color(0xFF263238),
    size: Dp = 120.dp,
    strokeWidth: Float = 10f,
    modifier: Modifier = Modifier
) {
    val textColor = Color(0xFFE0E0E0)
    val labelColor = Color(0xFF90A4AE)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val sweepAngle = 240f  // Arc covers 240 degrees
            val startAngle = 150f // Start from bottom-left
            val padding = strokeWidth / 2
            val arcSize = Size(
                this.size.width - strokeWidth,
                this.size.height - strokeWidth
            )

            // Background track
            drawArc(
                color = trackColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Value arc
            val valueSweep = (value.coerceIn(0f, 100f) / 100f) * sweepAngle
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = valueSweep,
                useCenter = false,
                topLeft = Offset(padding, padding),
                size = arcSize,
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )

            // Tick marks (at 0%, 25%, 50%, 75%, 100%)
            for (tick in 0..4) {
                val angle = Math.toRadians((startAngle + sweepAngle * tick / 4.0).toDouble())
                val innerRadius = (this.size.width - strokeWidth * 2) / 2 - 8
                val outerRadius = (this.size.width - strokeWidth * 2) / 2 + 4
                val centerX = this.size.width / 2
                val centerY = this.size.height / 2

                val x1 = centerX + innerRadius * Math.cos(angle).toFloat()
                val y1 = centerY + innerRadius * Math.sin(angle).toFloat()
                val x2 = centerX + outerRadius * Math.cos(angle).toFloat()
                val y2 = centerY + outerRadius * Math.sin(angle).toFloat()

                drawLine(
                    color = trackColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2f
                )
            }
        }

        // Center text
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${value.toInt()}$unit",
                color = textColor,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                color = labelColor,
                fontSize = 11.sp
            )
        }
    }
}

// ── Preset Colors ────────────────────────────────────────

object GaugeColors {
    val cpu = Color(0xFF2196F3)
    val ram = Color(0xFF4CAF50)
    val battery = Color(0xFFFFEB3B)
    val batteryLow = Color(0xFFF44336)
    val temperature = Color(0xFFFF5722)
    val disk = Color(0xFF9C27B0)
    val network = Color(0xFF00BCD4)
}

// ── Preview ──────────────────────────────────────────────

@Composable
fun PerformanceGaugePreview() {
    Box(contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            PerformanceGauge(
                value = 67f,
                label = "CPU",
                color = GaugeColors.cpu,
                size = 100.dp,
                strokeWidth = 8f
            )
        }
    }
}
