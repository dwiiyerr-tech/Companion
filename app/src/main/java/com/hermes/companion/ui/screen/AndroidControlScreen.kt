import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hermes.companion.ui.component.PerformanceGauge
import com.hermes.companion.ui.theme.HermesPurple
import com.hermes.companion.ui.theme.StatusGreen
import com.hermes.companion.ui.theme.StatusYellow
import com.hermes.companion.ui.component.StatusBadge
import com.hermes.companion.ui.component.StatusBadge.BadgeStatus
import com.hermes.companion.ui.screenmodel.ServiceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidControlScreen(
    viewModel: AndroidControlViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Control", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // System Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Device Information", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            InfoTile("Model", uiState.deviceModel, Icons.Filled.PhoneAndroid)
                            InfoTile("OS", "Android ${uiState.androidVersion}", Icons.Filled.Info)
                            InfoTile("Battery", "${uiState.batteryLevel}%", Icons.Filled.BatteryFull)
                        }
                    }
                }
            }

            // Service Status Cards
            item {
                Text("Runtime Services", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceCard(
                        title = "Accessibility",
                        status = getAccessibilityStatus(uiState.accessibilityEnabled),
                        icon = Icons.Filled.TouchApp,
                        onToggle = { viewModel.toggleAccessibility() }
                    )
                    ServiceCard(
                        title = "Notifications",
                        status = getNotificationStatus(uiState.notificationServiceEnabled),
                        icon = Icons.Filled.Notifications,
                        onToggle = { viewModel.toggleNotificationService() }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ServiceCard(
                        title = "Media Projection",
                        status = getMediaProjectionStatus(uiState.mediaProjectionActive),
                        icon = Icons.Filled.ScreenShare,
                        onToggle = { viewModel.toggleMediaProjection() }
                    )
                    ServiceCard(
                        title = "Clipboard",
                        status = getClipboardStatus(uiState.clipboardStatus),
                        icon = Icons.Filled.ContentPaste,
                        onToggle = { viewModel.toggleClipboard() }
                    )
                }
            }

            // Sensors
            item {
                Text("Sensors", style = MaterialTheme.typography.titleMedium)
            }
            item {
                if (uiState.sensors.isEmpty()) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text("No sensors available", modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(uiState.sensors) { sensor ->
                            SensorRow(
                                name = sensor.name,
                                value = sensor.value,
                                type = sensor.type
                            )
                        }
                    }
                }
            }

            // Permissions
            item {
                Text("Permissions", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        uiState.permissions.forEach { perm ->
                            PermissionRow(
                                name = perm.name,
                                granted = perm.granted,
                                description = perm.description
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoTile(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = HermesPurple, modifier = Modifier.size(24.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ServiceCard(
    title: String,
    status: ServiceStatus,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onToggle: () -> Unit
) {
    val color = when (status) {
        ServiceStatus.ENABLED -> StatusGreen
        ServiceStatus.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
        ServiceStatus.REQUESTING -> StatusYellow
        ServiceStatus.ERROR -> MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth().weight(1f),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = color)
                    Spacer(Modifier.width(8.dp))
                    Text(title, style = MaterialTheme.typography.bodyMedium)
                }
                Switch(
                    checked = status == ServiceStatus.ENABLED,
                    onCheckedChange = { onToggle() },
                    colors = SwitchDefaults.colors(checkedThumbColor = color)
                )
            }
            Spacer(Modifier.height(4.dp))
            StatusBadge(status = when (status) {
                ServiceStatus.ENABLED -> BadgeStatus.SUCCESS
                ServiceStatus.DISABLED -> BadgeStatus.DISABLED
                ServiceStatus.REQUESTING -> BadgeStatus.INFO
                ServiceStatus.ERROR -> BadgeStatus.ERROR
            }, text = status.name)
        }
    }
}

@Composable
private fun SensorRow(name: String, value: String, type: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Filled.Sensors, contentDescription = null, tint = HermesPurple)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(name, style = MaterialTheme.typography.bodyMedium)
                    Text(type, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun PermissionRow(name: String, granted: Boolean, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(name, style = MaterialTheme.typography.bodyMedium)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(
            if (granted) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
            contentDescription = null,
            tint = if (granted) StatusGreen else MaterialTheme.colorScheme.error
        )
    }
}

// Helper functions to convert boolean state to ServiceStatus enum
private fun getAccessibilityStatus(enabled: Boolean): ServiceStatus {
    return if (enabled) ServiceStatus.ENABLED else ServiceStatus.DISABLED
}

private fun getNotificationStatus(enabled: Boolean): ServiceStatus {
    return if (enabled) ServiceStatus.ENABLED else ServiceStatus.DISABLED
}

private fun getMediaProjectionStatus(active: Boolean): ServiceStatus {
    return if (active) ServiceStatus.ENABLED else ServiceStatus.DISABLED
}

private fun getClipboardStatus(clipboard: Boolean): ServiceStatus {
    return if (clipboard) ServiceStatus.ENABLED else ServiceStatus.DISABLED
}
