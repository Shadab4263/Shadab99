package com.example.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ShizukuState
import com.example.model.ShizukuStatus
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurfaceBorder
import com.example.ui.theme.DarkSurfaceCard
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.ElectricLime
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.ShizukuController

@Composable
fun ShizukuStatusCard(
    shizukuState: ShizukuState,
    targetFps: Int,
    onRequestPermission: (Activity) -> Unit,
    onCheckStatus: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    val (badgeText, badgeColor, badgeIcon) = when (shizukuState.status) {
        ShizukuStatus.AUTHORIZED_ACTIVE -> Triple("SHIZUKU ACTIVE & AUTHORIZED", ElectricLime, Icons.Default.CheckCircle)
        ShizukuStatus.PERMISSION_REQUIRED -> Triple("PERMISSION REQUIRED", Color(0xFFFFA000), Icons.Default.Security)
        ShizukuStatus.INSTALLED_STOPPED -> Triple("SHIZUKU SERVICE STOPPED", Color(0xFFFFA000), Icons.Default.Warning)
        ShizukuStatus.NOT_INSTALLED -> Triple("SHIZUKU NOT INSTALLED", Color(0xFF94A3B8), Icons.Default.Terminal)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
            .padding(16.dp)
            .testTag("shizuku_status_card")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SHIZUKU (SUZUKI) ELEVATION ENGINE",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Rootless elevated ADB shell for system refresh rate",
                            color = TextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
            }

            // Status Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor.copy(alpha = 0.12f))
                    .border(1.dp, badgeColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = badgeText,
                        color = badgeColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                if (shizukuState.version > 0) {
                    Text(
                        text = "API v${shizukuState.version}",
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }

            // Description
            Text(
                text = if (shizukuState.isPermissionGranted) {
                    "Elevated permission granted. When you adjust FPS (kam or jyada), this tool directly overrides Android's system display refresh tables to force $targetFps FPS across all apps and games."
                } else {
                    "Shizuku allows this tool to run elevated system shell commands without root or a PC, forcing 120Hz/90Hz display refresh rates in system settings."
                },
                color = TextSecondary,
                fontSize = 11.sp,
                lineHeight = 15.sp
            )

            // Primary Action Button (Request Permission / Open Shizuku)
            if (shizukuState.status == ShizukuStatus.PERMISSION_REQUIRED && activity != null) {
                Button(
                    onClick = { onRequestPermission(activity) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("btn_request_shizuku_permission"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "GRANT SHIZUKU (SUZUKI) PERMISSION",
                        color = DarkBackground,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            } else if (!shizukuState.isBinderAlive) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { ShizukuController.openShizukuOrStore(context) },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_open_shizuku"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            tint = DarkBackground,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (shizukuState.isInstalled) "OPEN SHIZUKU" else "GET SHIZUKU",
                            color = DarkBackground,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onCheckStatus,
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .testTag("btn_check_shizuku"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkBackground),
                        border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
                    ) {
                        Text(
                            text = "CHECK STATUS",
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Copy ADB Command Fallback
            OutlinedButton(
                onClick = { ShizukuController.copyAdbCommandToClipboard(context, targetFps) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .testTag("btn_copy_adb_cmd"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkBackground),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkSurfaceBorder)
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "COPY MANUAL ADB COMMAND FOR $targetFps FPS",
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Last execution logs
            AnimatedVisibility(visible = shizukuState.lastExecutionOutput != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkBackground)
                        .padding(8.dp)
                ) {
                    Column {
                        Text(
                            text = "LAST SHIZUKU COMMAND OUTPUT:",
                            color = TextMuted,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = shizukuState.lastExecutionOutput ?: "",
                            color = if (shizukuState.executionSuccess == true) ElectricLime else Color(0xFFFF5252),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}
