package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BenchmarkProfile
import com.example.data.model.DiagnosticSession
import com.example.ui.components.CircularGauge
import com.example.ui.components.HealthPillBadge
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.ThermalHot
import com.example.ui.theme.formatTemp
import com.example.ui.theme.getTemperatureColor
import com.example.viewmodel.HardwareUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DiagnosticsScreen(
    state: HardwareUiState,
    savedSessions: List<DiagnosticSession>,
    onSaveSnapshot: (String) -> Unit,
    onResetStats: () -> Unit,
    onClearSavedSessions: () -> Unit,
    onTriggerThrottleDemo: () -> Unit,
    onDismissSnapshotToast: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val metrics = state.currentMetrics
    val stats = state.sessionStats
    val health = metrics.overallHealth
    val healthColor = Color(health.color)

    LaunchedEffect(state.showDiagnosticSnapshotSavedToast) {
        if (state.showDiagnosticSnapshotSavedToast) {
            Toast.makeText(context, "Diagnostic session snapshot saved to database!", Toast.LENGTH_SHORT).show()
            onDismissSnapshotToast()
        }
    }

    // Thermal Headroom Calculations (assuming 95°C TjMax for modern CPUs and 88°C for GPUs)
    val cpuHeadroom = (95f - metrics.cpu.tempCelsius).coerceAtLeast(0f)
    val gpuHeadroom = (88f - metrics.gpu.tempCelsius).coerceAtLeast(0f)

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
                        .background(healthColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = Icons.Default.HealthAndSafety, contentDescription = null, tint = healthColor)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "SYSTEM HEALTH & THERMAL DIAGNOSTICS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = healthColor,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Telemetry & Thermal Headroom Monitor",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Health Score & Status
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(SurfaceCard)
                    .border(1.dp, healthColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularGauge(
                    value = metrics.healthScore.toFloat(),
                    maxValue = 100f,
                    label = "HEALTH",
                    unit = "/100",
                    accentColor = healthColor,
                    size = 130.dp,
                    strokeWidth = 12.dp,
                    showSubtext = health.label
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    HealthPillBadge(status = health)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (metrics.healthScore >= 90) "System is operating within optimal thermal parameters with zero throttling."
                        else if (metrics.healthScore >= 70) "Slightly elevated operating temperatures or high sustained loads observed."
                        else "Thermal limit reached! Check cooling radiator, dust filters, or thermal paste contact.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Thermal Headroom Card
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
                    text = "THERMAL HEADROOM TO TJMAX",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                HeadroomBar(
                    label = "CPU Package Headroom",
                    currentTemp = metrics.cpu.tempCelsius,
                    headroom = cpuHeadroom,
                    limit = 95f,
                    useFahrenheit = state.useFahrenheit
                )

                Spacer(modifier = Modifier.height(12.dp))

                HeadroomBar(
                    label = "GPU Core Headroom",
                    currentTemp = metrics.gpu.tempCelsius,
                    headroom = gpuHeadroom,
                    limit = 88f,
                    useFahrenheit = state.useFahrenheit
                )
            }
        }

        // Active Diagnostic Alerts
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
                    text = "DIAGNOSTIC ADVISORY ENGINE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(10.dp))

                state.diagnosticAlerts.forEach { alert ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = alert,
                            fontSize = 12.sp,
                            color = if (alert.startsWith("⚠️") || alert.startsWith("🔥")) CrimsonDanger else TextPrimary,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // Session Peak Statistics
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
                        text = "SESSION PEAK & AVERAGE TELEMETRY",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onResetStats, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                DetailRow(icon = Icons.Default.Thermostat, label = "Max CPU Temperature", value = formatTemp(stats.maxCpuTemp, state.useFahrenheit), valueColor = getTemperatureColor(stats.maxCpuTemp))
                DetailRow(icon = Icons.Default.Thermostat, label = "Max GPU Temperature", value = formatTemp(stats.maxGpuTemp, state.useFahrenheit), valueColor = getTemperatureColor(stats.maxGpuTemp))
                DetailRow(icon = Icons.Default.Warning, label = "Peak RAM Consumed", value = "${String.format("%.1f", stats.peakRamGb)} GB")
                DetailRow(icon = Icons.Default.HealthAndSafety, label = "Average CPU Load", value = "${String.format("%.1f", stats.avgCpuUsage)}%")
                DetailRow(icon = Icons.Default.HealthAndSafety, label = "Average GPU Load", value = "${String.format("%.1f", stats.avgGpuUsage)}%")
            }
        }

        // Actions: Save Snapshot & Thermal Throttle Test
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val sessionTitle = "Snapshot @ ${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}"
                        onSaveSnapshot(sessionTitle)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color(0xFF00363D)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Snapshot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onTriggerThrottleDemo,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonDanger.copy(alpha = 0.2f),
                        contentColor = CrimsonDanger
                    ),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CrimsonDanger)
                ) {
                    Text("⚠️ Demo Throttling", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Saved Sessions History from Room Database
        if (savedSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED BENCHMARK SNAPSHOTS (${savedSessions.size})",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        letterSpacing = 1.sp
                    )
                    IconButton(onClick = onClearSavedSessions, modifier = Modifier.size(24.dp)) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Clear All", tint = TextMuted, modifier = Modifier.size(16.dp))
                    }
                }
            }

            items(savedSessions) { session ->
                SavedSessionCard(session = session, useFahrenheit = state.useFahrenheit)
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun HeadroomBar(
    label: String,
    currentTemp: Float,
    headroom: Float,
    limit: Float,
    useFahrenheit: Boolean
) {
    val tempColor = getTemperatureColor(currentTemp)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 11.sp, color = TextPrimary)
            Text(
                text = "${formatTemp(currentTemp, useFahrenheit)} (Limit: ${limit.toInt()}°C)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = tempColor,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(SurfaceCardLight)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((currentTemp / limit).coerceIn(0f, 1f))
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(tempColor)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Remaining Thermal Headroom: +${String.format("%.1f", headroom)}°C before throttling",
            fontSize = 10.sp,
            color = if (headroom < 10f) CrimsonDanger else TextMuted
        )
    }
}

@Composable
private fun SavedSessionCard(
    session: DiagnosticSession,
    useFahrenheit: Boolean
) {
    val dateStr = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(session.timestamp))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = session.sessionName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Text(text = "Score: ${session.healthScore}/100", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
        }
        Text(text = dateStr, fontSize = 10.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0C1322))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Max CPU", fontSize = 9.sp, color = TextMuted)
                Text(text = formatTemp(session.maxCpuTemp, useFahrenheit), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column {
                Text(text = "Max GPU", fontSize = 9.sp, color = TextMuted)
                Text(text = formatTemp(session.maxGpuTemp, useFahrenheit), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column {
                Text(text = "Peak RAM", fontSize = 9.sp, color = TextMuted)
                Text(text = "${String.format("%.1f", session.peakRamGb)} GB", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            }
            Column {
                Text(text = "Throttling", fontSize = 9.sp, color = TextMuted)
                Text(
                    text = if (session.throttlingDetected) "YES" else "NO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (session.throttlingDetected) CrimsonDanger else EmeraldGreen
                )
            }
        }
    }
}
