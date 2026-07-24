package com.hermes.companion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.companion.ui.component.PerformanceGauge
import com.hermes.companion.ui.theme.HermesPurple

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
                            InfoTile("Model", uiState.deviceInfo?.model ?: "Unknown", Icons.Filled.PhoneAndroid)
                            InfoTile("OS", "Android ${uiState.deviceInfo?.androidVersion ?: "?"}", Icons.Filled.Info)
                            InfoTile("Battery", "${uiState.batteryLevel ?: 0}%", Icons.Filled.BatteryFull)
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
                        status = uiState.accessibilityStatus,
                        icon = Icons.Filled.TouchApp,
                        onToggle = { viewModel.toggleAccessibility() }
                    )
                    ServiceCard(
                        title = "Notifications",
                        status = uiState.notificationStatus,
                        icon = Icons.Filled.Notifications,
                        onToggle = { viewModel.toggleNotifications() }
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
                        status = uiState.mediaProjectionStatus,
                        icon = Icons.Filled.ScreenShare,
                        onToggle = { viewModel.toggleMediaProjection() }
                    )
                    ServiceCard(
                        title = "Clipboard",
                        status = uiState.clipboardStatus,
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
        ServiceStatus.ENABLED -> MaterialTheme.colorScheme.success
        ServiceStatus.DISABLED -> MaterialTheme.colorScheme.onSurfaceVariant
        ServiceStatus.REQUESTING -> MaterialTheme.colorScheme.warning
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
            Text(
                status.name,
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
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
            tint = if (granted) MaterialTheme.colorScheme.success else MaterialTheme.colorScheme.error
        )
    }
}