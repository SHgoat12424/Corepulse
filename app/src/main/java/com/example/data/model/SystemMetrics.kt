package com.example.data.model

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Health status categorization based on temperatures, thermal headroom, and load.
 */
enum class HealthStatus(val label: String, val color: Long) {
    OPTIMAL("Optimal", 0xFF00E676),
    NORMAL("Normal", 0xFF00E5FF),
    WARM("Warm", 0xFFFFB300),
    ALERT("Hot / Throttling", 0xFFFF3D71),
    CRITICAL("Thermal Limit Exceeded", 0xFFFF1744)
}

/**
 * Active connection mode.
 */
enum class ConnectionMode {
    PC_NETWORK,
    SIMULATED_TEST_RIG,
    LOCAL_ANDROID_DEVICE
}

/**
 * Benchmark load presets for testing.
 */
enum class BenchmarkProfile(val title: String, val description: String) {
    IDLE("Idle / Desktop", "Low power, ambient temperatures (35-45°C)"),
    BALANCED_GAMING("Gaming (Cyberpunk 2077)", "Heavy GPU 85%+, moderate CPU 50%, 65-75°C"),
    RENDER_WORKLOAD("4K Video Export", "Heavy CPU 95%, GPU 70%, warm thermals"),
    CINEBENCH_STRESS("All-Core Stress Test", "Max CPU 100%, high thermal spike (85-95°C)"),
    THERMAL_THROTTLE_DEMO("Thermal Throttling Demo", "Exceeds 95°C limit to demonstrate diagnostics & alerts")
}

/**
 * Detailed CPU metrics.
 */
data class CpuMetrics(
    val name: String = "AMD Ryzen 7 7800X3D",
    val usagePercent: Float = 28.5f,
    val tempCelsius: Float = 52.0f,
    val clockGhz: Float = 4.85f,
    val powerWatts: Float = 58.2f,
    val coreCount: Int = 8,
    val threadCount: Int = 16,
    val coreLoads: List<Float> = listOf(35f, 22f, 48f, 15f, 60f, 31f, 18f, 25f),
    val isThrottling: Boolean = false
)

/**
 * Detailed GPU metrics.
 */
data class GpuMetrics(
    val name: String = "NVIDIA GeForce RTX 4080 Super",
    val usagePercent: Float = 64.0f,
    val tempCelsius: Float = 59.5f,
    val hotSpotTempCelsius: Float = 71.0f,
    val vramUsedGb: Float = 6.8f,
    val vramTotalGb: Float = 16.0f,
    val clockMhz: Int = 2550,
    val fanPercent: Float = 45.0f,
    val fanRpm: Int = 1350,
    val powerWatts: Float = 185.0f,
    val isThrottling: Boolean = false
) {
    val vramUsagePercent: Float
        get() = if (vramTotalGb > 0f) (vramUsedGb / vramTotalGb * 100f).coerceIn(0f, 100f) else 0f
}

/**
 * Detailed RAM metrics.
 */
data class RamMetrics(
    val usedGb: Float = 13.8f,
    val totalGb: Float = 32.0f,
    val usagePercent: Float = 43.1f,
    val swapUsedGb: Float = 2.4f,
    val swapTotalGb: Float = 12.0f,
    val speedMhz: Int = 6000,
    val memoryType: String = "DDR5"
)

/**
 * System and environmental telemetry.
 */
data class SystemMetrics(
    val timestamp: Long = System.currentTimeMillis(),
    val cpu: CpuMetrics = CpuMetrics(),
    val gpu: GpuMetrics = GpuMetrics(),
    val ram: RamMetrics = RamMetrics(),
    val osName: String = "Windows 11 Pro 64-bit",
    val uptimeSeconds: Long = 28450L,
    val netDownloadKbps: Float = 840.5f,
    val netUploadKbps: Float = 145.2f,
    val latencyMs: Int = 16,
    val connectionMode: ConnectionMode = ConnectionMode.SIMULATED_TEST_RIG
) {
    val maxTemperature: Float
        get() = maxOf(cpu.tempCelsius, gpu.tempCelsius, gpu.hotSpotTempCelsius)

    val overallHealth: HealthStatus
        get() = when {
            cpu.isThrottling || gpu.isThrottling || maxTemperature >= 92f -> HealthStatus.CRITICAL
            maxTemperature >= 84f || cpu.usagePercent >= 96f && gpu.usagePercent >= 96f -> HealthStatus.ALERT
            maxTemperature >= 72f -> HealthStatus.WARM
            maxTemperature >= 48f -> HealthStatus.NORMAL
            else -> HealthStatus.OPTIMAL
        }

    val healthScore: Int
        get() {
            var score = 100
            if (maxTemperature > 88f) score -= 30
            else if (maxTemperature > 80f) score -= 15
            else if (maxTemperature > 72f) score -= 5

            if (cpu.usagePercent > 95f) score -= 10
            if (gpu.usagePercent > 95f) score -= 10
            if (ram.usagePercent > 90f) score -= 15

            if (cpu.isThrottling || gpu.isThrottling) score -= 25
            return score.coerceIn(10, 100)
        }
}

/**
 * Historical metric point for smooth graphs.
 */
data class MetricPoint(
    val timestamp: Long,
    val cpuUsage: Float,
    val gpuUsage: Float,
    val ramUsage: Float,
    val cpuTemp: Float,
    val gpuTemp: Float
)

/**
 * Room entity for saved PC connection endpoints.
 */
@Entity(tableName = "connection_profiles")
data class ConnectionProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val hostIp: String,
    val port: Int = 8085,
    val path: String = "/data.json",
    val isDefault: Boolean = false,
    val lastConnected: Long = System.currentTimeMillis()
)

/**
 * Room entity for saved diagnostic session reports.
 */
@Entity(tableName = "diagnostic_sessions")
data class DiagnosticSession(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val sessionName: String,
    val maxCpuTemp: Float,
    val maxGpuTemp: Float,
    val avgCpuUsage: Float,
    val avgGpuUsage: Float,
    val peakRamGb: Float,
    val healthScore: Int,
    val throttlingDetected: Boolean,
    val notes: String = ""
)
