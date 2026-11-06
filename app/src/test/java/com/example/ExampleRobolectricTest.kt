package com.example

import com.example.model.CompatibilityGrade
import com.example.model.DisplayTelemetry
import com.example.model.HardwareSpecs
import com.example.utils.HardwareDetector
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleRobolectricTest {

  @Test
  fun testEvaluateBgmi120Fps_with120HzDisplayAndFlagshipSoC() {
    val display = DisplayTelemetry(
      currentRefreshRate = 120f,
      maxRefreshRate = 120f,
      supportedRates = listOf(60f, 90f, 120f),
      is120HzSupported = true,
      is120HzActive = true
    )
    val hardware = HardwareSpecs(
      model = "Galaxy S24",
      manufacturer = "Samsung",
      socName = "Snapdragon 8 Gen 3",
      totalRamGb = 12f,
      availableRamGb = 7.5f,
      batteryTempC = 31.0f
    )
    val verdict = HardwareDetector.evaluateBgmi120Fps(display, hardware)
    assertEquals(CompatibilityGrade.GRADE_120_ULTRA, verdict.grade)
    assertTrue(verdict.overallScore >= 90)
    assertEquals("Smooth", verdict.recommendedGraphics)
    assertEquals("120 FPS", verdict.recommendedFrameRate)
  }
}
