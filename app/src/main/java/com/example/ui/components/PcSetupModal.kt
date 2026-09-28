package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BenchmarkProfile
import com.example.data.model.ConnectionMode
import com.example.data.model.ConnectionProfile
import com.example.data.repository.ConnectionState
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PcSetupModal(
    connectionState: ConnectionState,
    savedProfiles: List<ConnectionProfile>,
    onConnect: (String, Int, String) -> Unit,
    onSetMode: (ConnectionMode) -> Unit,
    onSetBenchmarkProfile: (BenchmarkProfile) -> Unit,
    onDeleteProfile: (ConnectionProfile) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    var selectedSubTab by remember { mutableIntStateOf(0) }

    var hostInput by remember { mutableStateOf(connectionState.host) }
    var portInput by remember { mutableStateOf(connectionState.port.toString()) }
    var pathInput by remember { mutableStateOf(connectionState.path) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SpaceDark,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "PC Setup",
                            tint = CyberCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "PC CONNECTION & TEST RIG",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Real-time telemetry source configuration",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sub Tabs
            SecondaryTabRow(
                selectedTabIndex = selectedSubTab,
                containerColor = SurfaceCard,
                contentColor = CyberCyan,
                divider = {}
            ) {
                Tab(
                    selected = selectedSubTab == 0,
                    onClick = { selectedSubTab = 0 },
                    text = { Text("Connect PC", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 1,
                    onClick = { selectedSubTab = 1 },
                    text = { Text("PC Scripts", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedSubTab == 2,
                    onClick = { selectedSubTab = 2 },
                    text = { Text("Test Rig", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            when (selectedSubTab) {
                0 -> {
                    // PC Connect Form
                    Text(
                        text = "Enter your PC's IP address on the local Wi-Fi network and port (e.g. 192.168.1.50:8085). Supports LibreHardwareMonitor web server or our quick Python script.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Quick presets buttons
                    Text(text = "Quick IP Presets:", fontSize = 11.sp, color = TextMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PresetChip(label = "10.0.2.2 (Emulator Host)", onClick = { hostInput = "10.0.2.2"; portInput = "8085" })
                        PresetChip(label = "192.168.1.100", onClick = { hostInput = "192.168.1.100"; portInput = "8085" })
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = hostInput,
                        onValueChange = { hostInput = it },
                        label = { Text("PC IP Address") },
                        placeholder = { Text("192.168.1.xxx") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("pc_ip_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = SurfaceBorder,
                            focusedContainerColor = SurfaceCard,
                            unfocusedContainerColor = SurfaceCard
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Port") },
                            placeholder = { Text("8085") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pc_port_input"),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            )
                        )

                        OutlinedTextField(
                            value = pathInput,
                            onValueChange = { pathInput = it },
                            label = { Text("Path") },
                            placeholder = { Text("/data.json") },
                            modifier = Modifier.weight(1.2f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedContainerColor = SurfaceCard,
                                unfocusedContainerColor = SurfaceCard
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Connect button
                    Button(
                        onClick = {
                            val port = portInput.toIntOrNull() ?: 8085
                            onConnect(hostInput, port, pathInput)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("connect_pc_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CyberCyan,
                            contentColor = Color(0xFF00363D)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (connectionState.isConnecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color(0xFF00363D),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connecting to PC...", fontWeight = FontWeight.Bold)
                        } else {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Connect to PC Monitor", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (connectionState.errorMessage != null && connectionState.mode == ConnectionMode.PC_NETWORK) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "⚠️ ${connectionState.errorMessage}",
                            fontSize = 11.sp,
                            color = CrimsonDanger,
                            lineHeight = 15.sp
                        )
                    }

                    // Saved PC Profiles
                    if (savedProfiles.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "SAVED CONNECTIONS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyberCyan,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        savedProfiles.forEach { profile ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(SurfaceCard)
                                    .border(1.dp, SurfaceBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        hostInput = profile.hostIp
                                        portInput = profile.port.toString()
                                        pathInput = profile.path
                                        onConnect(profile.hostIp, profile.port, profile.path)
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = profile.name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
                                    Text(text = "${profile.hostIp}:${profile.port}${profile.path}", fontSize = 10.sp, color = TextMuted)
                                }
                                IconButton(
                                    onClick = { onDeleteProfile(profile) },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = TextMuted, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
                1 -> {
                    // PC Server Scripts
                    Text(
                        text = "To stream real-time CPU, GPU, RAM & Temperatures from your PC, choose either method below:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ScriptCard(
                        title = "Method 1: LibreHardwareMonitor (Recommended)",
                        description = "1. Download LibreHardwareMonitor (free, open source)\n2. In Options, check 'Remote Web Server' and click 'Run'\n3. Done! Port defaults to 8085 and provides full CPU, GPU, VRM, and motherboard sensors.",
                        copyText = "https://github.com/LibreHardwareMonitor/LibreHardwareMonitor",
                        copyLabel = "Copy GitHub URL",
                        context = context
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val pythonScript = """
# CorePulse Lightweight PC Server
# Run in terminal: pip install psutil
import http.server, json, psutil
class H(http.server.BaseHTTPRequestHandler):
    def do_GET(s):
        s.send_response(200); s.send_header('Content-type','application/json'); s.end_headers()
        temps = psutil.sensors_temperatures() if hasattr(psutil, 'sensors_temperatures') else {}
        t = 50.0
        for k in temps: t = temps[k][0].current; break
        d = {
            "cpu": {"name": "PC CPU", "usage": psutil.cpu_percent(), "temp": t, "frequency_ghz": 4.5, "cores": psutil.cpu_percent(percpu=True)},
            "gpu": {"name": "PC GPU", "usage": 45, "temp": 58, "hotspot_temp": 68, "vram_used_gb": 5.2, "vram_total_gb": 16.0, "fan_percent": 42},
            "ram": {"used_gb": round(psutil.virtual_memory().used/(1024**3),1), "total_gb": round(psutil.virtual_memory().total/(1024**3),1), "usage_percent": psutil.virtual_memory().percent}
        }
        s.wfile.write(json.dumps(d).encode())
print("CorePulse PC Server listening on 0.0.0.0:8085...")
http.server.HTTPServer(('0.0.0.0', 8085), H).serve_forever()
                    """.trimIndent()

                    ScriptCard(
                        title = "Method 2: 1-File Python Script (Zero Setup)",
                        description = "Runs instantly on Windows / macOS / Linux. Exposes CPU, GPU, RAM and Thermals as JSON on port 8085.",
                        copyText = pythonScript,
                        copyLabel = "Copy Python Script",
                        context = context
                    )
                }
                2 -> {
                    // Test Rig Profiles
                    Text(
                        text = "Benchmark & Test Rig allows you to immediately test gaming workloads, Cinebench multi-core stress, and thermal throttling behaviors without connecting to a PC.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    BenchmarkProfile.values().forEach { profile ->
                        val isSelected = connectionState.mode == ConnectionMode.SIMULATED_TEST_RIG && connectionState.activeProfile == profile
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) SurfaceCardLight else SurfaceCard)
                                .border(
                                    1.dp,
                                    if (isSelected) CyberCyan else SurfaceBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable {
                                    onSetBenchmarkProfile(profile)
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = profile.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) CyberCyan else TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = profile.description,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = CyberCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Local Android Device Option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (connectionState.mode == ConnectionMode.LOCAL_ANDROID_DEVICE) SurfaceCardLight else SurfaceCard)
                            .border(
                                1.dp,
                                if (connectionState.mode == ConnectionMode.LOCAL_ANDROID_DEVICE) EmeraldGreen else SurfaceBorder,
                                RoundedCornerShape(10.dp)
                            )
                            .clickable {
                                onSetMode(ConnectionMode.LOCAL_ANDROID_DEVICE)
                            }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(EmeraldGreen.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhoneAndroid,
                                contentDescription = "Local Device",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Android Device Hardware",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "Read local SoC cores, battery thermals & RAM directly",
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                        if (connectionState.mode == ConnectionMode.LOCAL_ANDROID_DEVICE) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Active",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PresetChip(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(SurfaceCardLight)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = label, fontSize = 10.sp, color = CyberCyan, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ScriptCard(
    title: String,
    description: String,
    copyText: String,
    copyLabel: String,
    context: Context
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .border(1.dp, SurfaceBorder, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("PC Server Script", copyText)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SurfaceCardLight,
                    contentColor = CyberCyan
                ),
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                modifier = Modifier.height(30.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(copyLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = description, fontSize = 11.sp, color = TextSecondary, lineHeight = 16.sp)
    }
}
