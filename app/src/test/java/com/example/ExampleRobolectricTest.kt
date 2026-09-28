package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.CpuMetrics
import com.example.data.model.GpuMetrics
import com.example.data.model.HealthStatus
import com.example.data.model.RamMetrics
import com.example.data.model.SystemMetrics
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CorePulse", appName)
  }

  @Test
  fun `verify system metrics health evaluation`() {
    val normalMetrics = SystemMetrics(
        cpu = CpuMetrics(tempCelsius = 50f, usagePercent = 40f),
        gpu = GpuMetrics(tempCelsius = 55f, usagePercent = 60f),
        ram = RamMetrics(usedGb = 12f, totalGb = 32f, usagePercent = 37.5f)
    )
    assertEquals(HealthStatus.NORMAL, normalMetrics.overallHealth)
    assertTrue(normalMetrics.healthScore >= 90)

    val throttlingMetrics = SystemMetrics(
        cpu = CpuMetrics(tempCelsius = 98f, isThrottling = true),
        gpu = GpuMetrics(tempCelsius = 90f)
    )
    assertEquals(HealthStatus.CRITICAL, throttlingMetrics.overallHealth)
    assertTrue(throttlingMetrics.healthScore < 60)
  }
}
