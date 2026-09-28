package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.BenchmarkProfile
import com.example.data.model.ConnectionMode
import com.example.data.model.ConnectionProfile
import com.example.data.model.DiagnosticSession
import com.example.data.model.MetricPoint
import com.example.data.model.SystemMetrics
import com.example.data.repository.ConnectionState
import com.example.data.repository.HardwareMonitorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionStats(
    val maxCpuTemp: Float = 0f,
    val minCpuTemp: Float = 999f,
    val maxGpuTemp: Float = 0f,
    val minGpuTemp: Float = 999f,
    val maxCpuUsage: Float = 0f,
    val maxGpuUsage: Float = 0f,
    val peakRamGb: Float = 0f,
    val avgCpuUsage: Float = 0f,
    val avgGpuUsage: Float = 0f,
    val sampleCount: Int = 0,
    val totalCpuUsageSum: Float = 0f,
    val totalGpuUsageSum: Float = 0f
)

data class HardwareUiState(
    val currentMetrics: SystemMetrics = SystemMetrics(),
    val connectionState: ConnectionState = ConnectionState(),
    val history: List<MetricPoint> = emptyList(),
    val sessionStats: SessionStats = SessionStats(),
    val useFahrenheit: Boolean = false,
    val selectedTab: Int = 0,
    val showPcSetupModal: Boolean = false,
    val showDiagnosticSnapshotSavedToast: Boolean = false,
    val diagnosticAlerts: List<String> = emptyList()
)

class HardwareViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = HardwareMonitorRepository(application)

    private val _uiState = MutableStateFlow(HardwareUiState())
    val uiState: StateFlow<HardwareUiState> = _uiState.asStateFlow()

    val savedProfiles: StateFlow<List<ConnectionProfile>> = repository.savedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedSessions: StateFlow<List<DiagnosticSession>> = repository.savedSessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val historyBuffer = mutableListOf<MetricPoint>()
    private val maxHistoryPoints = 40

    init {
        // Collect metrics
        viewModelScope.launch {
            repository.metrics.collect { metrics ->
                updateMetrics(metrics)
            }
        }

        // Collect connection state
        viewModelScope.launch {
            repository.connectionState.collect { connState ->
                _uiState.value = _uiState.value.copy(connectionState = connState)
            }
        }
    }

    private fun updateMetrics(metrics: SystemMetrics) {
        // Update history
        val point = MetricPoint(
            timestamp = metrics.timestamp,
            cpuUsage = metrics.cpu.usagePercent,
            gpuUsage = metrics.gpu.usagePercent,
            ramUsage = metrics.ram.usagePercent,
            cpuTemp = metrics.cpu.tempCelsius,
            gpuTemp = metrics.gpu.tempCelsius
        )
        historyBuffer.add(point)
        if (historyBuffer.size > maxHistoryPoints) {
            historyBuffer.removeAt(0)
        }

        // Update session stats
        val currentStats = _uiState.value.sessionStats
        val newSamples = currentStats.sampleCount + 1
        val newCpuSum = currentStats.totalCpuUsageSum + metrics.cpu.usagePercent
        val newGpuSum = currentStats.totalGpuUsageSum + metrics.gpu.usagePercent

        val updatedStats = SessionStats(
            maxCpuTemp = maxOf(currentStats.maxCpuTemp, metrics.cpu.tempCelsius),
            minCpuTemp = if (currentStats.sampleCount == 0) metrics.cpu.tempCelsius else minOf(currentStats.minCpuTemp, metrics.cpu.tempCelsius),
            maxGpuTemp = maxOf(currentStats.maxGpuTemp, metrics.gpu.tempCelsius),
            minGpuTemp = if (currentStats.sampleCount == 0) metrics.gpu.tempCelsius else minOf(currentStats.minGpuTemp, metrics.gpu.tempCelsius),
            maxCpuUsage = maxOf(currentStats.maxCpuUsage, metrics.cpu.usagePercent),
            maxGpuUsage = maxOf(currentStats.maxGpuUsage, metrics.gpu.usagePercent),
            peakRamGb = maxOf(currentStats.peakRamGb, metrics.ram.usedGb),
            avgCpuUsage = newCpuSum / newSamples,
            avgGpuUsage = newGpuSum / newSamples,
            sampleCount = newSamples,
            totalCpuUsageSum = newCpuSum,
            totalGpuUsageSum = newGpuSum
        )

        // Evaluate diagnostics
        val alerts = mutableListOf<String>()
        if (metrics.cpu.isThrottling) {
            alerts.add("⚠️ CPU Thermal Throttling active! Clock dropped to ${String.format("%.2f", metrics.cpu.clockGhz)} GHz.")
        } else if (metrics.cpu.tempCelsius > 85f) {
            alerts.add("🔥 High CPU temperature (${String.format("%.1f", metrics.cpu.tempCelsius)}°C). Check cooler contact.")
        }

        if (metrics.gpu.isThrottling) {
            alerts.add("⚠️ GPU Thermal limit reached! Fan running at ${metrics.gpu.fanPercent.toInt()}%.")
        } else if (metrics.gpu.tempCelsius > 80f) {
            alerts.add("🌡️ GPU Hot Spot is elevated (${String.format("%.1f", metrics.gpu.hotSpotTempCelsius)}°C).")
        }

        if (metrics.ram.usagePercent > 88f) {
            alerts.add("💾 RAM near capacity (${String.format("%.1f", metrics.ram.usedGb)} / ${metrics.ram.totalGb} GB). Pagefile paging active.")
        }

        if (alerts.isEmpty()) {
            alerts.add("✅ System health is excellent. Thermal headroom is adequate.")
        }

        _uiState.value = _uiState.value.copy(
            currentMetrics = metrics,
            history = historyBuffer.toList(),
            sessionStats = updatedStats,
            diagnosticAlerts = alerts
        )
    }

    fun setSelectedTab(tab: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = tab)
    }

    fun toggleTemperatureUnit() {
        _uiState.value = _uiState.value.copy(useFahrenheit = !_uiState.value.useFahrenheit)
    }

    fun setBenchmarkProfile(profile: BenchmarkProfile) {
        repository.setBenchmarkProfile(profile)
    }

    fun setConnectionMode(mode: ConnectionMode) {
        repository.setConnectionMode(mode)
    }

    fun connectToPc(host: String, port: Int, path: String) {
        repository.connectToPc(host, port, path)
    }

    fun setPollInterval(intervalMs: Long) {
        repository.setPollInterval(intervalMs)
    }

    fun setShowPcSetupModal(show: Boolean) {
        _uiState.value = _uiState.value.copy(showPcSetupModal = show)
    }

    fun dismissSnapshotToast() {
        _uiState.value = _uiState.value.copy(showDiagnosticSnapshotSavedToast = false)
    }

    fun saveDiagnosticSnapshot(sessionName: String = "Diagnostic Benchmark") {
        viewModelScope.launch {
            val stats = _uiState.value.sessionStats
            val metrics = _uiState.value.currentMetrics
            val session = DiagnosticSession(
                sessionName = sessionName,
                maxCpuTemp = stats.maxCpuTemp,
                maxGpuTemp = stats.maxGpuTemp,
                avgCpuUsage = stats.avgCpuUsage,
                avgGpuUsage = stats.avgGpuUsage,
                peakRamGb = stats.peakRamGb,
                healthScore = metrics.healthScore,
                throttlingDetected = metrics.cpu.isThrottling || metrics.gpu.isThrottling,
                notes = "${metrics.cpu.name} | ${metrics.gpu.name} | ${metrics.ram.totalGb}GB"
            )
            repository.saveSessionSnapshot(session)
            _uiState.value = _uiState.value.copy(showDiagnosticSnapshotSavedToast = true)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearSessionHistory()
        }
    }

    fun deleteProfile(profile: ConnectionProfile) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun resetSessionStats() {
        historyBuffer.clear()
        _uiState.value = _uiState.value.copy(
            sessionStats = SessionStats(),
            history = emptyList()
        )
    }
}
