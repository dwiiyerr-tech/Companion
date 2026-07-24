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
import com.hermes.companion.ui.component.StatusBadge
import com.hermes.companion.ui.component.PerformanceGauge
import com.hermes.companion.ui.theme.HermesPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToMission: () -> Unit = {},
    onNavigateToAgents: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    var brainConnected by remember { mutableStateOf(false) }
    var missionsRunning by remember { mutableIntStateOf(3) }
    var agentsActive by remember { mutableIntStateOf(7) }
    var uptime by remember { mutableStateOf("2h 14m") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hermes Companion", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    StatusBadge(
                        text = if (brainConnected) "Connected" else "Disconnected",
                        color = if (brainConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
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
            // Brain Connection Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Hermes Brain", style = MaterialTheme.typography.titleMedium)
                            Icon(
                                Icons.Filled.Circle,
                                contentDescription = "Status",
                                tint = if (brainConnected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "127.0.0.1:9876",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Stats Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        label = "Missions",
                        value = "$missionsRunning",
                        icon = Icons.Filled.PlayArrow,
                        color = HermesPurple
                    )
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        label = "Agents",
                        value = "$agentsActive",
                        icon = Icons.Filled.SmartToy,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    StatsCard(
                        modifier = Modifier.weight(1f),
                        label = "Uptime",
                        value = uptime,
                        icon = Icons.Filled.Timer,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }

            // Performance Gauges
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("System Metrics", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PerformanceGauge(label = "CPU", value = 0.34f, color = HermesPurple)
                            PerformanceGauge(label = "RAM", value = 0.62f, color = MaterialTheme.colorScheme.secondary)
                            PerformanceGauge(label = "Battery", value = 0.85f, color = MaterialTheme.colorScheme.tertiary)
                        }
                    }
                }
            }

            // Recent Activity
            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium)
            }
            items(activityList) { activity ->
                ActivityItem(
                    title = activity.title,
                    subtitle = activity.subtitle,
                    icon = activity.icon,
                    time = activity.time
                )
            }

            // Quick Actions
            item {
                Text("Quick Actions", style = MaterialTheme.typography.titleMedium)
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "New Mission",
                        icon = Icons.Filled.Add,
                        onClick = onNavigateToMission
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Agents",
                        icon = Icons.Filled.SmartToy,
                        onClick = onNavigateToAgents
                    )
                    QuickActionButton(
                        modifier = Modifier.weight(1f),
                        text = "Settings",
                        icon = Icons.Filled.Settings,
                        onClick = onNavigateToSettings
                    )
                }
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun StatsCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: androidx.compose.ui.graphics.Color
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = label, tint = color)
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun ActivityItem(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    time: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = HermesPurple, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(time, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun QuickActionButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(containerColor = HermesPurple)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(4.dp))
        Text(text, style = MaterialTheme.typography.bodySmall)
    }
}

private data class ActivityEntry(
    val title: String,
    val subtitle: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val time: String
)

private val activityList = listOf(
    ActivityEntry("Mission Completed", "Auto-post TikTok content", Icons.Filled.CheckCircle, "5m"),
    ActivityEntry("Agent Updated", "Vision agent health restored", Icons.Filled.Refresh, "12m"),
    ActivityEntry("Memory Saved", "New experience recorded", Icons.Filled.Storage, "23m"),
    ActivityEntry("Plugin Installed", "weather-forecast v1.2", Icons.Filled.Extension, "1h"),
)
