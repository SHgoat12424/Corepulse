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
import androidx.compose.material.icons.filled.Air
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
import com.example.ui.components.CircularGauge
import com.example.ui.components.HardwareProgressBar
import com.example.ui.components.SparklineChart
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.formatTemp
import com.example.ui.theme.getTemperatureColor
import com.example.viewmodel.HardwareUiState

@Composable
fun GpuDetailScreen(
    state: HardwareUiState,
    onTriggerGamingBenchmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gpu = state.currentMetrics.gpu
    val stats = state.sessionStats
    val gpuHistory = state.history.map { it.gpuUsage }
    val tempColor = getTemperatureColor(gpu.tempCelsius)
    val hotspotColor = getTemperatureColor(gpu.hotSpotTempCelsius)
    val hotspotDelta = (gpu.hotSpotTempCelsius - gpu.tempCelsius).coerceAtLeast(0f)

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
                        .background(ElectricPurple.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Speed, contentDescription = null, tint = ElectricPurple)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "GRAPHICS PROCESSOR ANALYTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = gpu.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Dual Gauges
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
                    value = gpu.usagePercent,
                    maxValue = 100f,
                    label = "3D LOAD",
                    unit = "%",
                    accentColor = ElectricPurple,
                    size = 120.dp,
                    showSubtext = "${gpu.clockMhz} MHz"
                )

                CircularGauge(
                    value = gpu.tempCelsius,
                    maxValue = 100f,
                    label = "CORE TEMP",
                    unit = if (state.useFahrenheit) "°F" else "°C",
                    accentColor = tempColor,
                    size = 120.dp,
                    showSubtext = "Delta: +${String.format("%.1f", hotspotDelta)}°"
                )
            }
        }

        // Live GPU Load Sparkline
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
                    text = "REAL-TIME GPU LOAD TIMELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                SparklineChart(
                    dataPoints = gpuHistory,
                    lineColor = ElectricPurple,
                    label = "DIRECTX / VULKAN PIPELINE",
                    height = 70.dp
                )
            }
        }

        // VRAM Video Memory In-Depth Bar
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
                        text = "VRAM FRAMEBUFFER ALLOCATION",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricPurple,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${String.format("%.1f", gpu.vramUsedGb)} / ${String.format("%.1f", gpu.vramTotalGb)} GB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                HardwareProgressBar(
                    progress = gpu.vramUsagePercent / 100f,
                    fillColor = ElectricPurple,
                    height = 10.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val freeGb = (gpu.vramTotalGb - gpu.vramUsedGb).coerceAtLeast(0f)
                    Text(text = "Free: ${String.format("%.1f", freeGb)} GB", fontSize = 11.sp, color = TextSecondary)
                    Text(text = "Utilization: ${gpu.vramUsagePercent.toInt()}%", fontSize = 11.sp, color = ElectricPurple, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Fan & Thermal Sensor Matrix
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
                    text = "COOLING & THERMAL MATRIX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(icon = Icons.Default.Thermostat, label = "Hot Spot Peak Temperature", value = formatTemp(gpu.hotSpotTempCelsius, state.useFahrenheit), valueColor = hotspotColor)
                DetailRow(icon = Icons.Default.Air, label = "Fan Tachometer", value = "${gpu.fanPercent.toInt()}% • ${gpu.fanRpm} RPM")
                DetailRow(icon = Icons.Default.Bolt, label = "Total Board Power (TBP)", value = "${gpu.powerWatts.toInt()} Watts")
                DetailRow(icon = Icons.Default.Speed, label = "Core Engine Clock", value = "${gpu.clockMhz} MHz")
                DetailRow(icon = Icons.Default.Thermostat, label = "Session Max GPU Temp", value = formatTemp(stats.maxGpuTemp, state.useFahrenheit))
            }
        }

        // Quick Benchmark Action
        item {
            Button(
                onClick = onTriggerGamingBenchmark,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ElectricPurple.copy(alpha = 0.2f),
                    contentColor = ElectricPurple
                ),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricPurple)
            ) {
                Text(text = "🎮 Run Cyberpunk 2077 Benchmark Load", fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
