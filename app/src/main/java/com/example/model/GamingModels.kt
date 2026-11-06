package com.example.model

data class DisplayModeInfo(
    val modeId: Int,
    val width: Int,
    val height: Int,
    val refreshRate: Float,
    val isCurrent: Boolean = false
)

data class DisplayTelemetry(
    val currentRefreshRate: Float = 60f,
    val maxRefreshRate: Float = 60f,
    val supportedRates: List<Float> = emptyList(),
    val modes: List<DisplayModeInfo> = emptyList(),
    val resolution: String = "1080 x 2400",
    val is120HzSupported: Boolean = false,
    val is120HzActive: Boolean = false,
    val preferredModeId: Int = 0
)

data class FpsMetrics(
    val currentFps: Int = 60,
    val averageFps: Float = 60f,
    val minFps: Int = 55,
    val maxFps: Int = 60,
    val frameTimeMs: Float = 16.6f,
    val frameStability: Int = 98, // percentage 0-100
    val recentFrameTimes: List<Float> = emptyList() // Last 40 frames in ms
)

data class HardwareSpecs(
    val model: String = "Device",
    val manufacturer: String = "Brand",
    val socName: String = "Unknown Chipset",
    val totalRamGb: Float = 8f,
    val availableRamGb: Float = 4.2f,
    val usedRamPercent: Int = 48,
    val batteryLevel: Int = 85,
    val batteryTempC: Float = 33.5f,
    val isCharging: Boolean = false,
    val androidVersion: String = "14",
    val sdkInt: Int = 34
)

enum class CompatibilityGrade {
    GRADE_120_ULTRA, // Capable of rock solid 120 FPS
    GRADE_90_HIGH,   // 90 FPS supported
    GRADE_60_EXTREME,// 60 FPS Extreme supported
    GRADE_LIMITED    // 30-45 FPS display limited
}

data class BgmiCompatibilityVerdict(
    val grade: CompatibilityGrade,
    val title: String,
    val subtitle: String,
    val overallScore: Int, // 0 - 100
    val displayScore: Int,
    val cpuScore: Int,
    val ramScore: Int,
    val thermalScore: Int,
    val recommendedGraphics: String,
    val recommendedFrameRate: String,
    val keyFactors: List<Pair<String, Boolean>>
)

data class TouchLatencyMetrics(
    val samplingRateHz: Int = 240,
    val touchLatencyMs: Float = 4.2f,
    val activeTouches: Int = 0,
    val jitterMs: Float = 0.5f,
    val lastTouchX: Float = 0f,
    val lastTouchY: Float = 0f
)

enum class ShizukuStatus {
    NOT_INSTALLED,
    INSTALLED_STOPPED,
    PERMISSION_REQUIRED,
    AUTHORIZED_ACTIVE
}

data class ShizukuState(
    val status: ShizukuStatus = ShizukuStatus.NOT_INSTALLED,
    val isInstalled: Boolean = false,
    val isBinderAlive: Boolean = false,
    val isPermissionGranted: Boolean = false,
    val version: Int = 0,
    val lastExecutedCommand: String? = null,
    val lastExecutionOutput: String? = null,
    val executionSuccess: Boolean? = null
)

data class FpsTuningProfile(
    val targetFps: Int = 120,
    val isDynamicAdjustmentEnabled: Boolean = true,
    val appliedRefreshRateHz: Int = 120,
    val modeName: String = "Ultra Esports (120 FPS)"
)
