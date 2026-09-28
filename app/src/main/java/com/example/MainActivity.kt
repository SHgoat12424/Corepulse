package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.BenchmarkProfile
import com.example.data.model.ConnectionMode
import com.example.ui.components.PcSetupModal
import com.example.ui.screens.CpuDetailScreen
import com.example.ui.screens.DiagnosticsScreen
import com.example.ui.screens.GpuDetailScreen
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.RamDetailScreen
import com.example.ui.theme.AmberGold
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.ElectricPurple
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.SpaceDark
import com.example.ui.theme.SurfaceBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.HardwareViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CorePulseApp()
            }
        }
    }
}

@Composable
fun CorePulseApp(viewModel: HardwareViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val savedSessions by viewModel.savedSessions.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = SpaceDark,
        topBar = {
            TopHardwareBar(
                connectionMode = uiState.connectionState.mode,
                isConnected = uiState.connectionState.isConnected,
                pingMs = uiState.connectionState.pingMs,
                useFahrenheit = uiState.useFahrenheit,
                onToggleUnit = { viewModel.toggleTemperatureUnit() },
                onOpenPcSetup = { viewModel.setShowPcSetupModal(true) }
            )
        },
        bottomBar = {
            HardwareBottomNav(
                selectedTab = uiState.selectedTab,
                onSelectTab = { viewModel.setSelectedTab(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (uiState.selectedTab) {
                0 -> OverviewScreen(
                    state = uiState,
                    onNavigateTab = { viewModel.setSelectedTab(it) },
                    onOpenPcSetup = { viewModel.setShowPcSetupModal(true) },
                    onToggleTempUnit = { viewModel.toggleTemperatureUnit() },
                    onSetPollInterval = { viewModel.setPollInterval(it) }
                )
                1 -> CpuDetailScreen(
                    state = uiState,
                    onTriggerStressTest = { viewModel.setBenchmarkProfile(BenchmarkProfile.CINEBENCH_STRESS) }
                )
                2 -> GpuDetailScreen(
                    state = uiState,
                    onTriggerGamingBenchmark = { viewModel.setBenchmarkProfile(BenchmarkProfile.BALANCED_GAMING) }
                )
                3 -> RamDetailScreen(
                    state = uiState
                )
                4 -> DiagnosticsScreen(
                    state = uiState,
                    savedSessions = savedSessions,
                    onSaveSnapshot = { viewModel.saveDiagnosticSnapshot(it) },
                    onResetStats = { viewModel.resetSessionStats() },
                    onClearSavedSessions = { viewModel.clearHistory() },
                    onTriggerThrottleDemo = { viewModel.setBenchmarkProfile(BenchmarkProfile.THERMAL_THROTTLE_DEMO) },
                    onDismissSnapshotToast = { viewModel.dismissSnapshotToast() }
                )
            }
        }

        // PC Connection & Script Setup Modal
        if (uiState.showPcSetupModal) {
            PcSetupModal(
                connectionState = uiState.connectionState,
                savedProfiles = savedProfiles,
                onConnect = { host, port, path ->
                    viewModel.connectToPc(host, port, path)
                    viewModel.setShowPcSetupModal(false)
                },
                onSetMode = { mode ->
                    viewModel.setConnectionMode(mode)
                    viewModel.setShowPcSetupModal(false)
                },
                onSetBenchmarkProfile = { profile ->
                    viewModel.setBenchmarkProfile(profile)
                    viewModel.setShowPcSetupModal(false)
                },
                onDeleteProfile = { profile ->
                    viewModel.deleteProfile(profile)
                },
                onDismiss = { viewModel.setShowPcSetupModal(false) }
            )
        }
    }
}

@Composable
fun TopHardwareBar(
    connectionMode: ConnectionMode,
    isConnected: Boolean,
    pingMs: Int,
    useFahrenheit: Boolean,
    onToggleUnit: () -> Unit,
    onOpenPcSetup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(SpaceDark)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding & Logo
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onOpenPcSetup() }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(CyberCyan, ElectricPurple)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "CorePulse",
                        tint = Color(0xFF06090F),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "COREPULSE",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary,
                            letterSpacing = 1.5.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CyberCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 5.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "MONITOR",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyberCyan,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                    Text(
                        text = "Real-time PC Performance HUD",
                        fontSize = 10.sp,
                        color = TextMuted
                    )
                }
            }

            // Right Action Buttons: Unit Toggle & Connection Status Chip
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // °C / °F Quick Switcher
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onToggleUnit() }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (useFahrenheit) "°F" else "°C",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )
                }

                // PC Connection Button
                val statusDotColor = if (isConnected) EmeraldGreen else CrimsonDanger
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard)
                        .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                        .clickable { onOpenPcSetup() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("top_pc_connection_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(statusDotColor)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "PC",
                            tint = TextSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HardwareBottomNav(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    NavigationBar(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        containerColor = SurfaceCard,
        tonalElevation = 6.dp
    ) {
        val navItems = listOf(
            Triple(0, "Overview", Icons.Default.Dashboard),
            Triple(1, "CPU", Icons.Default.Memory),
            Triple(2, "GPU", Icons.Default.Speed),
            Triple(3, "RAM", Icons.Default.Storage),
            Triple(4, "Health", Icons.Default.HealthAndSafety)
        )

        navItems.forEach { (index, label, icon) ->
            val isSelected = selectedTab == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onSelectTab(index) },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color(0xFF00363D),
                    selectedTextColor = CyberCyan,
                    indicatorColor = CyberCyan,
                    unselectedIconColor = TextMuted,
                    unselectedTextColor = TextMuted
                ),
                modifier = Modifier.testTag("nav_tab_$index")
            )
        }
    }
}
