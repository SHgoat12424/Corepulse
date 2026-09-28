package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BenchmarkProfile
import com.example.ui.components.CircularGauge
import com.example.ui.components.PerCoreGrid
import com.example.ui.components.SparklineChart
import com.example.ui.components.StatPair
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.formatTemp
import com.example.ui.theme.getTemperatureColor
import com.example.viewmodel.HardwareUiState

@Composable
fun CpuDetailScreen(
    state: HardwareUiState,
    onTriggerStressTest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cpu = state.currentMetrics.cpu
    val stats = state.sessionStats
    val cpuUsageHistory = state.history.map { it.cpuUsage }
    val tempColor = getTemperatureColor(cpu.tempCelsius)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(SpaceDark)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = CyberCyan)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PROCESSOR ANALYTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = cpu.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Real-time Dual Gauges
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularGauge(
                    value = cpu.usagePercent,
                    maxValue = 100f,
                    label = "CPU LOAD",
                    unit = "%",
                    accentColor = CyberCyan,
                    size = 120.dp,
                    showSubtext = "${String.format("%.2f", cpu.clockGhz)} GHz"
                )

                CircularGauge(
                    value = cpu.tempCelsius,
                    maxValue = 105f,
                    label = "PACKAGE TEMP",
                    unit = if (state.useFahrenheit) "°F" else "°C",
                    accentColor = tempColor,
                    size = 120.dp,
                    showSubtext = if (cpu.isThrottling) "THROTTLING!" else "Headroom: OK"
                )
            }
        }

        // Live Load Sparkline
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "REAL-TIME CPU LOAD CURVE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                SparklineChart(
                    dataPoints = cpuUsageHistory,
                    lineColor = CyberCyan,
                    label = "TOTAL CORE USAGE",
                    height = 70.dp
                )
            }
        }

        // Logical Cores Utilization Grid
        item {
            PerCoreGrid(coreLoads = cpu.coreLoads)
        }

        // Telemetry Vitals Details
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "HARDWARE SPECIFICATIONS & SENSORS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(icon = Icons.Default.Speed, label = "Core Frequency", value = "${String.format("%.2f", cpu.clockGhz)} GHz")
                DetailRow(icon = Icons.Default.Bolt, label = "Package Power Draw", value = "${String.format("%.1f", cpu.powerWatts)} Watts")
                DetailRow(icon = Icons.Default.Thermostat, label = "Peak Temperature (Session)", value = formatTemp(stats.maxCpuTemp, state.useFahrenheit))
                DetailRow(icon = Icons.Default.Memory, label = "Architecture Layout", value = "${cpu.coreCount} Cores / ${cpu.threadCount} Threads")
                DetailRow(
                    icon = Icons.Default.Thermostat,
                    label = "Thermal Throttling Status",
                    value = if (cpu.isThrottling) "ACTIVE (THROTTLED)" else "NORMAL",
                    valueColor = if (cpu.isThrottling) CrimsonDanger else EmeraldGreen
                )
            }
        }

        // Quick Stress Test Benchmark Action
        item {
            Button(
                onClick = onTriggerStressTest,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CyberCyan.copy(alpha = 0.2f),
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan)
            ) {
                Text(text = "🔥 Run All-Core Cinebench Stress Profile", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = TextMuted, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
        }
        Text(
            text = value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor,
            fontFamily = FontFamily.Monospace
        )
    }
}
