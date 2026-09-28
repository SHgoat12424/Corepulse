package com.example.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import com.example.data.local.CorePulseDatabase
import com.example.data.model.BenchmarkProfile
import com.example.data.model.ConnectionMode
import com.example.data.model.ConnectionProfile
import com.example.data.model.CpuMetrics
import com.example.data.model.DiagnosticSession
import com.example.data.model.GpuMetrics
import com.example.data.model.RamMetrics
import com.example.data.model.SystemMetrics
import com.example.data.network.HardwareMonitorClient
import com.example.data.network.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Random
import kotlin.math.sin

data class ConnectionState(
    val mode: ConnectionMode = ConnectionMode.SIMULATED_TEST_RIG,
    val host: String = "192.168.1.100",
    val port: Int = 8085,
    val path: String = "/data.json",
    val isConnected: Boolean = true,
    val isConnecting: Boolean = false,
    val errorMessage: String? = null,
    val pingMs: Int = 12,
    val activeProfile: BenchmarkProfile = BenchmarkProfile.BALANCED_GAMING,
    val pollIntervalMs: Long = 1000L
)

class HardwareMonitorRepository(private val context: Context) {

    private val db = CorePulseDatabase.getDatabase(context)
    private val profileDao = db.profileDao()
    private val diagnosticDao = db.diagnosticDao()
    private val networkClient = HardwareMonitorClient()

    private val _connectionState = MutableStateFlow(ConnectionState())
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private val _metrics = MutableStateFlow(SystemMetrics())
    val metrics: StateFlow<SystemMetrics> = _metrics.asStateFlow()

    private var pollingJob: Job? = null
    private val repositoryScope = CoroutineScope(Dispatchers.Default)
    private val random = Random()

    // Simulation inertia states
    private var simCpuTemp = 58.0f
    private var simGpuTemp = 62.0f
    private var simGpuHotspot = 72.0f
    private var simFanRpm = 1400
    private var tickCounter = 0

    val savedProfiles: Flow<List<ConnectionProfile>> = profileDao.getAllProfiles()
    val savedSessions: Flow<List<DiagnosticSession>> = diagnosticDao.getAllSessions()

    init {
        startPolling()
    }

    fun setConnectionMode(mode: ConnectionMode) {
        _connectionState.value = _connectionState.value.copy(
            mode = mode,
            errorMessage = null
        )
    }

    fun setBenchmarkProfile(profile: BenchmarkProfile) {
        _connectionState.value = _connectionState.value.copy(
            activeProfile = profile,
            mode = ConnectionMode.SIMULATED_TEST_RIG
        )
    }

    fun setPollInterval(intervalMs: Long) {
        _connectionState.value = _connectionState.value.copy(pollIntervalMs = intervalMs)
        startPolling()
    }

    fun connectToPc(host: String, port: Int = 8085, path: String = "/data.json") {
        _connectionState.value = _connectionState.value.copy(
            host = host,
            port = port,
            path = path,
            mode = ConnectionMode.PC_NETWORK,
            isConnecting = true,
            errorMessage = null
        )
        // Also save profile to DB asynchronously
        repositoryScope.launch {
            profileDao.insertProfile(
                ConnectionProfile(
                    name = "PC @ $host:$port",
                    hostIp = host,
                    port = port,
                    path = path,
                    lastConnected = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun saveSessionSnapshot(session: DiagnosticSession) {
        diagnosticDao.insertSession(session)
    }

    suspend fun deleteProfile(profile: ConnectionProfile) {
        profileDao.deleteProfile(profile)
    }

    suspend fun clearSessionHistory() {
        diagnosticDao.clearAllSessions()
    }

    private fun startPolling() {
        pollingJob?.cancel()
        pollingJob = repositoryScope.launch {
            while (isActive) {
                val state = _connectionState.value
                when (state.mode) {
                    ConnectionMode.PC_NETWORK -> {
                        pollPcNetwork(state.host, state.port, state.path)
                    }
                    ConnectionMode.SIMULATED_TEST_RIG -> {
                        generateSimulatedMetrics(state.activeProfile)
                    }
                    ConnectionMode.LOCAL_ANDROID_DEVICE -> {
                        generateLocalDeviceMetrics()
                    }
                }
                delay(state.pollIntervalMs)
            }
        }
    }

    private suspend fun pollPcNetwork(host: String, port: Int, path: String) {
        when (val result = networkClient.fetchMetrics(host, port, path)) {
            is NetworkResult.Success -> {
                _metrics.value = result.data.copy(
                    latencyMs = result.latencyMs,
                    connectionMode = ConnectionMode.PC_NETWORK
                )
                _connectionState.value = _connectionState.value.copy(
                    isConnected = true,
                    isConnecting = false,
                    errorMessage = null,
                    pingMs = result.latencyMs
                )
            }
            is NetworkResult.Error -> {
                _connectionState.value = _connectionState.value.copy(
                    isConnected = false,
                    isConnecting = false,
                    errorMessage = result.message
                )
            }
        }
    }

    private fun generateSimulatedMetrics(profile: BenchmarkProfile) {
        tickCounter++
        val wave = sin(tickCounter * 0.15).toFloat()
        val jitter = (random.nextFloat() - 0.5f) * 2f

        val targetCpuLoad: Float
        val targetGpuLoad: Float
        val targetCpuTemp: Float
        val targetGpuTemp: Float
        val baseClock: Float
        val vramUsed: Float
        val ramUsed: Float
        val throttling: Boolean
        val coreCount = 8

        when (profile) {
            BenchmarkProfile.IDLE -> {
                targetCpuLoad = 8f + wave * 4f + jitter
                targetGpuLoad = 4f + jitter
                targetCpuTemp = 42f + wave * 2f
                targetGpuTemp = 38f + wave
                baseClock = 3.8f
                vramUsed = 2.1f
                ramUsed = 9.4f
                throttling = false
            }
            BenchmarkProfile.BALANCED_GAMING -> {
                targetCpuLoad = 52f + wave * 12f + jitter
                targetGpuLoad = 88f + wave * 8f + jitter
                targetCpuTemp = 66f + wave * 4f
                targetGpuTemp = 71f + wave * 3f
                baseClock = 4.85f
                vramUsed = 11.4f + wave * 0.5f
                ramUsed = 16.8f
                throttling = false
            }
            BenchmarkProfile.RENDER_WORKLOAD -> {
                targetCpuLoad = 94f + wave * 4f + jitter
                targetGpuLoad = 68f + wave * 8f
                targetCpuTemp = 82f + wave * 3f
                targetGpuTemp = 67f + wave * 2f
                baseClock = 4.95f
                vramUsed = 13.8f
                ramUsed = 27.4f
                throttling = false
            }
            BenchmarkProfile.CINEBENCH_STRESS -> {
                targetCpuLoad = 99.5f + (random.nextFloat() * 0.5f)
                targetGpuLoad = 12f + jitter
                targetCpuTemp = 89f + wave * 3f
                targetGpuTemp = 46f
                baseClock = 5.05f
                vramUsed = 3.2f
                ramUsed = 19.5f
                throttling = false
            }
            BenchmarkProfile.THERMAL_THROTTLE_DEMO -> {
                targetCpuLoad = 100f
                targetGpuLoad = 98f
                targetCpuTemp = 97.5f + wave * 1.5f
                targetGpuTemp = 89.0f + wave * 1.5f
                baseClock = 3.20f // Throttled clock!
                vramUsed = 15.2f
                ramUsed = 29.8f
                throttling = true
            }
        }

        // Realistic thermal inertia physics
        simCpuTemp += (targetCpuTemp - simCpuTemp) * 0.18f + (random.nextFloat() - 0.5f) * 0.3f
        simGpuTemp += (targetGpuTemp - simGpuTemp) * 0.14f + (random.nextFloat() - 0.5f) * 0.3f
        simGpuHotspot = simGpuTemp + 11.5f + (random.nextFloat() * 1.5f)

        val targetFanRpm = (simGpuTemp * 28).toInt().coerceIn(800, 2400)
        simFanRpm += ((targetFanRpm - simFanRpm) * 0.2f).toInt()
        val fanPercent = ((simFanRpm - 800f) / 1600f * 100f).coerceIn(20f, 100f)

        val cpuPower = (simCpuTemp * 1.2f + targetCpuLoad * 0.8f).coerceIn(25f, 185f)
        val gpuPower = (targetGpuLoad * 2.8f + 30f).coerceIn(35f, 340f)

        // Per-core loads distribution
        val coreLoads = List(coreCount) { i ->
            val bias = if (i % 2 == 0) 1.1f else 0.85f
            (targetCpuLoad * bias + (random.nextFloat() - 0.5f) * 15f).coerceIn(0f, 100f)
        }

        val cpu = CpuMetrics(
            name = "AMD Ryzen 7 7800X3D (8C / 16T)",
            usagePercent = targetCpuLoad.coerceIn(0f, 100f),
            tempCelsius = simCpuTemp,
            clockGhz = baseClock,
            powerWatts = cpuPower,
            coreCount = coreCount,
            threadCount = coreCount * 2,
            coreLoads = coreLoads,
            isThrottling = throttling
        )

        val gpu = GpuMetrics(
            name = "NVIDIA GeForce RTX 4080 Super (16GB)",
            usagePercent = targetGpuLoad.coerceIn(0f, 100f),
            tempCelsius = simGpuTemp,
            hotSpotTempCelsius = simGpuHotspot,
            vramUsedGb = vramUsed.coerceIn(0f, 16.0f),
            vramTotalGb = 16.0f,
            clockMhz = if (throttling) 1850 else 2580,
            fanPercent = fanPercent,
            fanRpm = simFanRpm,
            powerWatts = gpuPower,
            isThrottling = throttling
        )

        val ram = RamMetrics(
            usedGb = ramUsed,
            totalGb = 32.0f,
            usagePercent = (ramUsed / 32.0f * 100f).coerceIn(0f, 100f),
            swapUsedGb = if (throttling) 7.5f else 2.6f,
            swapTotalGb = 16.0f,
            speedMhz = 6000,
            memoryType = "DDR5 EXPO"
        )

        _metrics.value = SystemMetrics(
            timestamp = System.currentTimeMillis(),
            cpu = cpu,
            gpu = gpu,
            ram = ram,
            osName = "Windows 11 Rig (Simulated)",
            uptimeSeconds = 28450L + tickCounter,
            netDownloadKbps = (targetGpuLoad * 15f + 120f),
            netUploadKbps = (targetCpuLoad * 4f + 35f),
            latencyMs = 8 + (random.nextInt(6)),
            connectionMode = ConnectionMode.SIMULATED_TEST_RIG
        )

        _connectionState.value = _connectionState.value.copy(
            isConnected = true,
            isConnecting = false,
            errorMessage = null,
            pingMs = 8
        )
    }

    private fun generateLocalDeviceMetrics() {
        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamGb = memInfo.totalMem / (1024f * 1024f * 1024f)
        val availRamGb = memInfo.availMem / (1024f * 1024f * 1024f)
        val usedRamGb = (totalRamGb - availRamGb).coerceAtLeast(0.1f)
        val ramUsagePct = (usedRamGb / totalRamGb * 100f).coerceIn(0f, 100f)

        // Read battery temperature
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryTempRaw = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 320) ?: 320
        val deviceTemp = batteryTempRaw / 10.0f

        val cores = Runtime.getRuntime().availableProcessors()
        val dummyCpuLoad = 25f + (random.nextFloat() * 20f)

        val cpu = CpuMetrics(
            name = "Android SoC (${cores} Cores)",
            usagePercent = dummyCpuLoad,
            tempCelsius = deviceTemp + 3f,
            clockGhz = 2.4f,
            powerWatts = 4.2f,
            coreCount = cores,
            threadCount = cores,
            coreLoads = List(cores) { (dummyCpuLoad + (random.nextFloat() - 0.5f) * 10f).coerceIn(0f, 100f) },
            isThrottling = deviceTemp > 45f
        )

        val gpu = GpuMetrics(
            name = "Mobile Adreno/Mali GPU",
            usagePercent = 18f + (random.nextFloat() * 15f),
            tempCelsius = deviceTemp + 2f,
            hotSpotTempCelsius = deviceTemp + 5f,
            vramUsedGb = usedRamGb * 0.35f,
            vramTotalGb = totalRamGb * 0.5f,
            clockMhz = 850,
            fanPercent = 0f,
            fanRpm = 0,
            powerWatts = 2.5f,
            isThrottling = deviceTemp > 45f
        )

        val ram = RamMetrics(
            usedGb = usedRamGb,
            totalGb = totalRamGb,
            usagePercent = ramUsagePct,
            swapUsedGb = 0.8f,
            swapTotalGb = 4.0f,
            speedMhz = 4266,
            memoryType = "LPDDR5"
        )

        _metrics.value = SystemMetrics(
            timestamp = System.currentTimeMillis(),
            cpu = cpu,
            gpu = gpu,
            ram = ram,
            osName = "Android ${android.os.Build.VERSION.RELEASE} (Local Device)",
            uptimeSeconds = android.os.SystemClock.elapsedRealtime() / 1000L,
            netDownloadKbps = 120f,
            netUploadKbps = 24f,
            latencyMs = 1,
            connectionMode = ConnectionMode.LOCAL_ANDROID_DEVICE
        )

        _connectionState.value = _connectionState.value.copy(
            isConnected = true,
            isConnecting = false,
            errorMessage = null,
            pingMs = 1
        )
    }
}
