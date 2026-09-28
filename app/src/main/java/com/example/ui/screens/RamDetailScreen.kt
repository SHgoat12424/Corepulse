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
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CircularGauge
import com.example.ui.components.HardwareProgressBar
import com.example.ui.components.SparklineChart
import com.example.ui.theme.AmberGold
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.HardwareUiState

@Composable
fun RamDetailScreen(
    state: HardwareUiState,
    modifier: Modifier = Modifier
) {
    val ram = state.currentMetrics.ram
    val stats = state.sessionStats
    val ramHistory = state.history.map { it.ramUsage }
    val freeGb = (ram.totalGb - ram.usedGb).coerceAtLeast(0f)

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
                        .background(AmberGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.Memory, contentDescription = null, tint = AmberGold)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SYSTEM MEMORY ANALYTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${ram.totalGb.toInt()} GB ${ram.memoryType} @ ${ram.speedMhz} MT/s",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Circular Gauge Card
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
                    value = ram.usagePercent,
                    maxValue = 100f,
                    label = "RAM LOAD",
                    unit = "%",
                    accentColor = AmberGold,
                    size = 120.dp,
                    showSubtext = "${String.format("%.1f", ram.usedGb)} GB In Use"
                )

                Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                    Text(text = "Physical Capacity", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "${String.format("%.1f", ram.totalGb)} GB",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(text = "Available Headroom", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "${String.format("%.1f", freeGb)} GB Free",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AmberGold,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }

        // Rolling History
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
                    text = "MEMORY CONSUMPTION TIMELINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = AmberGold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                SparklineChart(
                    dataPoints = ramHistory,
                    lineColor = AmberGold,
                    label = "COMMIT CHARGE %",
                    height = 70.dp
                )
            }
        }

        // Swap / Pagefile Telemetry
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
                        text = "VIRTUAL MEMORY / PAGEFILE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = AmberGold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "${String.format("%.1f", ram.swapUsedGb)} / ${String.format("%.1f", ram.swapTotalGb)} GB",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val swapProgress = if (ram.swapTotalGb > 0) (ram.swapUsedGb / ram.swapTotalGb) else 0.1f
                HardwareProgressBar(
                    progress = swapProgress,
                    fillColor = AmberGold,
                    height = 8.dp
                )

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (ram.usagePercent > 85f) "High paging detected. Operating system is offloading background processes." else "Pagefile activity is healthy. Minimal disk thrashing.",
                    fontSize = 11.sp,
                    color = TextSecondary,
                    lineHeight = 15.sp
                )
            }
        }

        // Memory Sensor Specs
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
                    text = "MEMORY SPECIFICATIONS & METRICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(icon = Icons.Default.Speed, label = "Transfer Rate", value = "${ram.speedMhz} MT/s")
                DetailRow(icon = Icons.Default.Memory, label = "Memory Type", value = ram.memoryType)
                DetailRow(icon = Icons.Default.Save, label = "Peak RAM in Session", value = "${String.format("%.1f", stats.peakRamGb)} GB")
                DetailRow(icon = Icons.Default.Storage, label = "Total Address Space", value = "${ram.totalGb.toInt()} GB Physical")
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
