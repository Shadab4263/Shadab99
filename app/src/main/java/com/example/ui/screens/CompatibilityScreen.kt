package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CompatibilityGrade
import com.example.ui.MainUiState
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CompatibilityScreen(
    state: MainUiState,
    onRefreshScan: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val verdict = state.verdict
    val hardware = state.hardwareSpecs
    val display = state.displayTelemetry

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "BGMI 120 FPS WHITELIST",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Hardware Capability Audit",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black
                )
            }

            Button(
                onClick = onRefreshScan,
                modifier = Modifier
                    .height(38.dp)
                    .testTag("rescan_compatibility_button"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rescan",
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "Rescan", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Verdict Hero Card
        val badgeColor = when (verdict.grade) {
            CompatibilityGrade.GRADE_120_ULTRA -> ElectricLime
            CompatibilityGrade.GRADE_90_HIGH -> NeonCyan
            CompatibilityGrade.GRADE_60_EXTREME -> Color(0xFFFFA000)
            CompatibilityGrade.GRADE_LIMITED -> Color(0xFFFF5252)
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            badgeColor.copy(alpha = 0.15f),
                            DarkSurfaceCard
                        )
                    )
                )
                .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(18.dp)
                .testTag("compatibility_verdict_card")
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(badgeColor.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = verdict.title,
                                color = badgeColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = verdict.subtitle,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Score Circle
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(DarkBackground)
                            .border(2.dp, badgeColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${verdict.overallScore}",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "/ 100",
                                color = TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Score bars
                ScoreRow(title = "Display Panel (120Hz)", score = verdict.displayScore, color = ElectricLime)
                Spacer(modifier = Modifier.height(6.dp))
                ScoreRow(title = "SoC Gaming Compute", score = verdict.cpuScore, color = NeonCyan)
                Spacer(modifier = Modifier.height(6.dp))
                ScoreRow(title = "RAM Headroom", score = verdict.ramScore, color = Color(0xFF60A5FA))
                Spacer(modifier = Modifier.height(6.dp))
                ScoreRow(title = "Thermal Margin", score = verdict.thermalScore, color = badgeColor)
            }
        }

        // Requirements Checklist
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("requirements_checklist_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "HARDWARE VALIDATION CRITERIA",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                verdict.keyFactors.forEach { (factor, passed) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = factor,
                            color = if (passed) TextPrimary else TextMuted,
                            fontSize = 12.sp,
                            fontWeight = if (passed) FontWeight.Medium else FontWeight.Normal
                        )
                        Icon(
                            imageVector = if (passed) Icons.Default.CheckCircle else Icons.Default.Warning,
                            contentDescription = if (passed) "Passed" else "Check",
                            tint = if (passed) ElectricLime else Color(0xFFFFA000),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Detected Display Modes List
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("display_modes_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HARDWARE DISPLAY MODES",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${display.modes.size} Detected",
                        color = NeonCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                display.modes.forEach { mode ->
                    val isHighHz = mode.refreshRate >= 115f
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (mode.isCurrent) NeonCyan.copy(alpha = 0.12f) else DarkBackground)
                            .border(
                                1.dp,
                                if (mode.isCurrent) NeonCyan else DarkSurfaceBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "${mode.width} x ${mode.height}",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (mode.isCurrent) "ACTIVE MODE" else "Mode ID: ${mode.modeId}",
                                color = if (mode.isCurrent) NeonCyan else TextMuted,
                                fontSize = 10.sp,
                                fontWeight = if (mode.isCurrent) FontWeight.Bold else FontWeight.Normal
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isHighHz) ElectricLime.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${mode.refreshRate.toInt()} Hz",
                                color = if (isHighHz) ElectricLime else TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Recommended In-Game Settings Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(16.dp)
                .testTag("recommended_settings_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "RECOMMENDED BGMI IN-GAME SETTINGS",
                    color = ElectricLime,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )

                ConfigRow(label = "Graphics", value = verdict.recommendedGraphics)
                ConfigRow(label = "Frame Rate", value = verdict.recommendedFrameRate)
                ConfigRow(label = "Combat Style", value = "Colorful / Soft")
                ConfigRow(label = "Anti-Aliasing", value = "2X (or Close for max FPS)")
                ConfigRow(label = "Auto-Adjust Graphics", value = "Disabled (Critical to avoid 60 FPS drops)")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun ScoreRow(title: String, score: Int, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, color = TextSecondary, fontSize = 11.sp)
            Text(
                text = "$score%",
                color = color,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color(0xFF1E283A)
        )
    }
}

@Composable
private fun ConfigRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextMuted, fontSize = 12.sp)
        Text(text = value, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
