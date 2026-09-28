package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ConnectionMode
import com.example.ui.components.CpuCard
import com.example.ui.components.GpuCard
import com.example.ui.components.RamCard
import com.example.ui.components.SparklineChart
import com.example.ui.components.ThermalHealthCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.HardwareUiState

@Composable
fun OverviewScreen(
    state: HardwareUiState,
    onNavigateTab: (Int) -> Unit,
    onOpenPcSetup: () -> Unit,
    onToggleTempUnit: () -> Unit,
    onSetPollInterval: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val metrics = state.currentMetrics
    val conn = state.connectionState
    val cpuHistory = state.history.map { it.cpuUsage }
    val gpuHistory = state.history.map { it.gpuUsage }
    val tempHistory = state.history.map { it.cpuTemp }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Connection & Status Banner
        item {
            Spacer(modifier = Modifier.height(4.dp))
            ConnectionStatusBanner(
                conn = conn,
                onOpenPcSetup = onOpenPcSetup
            )
        }

        // Quick Controls Bar (Unit toggle, Refresh speed, Quick Setup)
        item {
            QuickControlsRow(
                pollIntervalMs = conn.pollIntervalMs,
                useFahrenheit = state.useFahrenheit,
                onToggleTempUnit = onToggleTempUnit,
                onSetPollInterval = onSetPollInterval,
                onOpenPcSetup = onOpenPcSetup
            )
        }

        // Diagnostic Alert Banner if throttling or high temp
        if (state.diagnosticAlerts.any { it.startsWith("⚠️") || it.startsWith("🔥") }) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CrimsonDanger.copy(alpha = 0.15f))
                        .border(1.dp, CrimsonDanger.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .clickable { onNavigateTab(4) } // Jump to diagnostics
                        .padding(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "🔥", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "THERMAL OR LOAD WARNING",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonDanger,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = state.diagnosticAlerts.firstOrNull { it.startsWith("⚠️") || it.startsWith("🔥") } ?: "",
                                fontSize = 11.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // CPU Card
        item {
            CpuCard(
                cpu = metrics.cpu,
                historyUsage = cpuHistory,
                useFahrenheit = state.useFahrenheit,
                onClick = { onNavigateTab(1) },
                modifier = Modifier.testTag("overview_cpu_card")
            )
        }

        // GPU Card
        item {
            GpuCard(
                gpu = metrics.gpu,
                historyUsage = gpuHistory,
                useFahrenheit = state.useFahrenheit,
                onClick = { onNavigateTab(2) },
                modifier = Modifier.testTag("overview_gpu_card")
            )
        }

        // RAM Card
        item {
            RamCard(
                ram = metrics.ram,
                onClick = { onNavigateTab(3) },
                modifier = Modifier.testTag("overview_ram_card")
            )
        }

        // Thermals & System Health Card
        item {
            ThermalHealthCard(
                metrics = metrics,
                useFahrenheit = state.useFahrenheit,
                onClick = { onNavigateTab(4) },
                modifier = Modifier.testTag("overview_thermal_card")
            )
        }

        // Combined Rolling Multi-Telemetry Sparkline
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "THERMAL PROGRESSION (CPU °C)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Rolling 40s",
                        fontSize = 10.sp,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                SparklineChart(
                    dataPoints = tempHistory,
                    lineColor = AmberGold,
                    minBound = 30f,
                    maxBound = 100f,
                    unit = "°C",
                    height = 75.dp
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun ConnectionStatusBanner(
    conn: com.example.data.repository.ConnectionState,
    onOpenPcSetup: () -> Unit
) {
    val statusColor = if (conn.isConnected) EmeraldGreen else CrimsonDanger
    val modeTitle = when (conn.mode) {
        ConnectionMode.PC_NETWORK -> "PC LINK: ${conn.host}:${conn.port}"
        ConnectionMode.SIMULATED_TEST_RIG -> "TEST RIG: ${conn.activeProfile.title}"
        ConnectionMode.LOCAL_ANDROID_DEVICE -> "LOCAL DEVICE TELEMETRY"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .clickable { onOpenPcSetup() }
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = modeTitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                Text(
                    text = if (conn.isConnected) "Streaming live • Ping ${conn.pingMs}ms" else "Disconnected • Tap to configure",
                    fontSize = 10.sp,
                    color = if (conn.isConnected) EmeraldGreen else CrimsonDanger
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(SurfaceCardLight)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Switch",
                    tint = CyberCyan,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CHANGE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }
        }
    }
}

@Composable
private fun QuickControlsRow(
    pollIntervalMs: Long,
    useFahrenheit: Boolean,
    onToggleTempUnit: () -> Unit,
    onSetPollInterval: (Long) -> Unit,
    onOpenPcSetup: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Temp Unit Toggle
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                .clickable { onToggleTempUnit() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (useFahrenheit) "UNIT: °F" else "UNIT: °C",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan,
                fontFamily = FontFamily.Monospace
            )
        }

        // Refresh Rate Selector
        Box(
            modifier = Modifier
                .weight(1.2f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                .clickable {
                    val nextInterval = when (pollIntervalMs) {
                        500L -> 1000L
                        1000L -> 2000L
                        else -> 500L
                    }
                    onSetPollInterval(nextInterval)
                }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "SPEED: ${pollIntervalMs}ms",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontFamily = FontFamily.Monospace
            )
        }

        // PC Connection Guide Button
        Box(
            modifier = Modifier
                .weight(1.3f)
                .clip(RoundedCornerShape(8.dp))
                .background(SurfaceCard)
                .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                .clickable { onOpenPcSetup() }
                .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Computer,
                    contentDescription = null,
                    tint = ElectricPurple,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "PC CONNECT",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple
                )
            }
        }
    }
}
