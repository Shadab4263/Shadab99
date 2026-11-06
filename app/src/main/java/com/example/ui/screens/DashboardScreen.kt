package com.example.ui.screens

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MainUiState
import com.example.ui.components.FpsGauge
import com.example.ui.components.FpsTunerCard
import com.example.ui.components.FrameHistoryChart
import com.example.ui.components.HardwareQuickStatsRow
import com.example.ui.components.HeroBanner
import com.example.ui.components.ShizukuStatusCard
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.GameLauncherHelper

@Composable
fun DashboardScreen(
    state: MainUiState,
    onToggle120HzLock: (Boolean) -> Unit,
    onRunRamBoost: () -> Unit,
    onDecreaseFps: () -> Unit,
    onIncreaseFps: () -> Unit,
    onSelectFps: (Int) -> Unit,
    onApplyFpsTuning: () -> Unit,
    onRequestShizukuPermission: (Activity) -> Unit,
    onCheckShizukuStatus: () -> Unit,
    availableFpsSteps: List<Int>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Hero Banner with Gaming Visual
        HeroBanner(
            display = state.displayTelemetry,
            hardware = state.hardwareSpecs
        )

        // Boost Status Feedback Toast/Card
        AnimatedVisibility(
            visible = state.boostMessage != null || state.isBoosting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(NeonCyan.copy(alpha = 0.12f))
                    .border(1.dp, NeonCyan.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                    .padding(12.dp)
                    .testTag("boost_status_banner")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (state.isBoosting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = NeonCyan,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            tint = ElectricLime,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = state.boostMessage ?: "Optimizing gaming pipeline...",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Gaming Mutual FPS Kam / Jyada Controller Card
        FpsTunerCard(
            fpsTuning = state.fpsTuning,
            shizukuState = state.shizukuState,
            availableSteps = availableFpsSteps,
            onDecreaseFps = onDecreaseFps,
            onIncreaseFps = onIncreaseFps,
            onSelectFps = onSelectFps,
            onApplyFpsTuning = onApplyFpsTuning
        )

        // Shizuku (Suzuki) Permission & Elevation Engine Card
        ShizukuStatusCard(
            shizukuState = state.shizukuState,
            targetFps = state.fpsTuning.targetFps,
            onRequestPermission = onRequestShizukuPermission,
            onCheckStatus = onCheckShizukuStatus
        )

        // Real-Time Frame Engine Gauge
        FpsGauge(
            fpsMetrics = state.fpsMetrics,
            maxRefreshRate = state.displayTelemetry.maxRefreshRate
        )

        // 120Hz Display Lock Switch Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(16.dp))
                .padding(14.dp)
                .testTag("display_lock_card")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "LOCK DISPLAY AT 120Hz",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(if (state.is120HzForceLocked) ElectricLime.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (state.is120HzForceLocked) "ACTIVE" else "AUTO",
                                color = if (state.is120HzForceLocked) ElectricLime else TextMuted,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Prevents system from downclocking display refresh rate to 60Hz during intensive gaming battles.",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }

                Switch(
                    checked = state.is120HzForceLocked,
                    onCheckedChange = onToggle120HzLock,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = DarkBackground,
                        checkedTrackColor = ElectricLime,
                        uncheckedThumbColor = TextMuted,
                        uncheckedTrackColor = DarkSurfaceVariant
                    ),
                    modifier = Modifier.testTag("switch_120hz_lock")
                )
            }
        }

        // Live Frame History Chart
        FrameHistoryChart(frameTimes = state.fpsMetrics.recentFrameTimes)

        // Quick Stats Row (RAM & Thermal)
        HardwareQuickStatsRow(hardware = state.hardwareSpecs)

        // Game Launch Card (BGMI)
        val bgmiGame = state.installedGames.firstOrNull { it.packageName == GameLauncherHelper.PACKAGE_BGMI }
        val isBgmiInstalled = bgmiGame?.isInstalled == true

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("game_launcher_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "BATTLEGROUNDS MOBILE INDIA",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = if (isBgmiInstalled) "Game Detected • Ready with ${state.fpsTuning.targetFps} FPS Profile" else "Not detected • Tap below to install official BGMI",
                            color = if (isBgmiInstalled) ElectricLime else TextMuted,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isBgmiInstalled) ElectricLime else Color(0xFFFFA000))
                    )
                }

                Button(
                    onClick = { GameLauncherHelper.launchGameOrStore(context, GameLauncherHelper.PACKAGE_BGMI) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("launch_game_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isBgmiInstalled) ElectricLime else NeonCyan
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBgmiInstalled) "LAUNCH BGMI (${state.fpsTuning.targetFps} FPS MODE)" else "GET BGMI ON GOOGLE PLAY",
                        color = DarkBackground,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        // Quick Optimization Utilities Grid
        Text(
            text = "SYSTEM GAMING UTILITIES",
            color = TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // RAM Boost Button
            OutlinedButton(
                onClick = onRunRamBoost,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("ram_boost_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurfaceCard)
            ) {
                Icon(
                    imageVector = Icons.Default.CleaningServices,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "RAM Boost",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Display Settings Shortcut
            OutlinedButton(
                onClick = { GameLauncherHelper.openDisplaySettings(context) },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("display_settings_button"),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkSurfaceCard)
            ) {
                Icon(
                    imageVector = Icons.Default.DisplaySettings,
                    contentDescription = null,
                    tint = ElectricLime,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Display 120Hz",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Developer Options Shortcut
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(12.dp))
                .clickable { GameLauncherHelper.openDeveloperSettings(context) }
                .padding(12.dp)
                .testTag("dev_settings_shortcut")
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Developer Peak Refresh Rate",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Shortcut to force peak refresh rate across all apps",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }
                Text(
                    text = "OPEN →",
                    color = NeonCyan,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
