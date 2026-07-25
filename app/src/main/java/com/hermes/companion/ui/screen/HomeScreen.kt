package com.hermes.companion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hermes.companion.ui.screenmodel.HomeViewModel
import com.hermes.companion.ui.theme.HermesPurpleLight
import com.hermes.companion.ui.theme.StatusCyan
import com.hermes.companion.ui.theme.StatusYellow

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
                    StatCard(
                        title = "Agents",
                        value = "${state.agentsActive}",
                        bgColor = HermesPurpleLight.copy(alpha = 0.2f)
                    )
                    StatCard(
                        title = "Missions",
                        value = "${state.missionsRunning}",
                        bgColor = StatusCyan.copy(alpha = 0.2f)
                    )
                    StatCard(
                        title = "Uptime",
                        value = state.uptime,
                        bgColor = StatusYellow.copy(alpha = 0.2f)
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

@Composable
private fun StatCard(
    title: String,
    value: String,
    bgColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(2.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp)
    }
}
