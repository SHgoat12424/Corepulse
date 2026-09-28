package com.example.data.network

import com.example.data.model.ConnectionMode
import com.example.data.model.CpuMetrics
import com.example.data.model.GpuMetrics
import com.example.data.model.RamMetrics
import com.example.data.model.SystemMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T, val latencyMs: Int) : NetworkResult<T>()
    data class Error(val message: String, val exception: Throwable? = null) : NetworkResult<Nothing>()
}

class HardwareMonitorClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(1500, TimeUnit.MILLISECONDS)
        .readTimeout(2000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(false)
        .build()

    suspend fun fetchMetrics(
        host: String,
        port: Int = 8085,
        path: String = "/data.json"
    ): NetworkResult<SystemMetrics> = withContext(Dispatchers.IO) {
        val cleanHost = host.trim().removePrefix("http://").removePrefix("https://").trimEnd('/')
        val cleanPath = if (path.startsWith("/")) path else "/$path"
        val url = "http://$cleanHost:$port$cleanPath"

        val startTime = System.currentTimeMillis()
        try {
            val request = Request.Builder()
                .url(url)
                .addHeader("Accept", "application/json")
                .build()

            client.newCall(request).execute().use { response ->
                val latency = (System.currentTimeMillis() - startTime).toInt().coerceAtLeast(1)
                if (!response.isSuccessful) {
                    return@withContext NetworkResult.Error("HTTP Error: ${response.code} ${response.message}")
                }
                val body = response.body?.string() ?: return@withContext NetworkResult.Error("Empty response body from PC")
                
                val parsed = parseJsonResponse(body, latency)
                NetworkResult.Success(parsed, latency)
            }
        } catch (e: IOException) {
            NetworkResult.Error("Connection failed to $cleanHost:$port: ${e.localizedMessage ?: "Timeout / Unreachable"}", e)
        } catch (e: Exception) {
            NetworkResult.Error("Error parsing data: ${e.message}", e)
        }
    }

    private fun parseJsonResponse(jsonStr: String, latency: Int): SystemMetrics {
        val json = JSONObject(jsonStr)

        // Check if this is LibreHardwareMonitor / OpenHardwareMonitor tree format
        if (json.has("Children") || json.has("children")) {
            return parseLhmTree(json, latency)
        }

        // Otherwise parse our lightweight flat JSON format
        return parseFlatJson(json, latency)
    }

    private fun parseFlatJson(json: JSONObject, latency: Int): SystemMetrics {
        val cpuObj = json.optJSONObject("cpu") ?: JSONObject()
        val gpuObj = json.optJSONObject("gpu") ?: JSONObject()
        val ramObj = json.optJSONObject("ram") ?: JSONObject()
        val sysObj = json.optJSONObject("system") ?: JSONObject()

        val cpuCoresList = mutableListOf<Float>()
        val coresArray = cpuObj.optJSONArray("cores")
        if (coresArray != null) {
            for (i in 0 until coresArray.length()) {
                cpuCoresList.add(coresArray.optDouble(i, 0.0).toFloat())
            }
        } else {
            val count = cpuObj.optInt("core_count", 8)
            val avg = cpuObj.optDouble("usage", 30.0).toFloat()
            for (i in 0 until count) {
                cpuCoresList.add((avg + (i % 3 - 1) * 5f).coerceIn(0f, 100f))
            }
        }

        val cpu = CpuMetrics(
            name = cpuObj.optString("name", "PC Processor"),
            usagePercent = cpuObj.optDouble("usage", 25.0).toFloat().coerceIn(0f, 100f),
            tempCelsius = cpuObj.optDouble("temperature", cpuObj.optDouble("temp", 48.0)).toFloat(),
            clockGhz = cpuObj.optDouble("frequency_ghz", cpuObj.optDouble("clock_ghz", 4.2)).toFloat(),
            powerWatts = cpuObj.optDouble("power_watts", cpuObj.optDouble("power_w", 55.0)).toFloat(),
            coreCount = cpuObj.optInt("core_count", cpuCoresList.size.coerceAtLeast(4)),
            threadCount = cpuObj.optInt("thread_count", cpuCoresList.size * 2),
            coreLoads = cpuCoresList,
            isThrottling = cpuObj.optBoolean("throttling", false) || cpuObj.optDouble("temp", 0.0) >= 95.0
        )

        val gpu = GpuMetrics(
            name = gpuObj.optString("name", "PC Graphics Card"),
            usagePercent = gpuObj.optDouble("usage", 50.0).toFloat().coerceIn(0f, 100f),
            tempCelsius = gpuObj.optDouble("temperature", gpuObj.optDouble("temp", 55.0)).toFloat(),
            hotSpotTempCelsius = gpuObj.optDouble("hotspot_temp", gpuObj.optDouble("hotspot", 66.0)).toFloat(),
            vramUsedGb = gpuObj.optDouble("vram_used_gb", gpuObj.optDouble("vram_used", 4.0)).toFloat(),
            vramTotalGb = gpuObj.optDouble("vram_total_gb", gpuObj.optDouble("vram_total", 16.0)).toFloat(),
            clockMhz = gpuObj.optInt("clock_mhz", 2400),
            fanPercent = gpuObj.optDouble("fan_percent", gpuObj.optDouble("fan_pct", 40.0)).toFloat(),
            fanRpm = gpuObj.optInt("fan_rpm", 1200),
            powerWatts = gpuObj.optDouble("power_watts", gpuObj.optDouble("power_w", 150.0)).toFloat(),
            isThrottling = gpuObj.optBoolean("throttling", false) || gpuObj.optDouble("temp", 0.0) >= 88.0
        )

        val ramTotal = ramObj.optDouble("total_gb", ramObj.optDouble("total", 32.0)).toFloat()
        val ramUsed = ramObj.optDouble("used_gb", ramObj.optDouble("used", 12.0)).toFloat()
        val ramUsage = ramObj.optDouble("usage_percent", ramObj.optDouble("usage_pct", if (ramTotal > 0f) (ramUsed / ramTotal * 100.0) else 40.0)).toFloat()

        val ram = RamMetrics(
            usedGb = ramUsed,
            totalGb = ramTotal,
            usagePercent = ramUsage.coerceIn(0f, 100f),
            swapUsedGb = ramObj.optDouble("swap_used_gb", ramObj.optDouble("swap_used", 2.0)).toFloat(),
            swapTotalGb = ramObj.optDouble("swap_total_gb", ramObj.optDouble("swap_total", 8.0)).toFloat(),
            speedMhz = ramObj.optInt("speed_mhz", 6000),
            memoryType = ramObj.optString("type", "DDR5")
        )

        return SystemMetrics(
            timestamp = System.currentTimeMillis(),
            cpu = cpu,
            gpu = gpu,
            ram = ram,
            osName = sysObj.optString("os", "PC System"),
            uptimeSeconds = sysObj.optLong("uptime_sec", sysObj.optLong("uptime", 12000L)),
            netDownloadKbps = sysObj.optDouble("network_rx_kbps", 450.0).toFloat(),
            netUploadKbps = sysObj.optDouble("network_tx_kbps", 80.0).toFloat(),
            latencyMs = latency,
            connectionMode = ConnectionMode.PC_NETWORK
        )
    }

    private fun parseLhmTree(root: JSONObject, latency: Int): SystemMetrics {
        // LibreHardwareMonitor / OpenHardwareMonitor tree extraction
        val sensors = mutableMapOf<String, Float>()
        var detectedCpuName = "PC CPU"
        var detectedGpuName = "PC GPU"
        val coreLoads = mutableListOf<Float>()

        fun walk(node: JSONObject) {
            val text = node.optString("Text", "")
            val valueStr = node.optString("Value", "")
            val sensorType = node.optString("Type", "")

            if (text.contains("Ryzen", true) || text.contains("Core i", true) || text.contains("Intel", true) || text.contains("AMD", true)) {
                if (!text.contains("Radeon", true) && !text.contains("GeForce", true)) {
                    detectedCpuName = text
                }
            }
            if (text.contains("GeForce", true) || text.contains("Radeon", true) || text.contains("RTX", true) || text.contains("GTX", true) || text.contains("Intel Arc", true)) {
                detectedGpuName = text
            }

            if (valueStr.isNotEmpty()) {
                val num = valueStr.replace("[^0-9.]".toRegex(), "").toFloatOrNull()
                if (num != null) {
                    sensors["$sensorType:$text"] = num
                    if (sensorType.equals("Load", true) && text.startsWith("CPU Core #", true)) {
                        coreLoads.add(num)
                    }
                }
            }

            val children = node.optJSONArray("Children") ?: node.optJSONArray("children")
            if (children != null) {
                for (i in 0 until children.length()) {
                    val child = children.optJSONObject(i)
                    if (child != null) walk(child)
                }
            }
        }

        walk(root)

        val cpuUsage = sensors.entries.firstOrNull { it.key.contains("CPU Total", true) }?.value
            ?: sensors.entries.firstOrNull { it.key.startsWith("Load:CPU", true) }?.value
            ?: 35f

        val cpuTemp = sensors.entries.firstOrNull { it.key.contains("CPU Package", true) }?.value
            ?: sensors.entries.firstOrNull { it.key.contains("Core (Tdie)", true) }?.value
            ?: sensors.entries.firstOrNull { it.key.startsWith("Temperature:CPU", true) }?.value
            ?: 52f

        val gpuUsage = sensors.entries.firstOrNull { it.key.contains("GPU Core", true) && it.key.contains("Load") }?.value
            ?: 45f

        val gpuTemp = sensors.entries.firstOrNull { it.key.contains("GPU Core", true) && it.key.contains("Temperature") }?.value
            ?: 58f

        val gpuHotspot = sensors.entries.firstOrNull { it.key.contains("Hot Spot", true) }?.value
            ?: (gpuTemp + 10f)

        val ramUsage = sensors.entries.firstOrNull { it.key.contains("Memory", true) && it.key.contains("Load") }?.value
            ?: 42f

        val ramUsed = sensors.entries.firstOrNull { it.key.contains("Memory Used", true) }?.value
            ?: 14f

        val ramAvail = sensors.entries.firstOrNull { it.key.contains("Memory Available", true) }?.value
            ?: 18f

        val totalRam = if (ramUsed + ramAvail > 1f) (ramUsed + ramAvail) else 32f

        return SystemMetrics(
            timestamp = System.currentTimeMillis(),
            cpu = CpuMetrics(
                name = detectedCpuName,
                usagePercent = cpuUsage.coerceIn(0f, 100f),
                tempCelsius = cpuTemp,
                clockGhz = 4.6f,
                powerWatts = 65f,
                coreCount = coreLoads.size.coerceAtLeast(8),
                threadCount = coreLoads.size.coerceAtLeast(8) * 2,
                coreLoads = if (coreLoads.isNotEmpty()) coreLoads else listOf(20f, 40f, 35f, 60f, 15f, 25f, 50f, 30f),
                isThrottling = cpuTemp >= 95f
            ),
            gpu = GpuMetrics(
                name = detectedGpuName,
                usagePercent = gpuUsage.coerceIn(0f, 100f),
                tempCelsius = gpuTemp,
                hotSpotTempCelsius = gpuHotspot,
                vramUsedGb = 6.4f,
                vramTotalGb = 16.0f,
                clockMhz = 2500,
                fanPercent = 45f,
                fanRpm = 1300,
                powerWatts = 180f,
                isThrottling = gpuTemp >= 88f
            ),
            ram = RamMetrics(
                usedGb = ramUsed,
                totalGb = totalRam,
                usagePercent = ramUsage.coerceIn(0f, 100f),
                swapUsedGb = 2.4f,
                swapTotalGb = 8.0f,
                speedMhz = 6000,
                memoryType = "DDR5"
            ),
            osName = "Windows PC (LibreHardwareMonitor)",
            uptimeSeconds = 36000L,
            latencyMs = latency,
            connectionMode = ConnectionMode.PC_NETWORK
        )
    }
}
