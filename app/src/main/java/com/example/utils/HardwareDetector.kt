package com.example.utils

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.Build
import android.view.Display
import com.example.model.BgmiCompatibilityVerdict
import com.example.model.CompatibilityGrade
import com.example.model.DisplayModeInfo
import com.example.model.DisplayTelemetry
import com.example.model.HardwareSpecs
import kotlin.math.roundToInt

object HardwareDetector {

    fun getDisplayTelemetry(context: Context): DisplayTelemetry {
        val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

        val currentRate = defaultDisplay?.refreshRate ?: 60f
        val modesList = mutableListOf<DisplayModeInfo>()
        val supportedRates = mutableSetOf<Float>()
        var maxRate = currentRate

        val rawModes = defaultDisplay?.supportedModes ?: emptyArray()
        for (mode in rawModes) {
            val rate = (mode.refreshRate * 10).roundToInt() / 10f
            supportedRates.add(rate)
            if (rate > maxRate) {
                maxRate = rate
            }
            modesList.add(
                DisplayModeInfo(
                    modeId = mode.modeId,
                    width = mode.physicalWidth,
                    height = mode.physicalHeight,
                    refreshRate = rate,
                    isCurrent = defaultDisplay?.mode?.modeId == mode.modeId
                )
            )
        }

        val sortedRates = supportedRates.sorted()
        val is120HzSupported = sortedRates.any { it >= 115f }
        val is120HzActive = currentRate >= 115f

        val currentWidth = defaultDisplay?.mode?.physicalWidth ?: 1080
        val currentHeight = defaultDisplay?.mode?.physicalHeight ?: 2400

        return DisplayTelemetry(
            currentRefreshRate = (currentRate * 10).roundToInt() / 10f,
            maxRefreshRate = (maxRate * 10).roundToInt() / 10f,
            supportedRates = sortedRates,
            modes = modesList,
            resolution = "${currentWidth} x ${currentHeight}",
            is120HzSupported = is120HzSupported,
            is120HzActive = is120HzActive,
            preferredModeId = defaultDisplay?.mode?.modeId ?: 0
        )
    }

    fun getHardwareSpecs(context: Context): HardwareSpecs {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalRamGb = memoryInfo.totalMem / (1024f * 1024f * 1024f)
        val availRamGb = memoryInfo.availMem / (1024f * 1024f * 1024f)
        val usedRamGb = totalRamGb - availRamGb
        val usedPercent = if (totalRamGb > 0) ((usedRamGb / totalRamGb) * 100).toInt() else 50

        // Battery info
        val batteryFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val batteryStatus = context.registerReceiver(null, batteryFilter)
        val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: 80
        val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: 100
        val batteryPercent = if (level >= 0 && scale > 0) ((level.toFloat() / scale) * 100).toInt() else 80

        val tempTenths = batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
        val batteryTempC = tempTenths / 10f

        val status = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                status == BatteryManager.BATTERY_STATUS_FULL

        val soc = getSocDescription()

        return HardwareSpecs(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            socName = soc,
            totalRamGb = (totalRamGb * 10).roundToInt() / 10f,
            availableRamGb = (availRamGb * 10).roundToInt() / 10f,
            usedRamPercent = usedPercent.coerceIn(0, 100),
            batteryLevel = batteryPercent,
            batteryTempC = (batteryTempC * 10).roundToInt() / 10f,
            isCharging = isCharging,
            androidVersion = Build.VERSION.RELEASE,
            sdkInt = Build.VERSION.SDK_INT
        )
    }

    private fun getSocDescription(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val socModel = Build.SOC_MODEL
            if (socModel.isNotBlank() && socModel != "unknown") {
                socModel
            } else {
                "${Build.HARDWARE} (${Build.BOARD})"
            }
        } else {
            "${Build.HARDWARE} (${Build.BOARD})"
        }
    }

    fun evaluateBgmi120Fps(display: DisplayTelemetry, hardware: HardwareSpecs): BgmiCompatibilityVerdict {
        val has120HzDisplay = display.supportedRates.any { it >= 115f }
        val hasHighRam = hardware.totalRamGb >= 7.5f
        val hasGoodRam = hardware.totalRamGb >= 5.5f
        val isOptimalTemp = hardware.batteryTempC < 38.0f
        val isModernAndroid = hardware.sdkInt >= Build.VERSION_CODES.S

        var displayScore = if (has120HzDisplay) 100 else if (display.maxRefreshRate >= 85f) 75 else 50
        var cpuScore = 70
        val socLower = (hardware.socName + " " + hardware.model + " " + hardware.manufacturer).lowercase()

        // High end gaming SoC check
        val isFlagshipSoC = socLower.contains("8 gen") ||
                socLower.contains("888") ||
                socLower.contains("870") ||
                socLower.contains("865") ||
                socLower.contains("dimensity 9") ||
                socLower.contains("dimensity 8") ||
                socLower.contains("tensor g") ||
                socLower.contains("exynos 2")

        if (isFlagshipSoC) {
            cpuScore = 95
        } else if (socLower.contains("778") || socLower.contains("7 gen") || socLower.contains("dimensity 7") || socLower.contains("g99")) {
            cpuScore = 80
        }

        val ramScore = when {
            hasHighRam -> 100
            hasGoodRam -> 85
            hardware.totalRamGb >= 3.5f -> 65
            else -> 40
        }

        val thermalScore = when {
            hardware.batteryTempC < 34f -> 100
            hardware.batteryTempC < 38f -> 85
            hardware.batteryTempC < 42f -> 65
            else -> 45
        }

        val overallScore = ((displayScore * 0.4f) + (cpuScore * 0.3f) + (ramScore * 0.2f) + (thermalScore * 0.1f)).toInt()

        val factors = listOf(
            "120Hz Display Panel" to has120HzDisplay,
            "Flagship Gaming SoC" to (isFlagshipSoC || cpuScore >= 80),
            "High RAM Capacity (>= 6GB)" to hasGoodRam,
            "Thermal Headroom (< 38°C)" to isOptimalTemp,
            "Modern Android Frame Pacing" to isModernAndroid
        )

        val (grade, title, subtitle, graphics, fps) = when {
            has120HzDisplay && isFlagshipSoC && hasGoodRam -> {
                BgmiRatingTuple(
                    CompatibilityGrade.GRADE_120_ULTRA,
                    "120 FPS UNLOCKED & READY",
                    "Your device supports maximum 120 FPS in BGMI. Enable Smooth + 120 FPS in in-game settings.",
                    "Smooth",
                    "120 FPS"
                )
            }
            has120HzDisplay && hasGoodRam -> {
                BgmiRatingTuple(
                    CompatibilityGrade.GRADE_120_ULTRA,
                    "120Hz DISPLAY READY",
                    "Display hardware supports 120 FPS. Select Smooth Graphics to sustain peak framerates without thermal drops.",
                    "Smooth",
                    "120 FPS / 90 FPS"
                )
            }
            display.maxRefreshRate >= 85f -> {
                BgmiRatingTuple(
                    CompatibilityGrade.GRADE_90_HIGH,
                    "90 FPS HIGH REFRESH READY",
                    "Your screen supports 90Hz. In BGMI, choose Smooth + 90 FPS for optimal competitive smoothness.",
                    "Smooth",
                    "90 FPS"
                )
            }
            has120HzDisplay.not() -> {
                BgmiRatingTuple(
                    CompatibilityGrade.GRADE_60_EXTREME,
                    "60 FPS EXTREME CAPPED",
                    "Your device screen is limited to 60Hz. BGMI will run up to 60 FPS Extreme mode.",
                    "Smooth / Balanced",
                    "Extreme (60 FPS)"
                )
            }
            else -> {
                BgmiRatingTuple(
                    CompatibilityGrade.GRADE_LIMITED,
                    "PERFORMANCE OPTIMIZATION RECOMMENDED",
                    "Free up RAM and avoid high graphics to prevent thermal throttling.",
                    "Smooth",
                    "Ultra / Extreme"
                )
            }
        }

        return BgmiCompatibilityVerdict(
            grade = grade,
            title = title,
            subtitle = subtitle,
            overallScore = overallScore,
            displayScore = displayScore,
            cpuScore = cpuScore,
            ramScore = ramScore,
            thermalScore = thermalScore,
            recommendedGraphics = graphics,
            recommendedFrameRate = fps,
            keyFactors = factors
        )
    }

    private data class BgmiRatingTuple(
        val grade: CompatibilityGrade,
        val title: String,
        val subtitle: String,
        val graphics: String,
        val fps: String
    )
}
