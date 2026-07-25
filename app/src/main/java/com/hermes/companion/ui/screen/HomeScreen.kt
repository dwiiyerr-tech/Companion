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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit = {},
    viewModel: HomeViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                // Stats Cards
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceEvenly) {
                    StatusBadge(
                        title = "Agents",
                        value = "${state.agentsActive}",
                        color = HermesPurpleLight.copy(alpha = 0.2f)
                    )
                    StatusBadge(
                        title = "Missions",
                        value = "${state.missionsRunning}",
                        color = StatusCyan.copy(alpha = 0.2f)
                    )
                    StatusBadge(
                        title = "Uptime",
                        value = state.uptime,
                        color = StatusYellow.copy(alpha = 0.2f)
                    )
                }
                Spacer(Modifier.height(12.dp))
            }

            item {
                Text("Recent Activity", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
            }

            items(state.recentActivity) { entry ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(Modifier.fillMaxWidth(), Arrangement.Start) {
                            Icon(
                                imageVector = entry.icon,
                                contentDescription = entry.title,
                                modifier = Modifier.size(24.dp),
                                tint = entry.color
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entry.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                Text(entry.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(entry.timeAgo, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(entry.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = entry.color, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}