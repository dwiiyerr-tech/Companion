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
import androidx.compose.ui.unit.sp
import com.hermes.companion.ui.component.StatusBadge
import com.hermes.companion.ui.theme.HermesPurple

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MissionScreen(
    viewModel: MissionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Running", "Queued", "Completed", "Failed")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Missions", fontWeight = FontWeight.Bold) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showCreateMissionDialog = true },
                containerColor = HermesPurple
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Create Mission")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                onTabSelected = { selectedTab = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, tab ->
                    Tab(
                        selected = index == selectedTab,
                        onClick = { selectedTab = index },
                        text = { Text(tab, fontSize = 14.sp) },
                        indicator = { tab ->
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .background(HermesPurple),
                                contentAlignment = Alignment.Center
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Mission List
            val missions = when (selectedTab) {
                0 -> uiState.runningMissions
                1 -> uiState.queuedMissions
                2 -> uiState.completedMissions
                else -> uiState.failedMissions
            }

            if (missions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(8.dp))
                        Text("No ${tabs[selectedTab].lowercase()} missions", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(missions) { mission ->
                        MissionCard(
                            mission = mission,
                            onClick = { viewModel.selectMission(mission) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionCard(
    mission: MissionSummary,
    onClick: () -> Unit
) {
    val statusColor = when (mission.status) {
        MissionStatus.RUNNING -> MaterialTheme.colorScheme.primary
        MissionStatus.QUEUED -> MaterialTheme.colorScheme.tertiary
        MissionStatus.COMPLETED -> MaterialTheme.colorScheme.success
        MissionStatus.FAILED -> MaterialTheme.colorScheme.error
        MissionStatus.PAUSED -> MaterialTheme.colorScheme.warning
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(mission.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                StatusBadge(text = mission.status.name, color = statusColor)
            }
            Spacer(Modifier.height(8.dp))
            Text(mission.goal, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(12.dp))
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                LinearProgressIndicator(
                    progress = mission.progress,
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = statusColor
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "${"%.0f".format(mission.progress * 100)}%",
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "${mission.agents.size} agents",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Created ${formatTime(mission.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (mission.agents.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    mission.agents.forEach { agent ->
                        Chip(
                            onClick = {},
                            label = { Text(agent, style = MaterialTheme.typography.labelSmall) },
                            modifier = Modifier.padding(top = 0.dp, bottom = 0.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "${diff / 1000}s ago"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}