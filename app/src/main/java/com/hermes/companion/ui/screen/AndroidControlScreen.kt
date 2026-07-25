package com.hermes.companion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hermes.companion.ui.component.StatusBadge
import com.hermes.companion.ui.component.StatusBadge.BadgeStatus
import com.hermes.companion.ui.screenmodel.AndroidControlViewModel
import com.hermes.companion.ui.screenmodel.ServiceStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AndroidControlScreen(
    onBack: () -> Unit = {},
    viewModel: AndroidControlViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadSystemInfo()
        viewModel.refreshSensors()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Control", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // System Info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Info", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        Text("Model: ${uiState.deviceModel ?: "Unknown"}")
                        Text("Android: ${uiState.androidVersion ?: "Unknown"}")
                        Text("Battery: ${uiState.batteryLevel ?: "?"}%")
                    }
                }
            }

            // Services
            item {
                Text("Services", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            item {
                ServiceCard("Accessibility", uiState.accessibilityEnabled) { viewModel.toggleAccessibility() }
            }
            item {
                ServiceCard("Notification Service", uiState.notificationServiceEnabled) { viewModel.toggleNotificationService() }
            }
            item {
                ServiceCard("Media Projection", uiState.mediaProjectionActive) { viewModel.toggleMediaProjection() }
            }
            item {
                ServiceCard("Clipboard Monitor", uiState.clipboardStatus) { viewModel.toggleClipboard() }
            }

            // Sensors
            item {
                Text("Sensors", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(uiState.sensors) { sensor ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(sensor.icon, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(sensor.name, style = MaterialTheme.typography.bodyMedium)
                        Text(sensor.value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text(sensor.type, style = MaterialTheme.typography.labelSmall)
                }
            }

            // Permissions
            item {
                Text("Permissions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            items(uiState.permissions) { perm ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(perm.name, style = MaterialTheme.typography.bodyMedium)
                    StatusBadge(
                        status = if (perm.granted) BadgeStatus.SUCCESS else BadgeStatus.ERROR,
                        text = if (perm.granted) "Granted" else "Denied"
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ServiceCard(title: String, enabled: Boolean, onToggle: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(
                    if (enabled) "Enabled" else "Disabled",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = { onToggle() })
        }
    }
}
