package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextSecondary

@Composable
fun FrameHistoryChart(
    frameTimes: List<Float>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
            .testTag("frame_history_chart")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FRAME PACING & RENDER TIME",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LegendItem(color = ElectricLime, label = "8.3ms (120 FPS)")
                    Spacer(modifier = Modifier.width(8.dp))
                    LegendItem(color = NeonCyan, label = "16.6ms (60 FPS)")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkBackground)
                    .padding(vertical = 6.dp, horizontal = 4.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height

                // Render limits: 0 ms to 25 ms
                val maxMs = 25f

                // Reference lines: 8.33ms (120 FPS), 11.1ms (90 FPS), 16.67ms (60 FPS)
                val line120Y = chartHeight - ((8.33f / maxMs) * chartHeight)
                val line90Y = chartHeight - ((11.1f / maxMs) * chartHeight)
                val line60Y = chartHeight - ((16.67f / maxMs) * chartHeight)

                val dashedEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                // 60 FPS line (cyan)
                drawLine(
                    color = NeonCyan.copy(alpha = 0.35f),
                    start = Offset(0f, line60Y),
                    end = Offset(chartWidth, line60Y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashedEffect
                )

                // 90 FPS line (subtle)
                drawLine(
                    color = Color(0xFF64748B).copy(alpha = 0.25f),
                    start = Offset(0f, line90Y),
                    end = Offset(chartWidth, line90Y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashedEffect
                )

                // 120 FPS line (green)
                drawLine(
                    color = ElectricLime.copy(alpha = 0.5f),
                    start = Offset(0f, line120Y),
                    end = Offset(chartWidth, line120Y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = dashedEffect
                )

                if (frameTimes.isNotEmpty()) {
                    val stepX = if (frameTimes.size > 1) chartWidth / (frameTimes.size - 1) else chartWidth
                    val path = Path()

                    frameTimes.forEachIndexed { index, timeMs ->
                        val clampedTime = timeMs.coerceIn(0f, maxMs)
                        val x = index * stepX
                        val y = chartHeight - ((clampedTime / maxMs) * chartHeight)

                        if (index == 0) {
                            path.moveTo(x, y)
                        } else {
                            path.lineTo(x, y)
                        }
                    }

                    drawPath(
                        path = path,
                        color = ElectricLime,
                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Previous 40 Frames", color = TextMuted, fontSize = 10.sp)
                Text(text = "Lower ms = Faster Response", color = TextMuted, fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(10.dp)
                .height(2.dp)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, color = TextMuted, fontSize = 9.sp)
    }
}
