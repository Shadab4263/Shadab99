package com.example.utils

import android.view.Choreographer
import com.example.model.FpsMetrics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class FpsTracker {
    private val _fpsMetrics = MutableStateFlow(FpsMetrics())
    val fpsMetrics: StateFlow<FpsMetrics> = _fpsMetrics.asStateFlow()

    private var isTracking = false
    private var lastFrameTimeNanos: Long = 0L
    private val frameDeltas = ArrayDeque<Long>(60)
    private var frameCountSinceLastUpdate = 0
    private var lastMetricsUpdateTime = System.currentTimeMillis()

    private val frameCallback = object : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            if (!isTracking) return

            if (lastFrameTimeNanos != 0L) {
                val deltaNanos = frameTimeNanos - lastFrameTimeNanos
                if (deltaNanos > 0) {
                    if (frameDeltas.size >= 60) {
                        frameDeltas.removeFirst()
                    }
                    frameDeltas.addLast(deltaNanos)
                    frameCountSinceLastUpdate++
                }
            }
            lastFrameTimeNanos = frameTimeNanos

            val now = System.currentTimeMillis()
            if (now - lastMetricsUpdateTime >= 250 && frameDeltas.isNotEmpty()) {
                computeAndPublishMetrics()
                lastMetricsUpdateTime = now
                frameCountSinceLastUpdate = 0
            }

            if (isTracking) {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    fun start() {
        if (isTracking) return
        isTracking = true
        lastFrameTimeNanos = 0L
        frameDeltas.clear()
        Choreographer.getInstance().postFrameCallback(frameCallback)
    }

    fun stop() {
        isTracking = false
        Choreographer.getInstance().removeFrameCallback(frameCallback)
    }

    private fun computeAndPublishMetrics() {
        val count = frameDeltas.size
        if (count == 0) return

        // Convert deltas in nanos to ms
        val frameTimesMs = frameDeltas.map { it / 1_000_000f }
        val avgFrameTimeMs = frameTimesMs.average().toFloat()

        // Instantaneous FPS calculation
        val currentFps = if (avgFrameTimeMs > 0) (1000f / avgFrameTimeMs).roundToInt() else 60

        // Frame stability: variance/deviation from mean
        val sortedTimes = frameTimesMs.sorted()
        val onePercentLowIndex = (count * 0.95f).toInt().coerceIn(0, count - 1)
        val onePercentLowTimeMs = sortedTimes[onePercentLowIndex]
        val minFps = if (onePercentLowTimeMs > 0) (1000f / onePercentLowTimeMs).roundToInt() else currentFps
        val maxFps = if (sortedTimes.first() > 0) (1000f / sortedTimes.first()).roundToInt() else currentFps

        // Stability: percentage of frames within 20% of target
        val expectedDelta = 1000f / currentFps.coerceAtLeast(30)
        val stableFrames = frameTimesMs.count { kotlin.math.abs(it - expectedDelta) < (expectedDelta * 0.35f) }
        val stability = ((stableFrames.toFloat() / count) * 100).toInt().coerceIn(50, 100)

        // Keep last 40 frames for chart
        val recentChartFrames = if (frameTimesMs.size > 40) {
            frameTimesMs.takeLast(40)
        } else {
            frameTimesMs
        }

        _fpsMetrics.value = FpsMetrics(
            currentFps = currentFps,
            averageFps = (1000f / avgFrameTimeMs * 10).roundToInt() / 10f,
            minFps = minFps.coerceAtMost(currentFps),
            maxFps = maxFps.coerceAtLeast(currentFps),
            frameTimeMs = (avgFrameTimeMs * 10).roundToInt() / 10f,
            frameStability = stability,
            recentFrameTimes = recentChartFrames
        )
    }
}
