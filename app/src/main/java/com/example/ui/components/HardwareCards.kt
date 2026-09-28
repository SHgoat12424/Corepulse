package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
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
import com.example.data.model.CpuMetrics
import com.example.data.model.GpuMetrics
import com.example.data.model.RamMetrics
import com.example.data.model.SystemMetrics
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.formatTemp
import com.example.ui.theme.getTemperatureColor

@Composable
fun CpuCard(
    cpu: CpuMetrics,
    historyUsage: List<Float>,
    useFahrenheit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempColor = getTemperatureColor(cpu.tempCelsius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "CPU",
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "PROCESSOR (CPU)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
                Text(
                    text = cpu.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Gauges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularGauge(
                value = cpu.usagePercent,
                maxValue = 100f,
                label = "LOAD",
                unit = "%",
                accentColor = CyberCyan,
                size = 110.dp,
                showSubtext = "${cpu.coreCount} Cores"
            )

            CircularGauge(
                value = cpu.tempCelsius,
                maxValue = 105f,
                label = "TEMP",
                unit = if (useFahrenheit) "°F" else "°C",
                accentColor = tempColor,
                size = 110.dp,
                showSubtext = if (cpu.isThrottling) "THROTTLING" else "${cpu.powerWatts.toInt()}W TDP"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Live Sparkline Chart
        SparklineChart(
            dataPoints = historyUsage,
            lineColor = CyberCyan,
            label = "UTILIZATION TIMELINE (LAST 30s)",
            height = 60.dp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Stats Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C1322))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatPair(label = "Clock", value = "${String.format("%.2f", cpu.clockGhz)} GHz")
            StatPair(label = "Power", value = "${String.format("%.1f", cpu.powerWatts)} W")
            StatPair(label = "Threads", value = "${cpu.threadCount} T")
        }
    }
}

@Composable
fun GpuCard(
    gpu: GpuMetrics,
    historyUsage: List<Float>,
    useFahrenheit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tempColor = getTemperatureColor(gpu.tempCelsius)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(ElectricPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Speed,
                    contentDescription = "GPU",
                    tint = ElectricPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GRAPHICS (GPU)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = ElectricPurple,
                    letterSpacing = 1.sp
                )
                Text(
                    text = gpu.name,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    maxLines = 1
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Main Gauges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularGauge(
                value = gpu.usagePercent,
                maxValue = 100f,
                label = "LOAD",
                unit = "%",
                accentColor = ElectricPurple,
                size = 110.dp,
                showSubtext = "${gpu.clockMhz} MHz"
            )

            CircularGauge(
                value = gpu.tempCelsius,
                maxValue = 100f,
                label = "CORE TEMP",
                unit = if (useFahrenheit) "°F" else "°C",
                accentColor = tempColor,
                size = 110.dp,
                showSubtext = "Hotspot: ${formatTemp(gpu.hotSpotTempCelsius, useFahrenheit)}"
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // VRAM Bar
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C1322))
                .padding(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "VRAM VIDEO MEMORY",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ElectricPurple,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = "${String.format("%.1f", gpu.vramUsedGb)} / ${String.format("%.1f", gpu.vramTotalGb)} GB (${gpu.vramUsagePercent.toInt()}%)",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            HardwareProgressBar(
                progress = gpu.vramUsagePercent / 100f,
                fillColor = ElectricPurple,
                height = 6.dp
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Quick Stats Footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C1322))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            StatPair(label = "Fan", value = "${gpu.fanPercent.toInt()}% (${gpu.fanRpm} RPM)")
            StatPair(label = "Power", value = "${gpu.powerWatts.toInt()} W")
            StatPair(label = "Hot Spot", value = formatTemp(gpu.hotSpotTempCelsius, useFahrenheit))
        }
    }
}

@Composable
fun RamCard(
    ram: RamMetrics,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(AmberGold.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Memory,
                    contentDescription = "RAM",
                    tint = AmberGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SYSTEM MEMORY (RAM)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "${ram.memoryType} @ ${ram.speedMhz} MT/s",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // RAM Utilization Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularGauge(
                value = ram.usagePercent,
                maxValue = 100f,
                label = "USED",
                unit = "%",
                accentColor = AmberGold,
                size = 90.dp
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Physical RAM", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${String.format("%.1f", ram.usedGb)} / ${String.format("%.1f", ram.totalGb)} GB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HardwareProgressBar(
                    progress = ram.usagePercent / 100f,
                    fillColor = AmberGold,
                    height = 6.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Pagefile / Swap", fontSize = 11.sp, color = TextSecondary)
                    Text(
                        text = "${String.format("%.1f", ram.swapUsedGb)} / ${String.format("%.1f", ram.swapTotalGb)} GB",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                HardwareProgressBar(
                    progress = if (ram.swapTotalGb > 0) ram.swapUsedGb / ram.swapTotalGb else 0.2f,
                    fillColor = TextMuted,
                    height = 6.dp
                )
            }
        }
    }
}

@Composable
fun ThermalHealthCard(
    metrics: SystemMetrics,
    useFahrenheit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val health = metrics.overallHealth
    val healthColor = Color(health.color)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, healthColor.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(healthColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (health.ordinal >= 3) Icons.Default.Warning else Icons.Default.Thermostat,
                    contentDescription = "Health",
                    tint = healthColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "SYSTEM HEALTH & THERMALS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = healthColor,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Score: ${metrics.healthScore}/100 • ${health.label}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }
            HealthPillBadge(status = health)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Thermal Headroom bars
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0C1322))
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Peak Thermal Zone", fontSize = 10.sp, color = TextMuted)
                Text(
                    text = formatTemp(metrics.maxTemperature, useFahrenheit),
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = healthColor,
                    fontFamily = FontFamily.Monospace
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Thermal Throttling", fontSize = 10.sp, color = TextMuted)
                Text(
                    text = if (metrics.cpu.isThrottling || metrics.gpu.isThrottling) "DETECTED (ACTIVE)" else "NONE (HEALTHY)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (metrics.cpu.isThrottling || metrics.gpu.isThrottling) CrimsonDanger else EmeraldGreen
                )
            }
        }
    }
}

@Composable
fun StatPair(label: String, value: String) {
    Column {
        Text(text = label, fontSize = 9.sp, color = TextMuted)
        Text(
            text = value,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            fontFamily = FontFamily.Monospace
        )
    }
}
