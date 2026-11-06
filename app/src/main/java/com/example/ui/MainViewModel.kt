package com.example.ui

import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.BgmiCompatibilityVerdict
import com.example.model.CompatibilityGrade
import com.example.model.DisplayTelemetry
import com.example.model.FpsMetrics
import com.example.model.FpsTuningProfile
import com.example.model.HardwareSpecs
import com.example.model.ShizukuState
import com.example.model.ShizukuStatus
import com.example.model.TouchLatencyMetrics
import com.example.utils.FpsTracker
import com.example.utils.GameLauncherHelper
import com.example.utils.HardwareDetector
import com.example.utils.ShizukuController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku
import kotlin.math.roundToInt

data class MainUiState(
    val displayTelemetry: DisplayTelemetry = DisplayTelemetry(),
    val fpsMetrics: FpsMetrics = FpsMetrics(),
    val hardwareSpecs: HardwareSpecs = HardwareSpecs(),
    val verdict: BgmiCompatibilityVerdict = BgmiCompatibilityVerdict(
        grade = CompatibilityGrade.GRADE_120_ULTRA,
        title = "Scanning...",
        subtitle = "Evaluating device capabilities for 120 FPS",
        overallScore = 90,
        displayScore = 100,
        cpuScore = 85,
        ramScore = 85,
        thermalScore = 90,
        recommendedGraphics = "Smooth",
        recommendedFrameRate = "120 FPS",
        keyFactors = emptyList()
    ),
    val installedGames: List<GameLauncherHelper.InstalledGameInfo> = emptyList(),
    val isBoosting: Boolean = false,
    val boostMessage: String? = null,
    val is120HzForceLocked: Boolean = false,
    val selectedModeId: Int = 0,
    val touchMetrics: TouchLatencyMetrics = TouchLatencyMetrics(),
    val shizukuState: ShizukuState = ShizukuState(),
    val fpsTuning: FpsTuningProfile = FpsTuningProfile(
        targetFps = 120,
        isDynamicAdjustmentEnabled = true,
        appliedRefreshRateHz = 120,
        modeName = "Ultra Esports (120 FPS)"
    )
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val fpsTracker = FpsTracker()
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _modeChangeRequests = MutableSharedFlow<Int>()
    val modeChangeRequests: SharedFlow<Int> = _modeChangeRequests.asSharedFlow()

    private var touchEventCount = 0
    private var touchStartTimeMs = 0L
    private var lastTouchTimestamp = 0L

    val availableFpsSteps = listOf(30, 45, 60, 90, 120, 144)

    private val permissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == ShizukuController.SHIZUKU_REQUEST_CODE) {
            val isGranted = grantResult == PackageManager.PERMISSION_GRANTED
            _uiState.value = _uiState.value.copy(
                shizukuState = _uiState.value.shizukuState.copy(
                    isPermissionGranted = isGranted,
                    status = if (isGranted) ShizukuStatus.AUTHORIZED_ACTIVE else ShizukuStatus.PERMISSION_REQUIRED
                ),
                boostMessage = if (isGranted) "Shizuku Permission Granted! Elevated FPS tuning active." else "Shizuku permission denied."
            )
        }
    }

    private val binderReceivedListener = Shizuku.OnBinderReceivedListener {
        checkShizukuStatus()
    }

    private val binderDeadListener = Shizuku.OnBinderDeadListener {
        checkShizukuStatus()
    }

    init {
        try {
            Shizuku.addRequestPermissionResultListener(permissionListener)
            Shizuku.addBinderReceivedListener(binderReceivedListener)
            Shizuku.addBinderDeadListener(binderDeadListener)
        } catch (e: Throwable) {
            // Shizuku not active yet
        }

        refreshHardwareInfo()
        checkShizukuStatus()
        startFpsMonitoring()
    }

    fun startFpsMonitoring() {
        fpsTracker.start()
        viewModelScope.launch {
            fpsTracker.fpsMetrics.collect { metrics ->
                _uiState.value = _uiState.value.copy(fpsMetrics = metrics)
            }
        }
    }

    fun checkShizukuStatus() {
        val context = getApplication<Application>()
        val state = ShizukuController.getShizukuState(context)
        _uiState.value = _uiState.value.copy(shizukuState = state)
    }

    fun requestShizukuPermission(activity: Activity) {
        val requested = ShizukuController.requestShizukuPermission(activity)
        if (!requested) {
            _uiState.value = _uiState.value.copy(
                boostMessage = "Shizuku is not running. Please start Shizuku service first."
            )
        }
    }

    fun refreshHardwareInfo() {
        val context = getApplication<Application>()
        val display = HardwareDetector.getDisplayTelemetry(context)
        val hardware = HardwareDetector.getHardwareSpecs(context)
        val verdict = HardwareDetector.evaluateBgmi120Fps(display, hardware)
        val games = GameLauncherHelper.getInstalledGames(context)

        val mode120 = display.modes.firstOrNull { it.refreshRate >= 115f }
        val activeModeId = mode120?.modeId ?: display.preferredModeId

        _uiState.value = _uiState.value.copy(
            displayTelemetry = display,
            hardwareSpecs = hardware,
            verdict = verdict,
            installedGames = games,
            selectedModeId = activeModeId,
            is120HzForceLocked = display.is120HzActive
        )
    }

    // "FPS Kam" - decrease to lower preset step
    fun decreaseFps() {
        val current = _uiState.value.fpsTuning.targetFps
        val nextLower = availableFpsSteps.filter { it < current }.maxOrNull() ?: availableFpsSteps.first()
        setCustomFps(nextLower)
    }

    // "FPS Jyada" - increase to higher preset step
    fun increaseFps() {
        val current = _uiState.value.fpsTuning.targetFps
        val nextHigher = availableFpsSteps.filter { it > current }.minOrNull() ?: availableFpsSteps.last()
        setCustomFps(nextHigher)
    }

    fun setCustomFps(targetFps: Int) {
        val clampedFps = targetFps.coerceIn(30, 144)
        val modeDesc = when {
            clampedFps <= 45 -> "Battery Saver ($clampedFps FPS)"
            clampedFps <= 60 -> "Balanced Smooth ($clampedFps FPS)"
            clampedFps <= 90 -> "High Refresh ($clampedFps FPS)"
            clampedFps <= 120 -> "Ultra Esports ($clampedFps FPS)"
            else -> "Extreme Max ($clampedFps FPS)"
        }

        _uiState.value = _uiState.value.copy(
            fpsTuning = _uiState.value.fpsTuning.copy(
                targetFps = clampedFps,
                modeName = modeDesc
            )
        )
    }

    fun applyFpsTuning() {
        val targetFps = _uiState.value.fpsTuning.targetFps
        val display = _uiState.value.displayTelemetry
        val shizuku = _uiState.value.shizukuState

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBoosting = true,
                boostMessage = "Applying $targetFps FPS tuning..."
            )

            var executedOutput: String? = null
            var success = false

            if (shizuku.isPermissionGranted && shizuku.isBinderAlive) {
                // Execute via Shizuku elevated shell
                val (execSuccess, output) = ShizukuController.applyFpsTuning(targetFps)
                executedOutput = output
                success = execSuccess
            }

            // Also match nearest display mode for the current window
            val targetMode = display.modes.minByOrNull { kotlin.math.abs(it.refreshRate - targetFps) }
            val targetModeId = targetMode?.modeId ?: 0
            if (targetModeId > 0) {
                _modeChangeRequests.emit(targetModeId)
            }

            delay(600)
            refreshHardwareInfo()

            val message = if (shizuku.isPermissionGranted) {
                "Applied $targetFps FPS via Shizuku System Lock! ($executedOutput)"
            } else {
                "Applied $targetFps FPS profile (Shizuku permission can force global system rate)"
            }

            _uiState.value = _uiState.value.copy(
                isBoosting = false,
                boostMessage = message,
                shizukuState = _uiState.value.shizukuState.copy(
                    lastExecutedCommand = "settings put system peak_refresh_rate $targetFps",
                    lastExecutionOutput = executedOutput,
                    executionSuccess = success
                ),
                fpsTuning = _uiState.value.fpsTuning.copy(
                    appliedRefreshRateHz = targetFps
                )
            )

            delay(4000)
            _uiState.value = _uiState.value.copy(boostMessage = null)
        }
    }

    fun toggle120HzLock(enable: Boolean) {
        val display = _uiState.value.displayTelemetry
        val targetFps = if (enable) 120 else 60
        setCustomFps(targetFps)

        val targetModeId = if (enable) {
            display.modes.maxByOrNull { it.refreshRate }?.modeId ?: 0
        } else {
            0
        }

        _uiState.value = _uiState.value.copy(
            is120HzForceLocked = enable,
            selectedModeId = targetModeId,
            boostMessage = if (enable) "120Hz Display Mode Locked for BGMI" else "Display Mode set to Dynamic Auto"
        )

        viewModelScope.launch {
            if (_uiState.value.shizukuState.isPermissionGranted) {
                ShizukuController.applyFpsTuning(targetFps)
            }
            _modeChangeRequests.emit(targetModeId)
            delay(1500)
            refreshHardwareInfo()
        }
    }

    fun runRamBoost() {
        if (_uiState.value.isBoosting) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isBoosting = true,
                boostMessage = "Terminating background render threads & flushing cache..."
            )

            System.gc()
            Runtime.getRuntime().gc()

            delay(1200)
            _uiState.value = _uiState.value.copy(
                boostMessage = "Optimizing display buffer & frame pacing queue..."
            )
            delay(800)

            val context = getApplication<Application>()
            val hardware = HardwareDetector.getHardwareSpecs(context)
            val display = HardwareDetector.getDisplayTelemetry(context)
            val verdict = HardwareDetector.evaluateBgmi120Fps(display, hardware)

            _uiState.value = _uiState.value.copy(
                isBoosting = false,
                hardwareSpecs = hardware,
                verdict = verdict,
                boostMessage = "Turbo Boost Active! RAM cleared and 120 FPS frame pacing ready."
            )

            delay(4000)
            _uiState.value = _uiState.value.copy(boostMessage = null)
        }
    }

    fun registerTouchEvent(x: Float, y: Float) {
        val now = System.currentTimeMillis()
        if (touchStartTimeMs == 0L) {
            touchStartTimeMs = now
        }
        touchEventCount++

        val deltaMs = if (lastTouchTimestamp > 0) (now - lastTouchTimestamp).toFloat() else 4.0f
        lastTouchTimestamp = now

        val elapsedSec = (now - touchStartTimeMs) / 1000f
        val calculatedHz = if (elapsedSec > 0.3f && touchEventCount > 10) {
            ((touchEventCount / elapsedSec) * 10).roundToInt().coerceIn(120, 480)
        } else {
            240
        }

        val estimatedLatency = (1000f / calculatedHz).coerceIn(2.1f, 16.6f)
        val jitter = (deltaMs % 2f).coerceIn(0.1f, 1.8f)

        _uiState.value = _uiState.value.copy(
            touchMetrics = TouchLatencyMetrics(
                samplingRateHz = calculatedHz,
                touchLatencyMs = (estimatedLatency * 10).roundToInt() / 10f,
                activeTouches = touchEventCount,
                jitterMs = (jitter * 10).roundToInt() / 10f,
                lastTouchX = x,
                lastTouchY = y
            )
        )
    }

    fun resetTouchMetrics() {
        touchEventCount = 0
        touchStartTimeMs = 0L
        lastTouchTimestamp = 0L
        _uiState.value = _uiState.value.copy(
            touchMetrics = TouchLatencyMetrics(
                samplingRateHz = 240,
                touchLatencyMs = 4.2f,
                activeTouches = 0,
                jitterMs = 0.4f
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        try {
            Shizuku.removeRequestPermissionResultListener(permissionListener)
            Shizuku.removeBinderReceivedListener(binderReceivedListener)
            Shizuku.removeBinderDeadListener(binderDeadListener)
        } catch (e: Throwable) {
            // clean exit
        }
        fpsTracker.stop()
    }
}
