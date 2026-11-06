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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberPink
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
fun GuideAntiBanScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column {
            Text(
                text = "OFFICIAL BGMI 120 FPS SETUP",
                color = ElectricLime,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp
            )
            Text(
                text = "Safe Activation & Anti-Ban Guide",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Anti-Ban Security Card (Critical)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF161424))
                .border(1.dp, ElectricLime.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("antiban_security_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Safe",
                        tint = ElectricLime,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "100% SAFE & BAN-PROOF",
                        color = ElectricLime,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "This application operates exclusively at the Android system level (Display refresh rate modes, Choreographer frame pacing, memory optimization). It DOES NOT modify any game files (active.sav, UserCustom.ini) and preserves 100% game client integrity.",
                    color = TextPrimary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberPink.copy(alpha = 0.12f))
                        .border(1.dp, CyberPink.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = CyberPink,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "NEVER use config file injectors or illegal active.sav tools. Krafton's anti-cheat scans game directory hashes and issues 10-year account bans.",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Step-by-Step Activation Guide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("activation_steps_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "HOW TO ENABLE 120 FPS IN BGMI",
                    color = NeonCyan,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )

                StepItem(
                    stepNumber = "1",
                    title = "Launch BGMI & Open Settings",
                    description = "Tap the Gear icon in the bottom-right corner of the lobby screen to access in-game settings."
                )

                StepItem(
                    stepNumber = "2",
                    title = "Select 'Graphics & Audio'",
                    description = "Navigate to the Graphics tab. Look at the top graphics preset bar."
                )

                StepItem(
                    stepNumber = "3",
                    title = "Set Graphics Preset to 'Smooth'",
                    description = "Crucial: BGMI 120 FPS is strictly enabled only when Graphics is set to Smooth. Higher presets (HDR/Ultra HD) cap at 45-60 FPS."
                )

                StepItem(
                    stepNumber = "4",
                    title = "Select Frame Rate: '120 FPS'",
                    description = "Scroll the Frame Rate slider to the far right. Tap '120 FPS' (or '90 FPS' if 120 is still in regional device rollout for your chipset)."
                )

                StepItem(
                    stepNumber = "5",
                    title = "Disable 'Auto-Adjust Graphics'",
                    description = "Scroll to the bottom of Graphics settings and turn OFF 'Auto-Adjust Graphics'. If enabled, BGMI drops your framerate back to 60 FPS whenever the device battery reaches 37°C."
                )

                StepItem(
                    stepNumber = "6",
                    title = "Lock Display to 120Hz in This App",
                    description = "Return to Dashboard and ensure 'Lock Display at 120Hz' is turned ON to keep your screen panel running at full speed."
                )
            }
        }

        // Pro Tips for Sustained 120 FPS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, DarkSurfaceBorder, RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("pro_tips_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TipsAndUpdates,
                        contentDescription = null,
                        tint = ElectricLime,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PRO TIPS FOR ZERO FPS DROPS",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TipItem(text = "Remove heavy phone cases during prolonged BGMI sessions to dissipate heat quickly.")
                TipItem(text = "Do not game while charging with fast chargers (30W+) to prevent thermal throttling.")
                TipItem(text = "Enable Device Game Turbo / Performance Mode in Android Settings for maximum CPU core clocks.")
                TipItem(text = "Turn on Do Not Disturb mode to avoid floating notification lag during close-quarter combat.")
            }
        }

        // Shizuku (Suzuki) Elevation Setup Guide
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(DarkSurfaceCard)
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
                .padding(16.dp)
                .testTag("shizuku_guide_card")
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "HOW TO ACTIVATE SHIZUKU (SUZUKI) PERMISSION",
                        color = NeonCyan,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                }

                Text(
                    text = "Shizuku enables elevated system commands without root via Android's Wireless Debugging:",
                    color = TextSecondary,
                    fontSize = 11.sp
                )

                TipItem(text = "Step 1: Install 'Shizuku' from Google Play Store or GitHub.")
                TipItem(text = "Step 2: Enable 'Developer Options' and turn on 'Wireless Debugging' in Android Settings.")
                TipItem(text = "Step 3: Open Shizuku -> Tap 'Pairing' with Wireless Debugging (enter pairing code from notification).")
                TipItem(text = "Step 4: Tap 'Start' in Shizuku. Once running, return here and tap 'GRANT SHIZUKU (SUZUKI) PERMISSION'.")
                TipItem(text = "Step 5: You can now tune FPS Kam / Jyada (60, 90, 120, 144) and it will force global system refresh rate.")
            }
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
private fun StepItem(
    stepNumber: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(NeonCyan.copy(alpha = 0.15f))
                .border(1.dp, NeonCyan, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stepNumber,
                color = NeonCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )
        }
    }
}

@Composable
private fun TipItem(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(ElectricLime)
        )
        Text(
            text = text,
            color = TextSecondary,
            fontSize = 11.sp,
            lineHeight = 15.sp
        )
    }
}
