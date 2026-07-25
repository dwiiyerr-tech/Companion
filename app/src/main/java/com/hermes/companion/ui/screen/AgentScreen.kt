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
import com.hermes.companion.ui.component.BadgeStatus
import com.hermes.companion.ui.component.StatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AgentScreen(
    onNavigateToAndroidControl: () -> Unit = {},
    onNavigateToBrowser: () -> Unit = {}
) {
    val agents = remember {
        listOf(
            AgentData("Planner", "Idle", 0.12f, 0.25f, null, Icons.Filled.AccountTree),
            AgentData("Reasoner", "Active", 0.45f, 0.38f, "Analyzing mission goal", Icons.Filled.Psychology),
            AgentData("Android", "Active", 0.22f, 0.41f, "UI tree scan", Icons.Filled.PhoneAndroid),
            AgentData("Browser", "Idle", 0.08f, 0.19f, null, Icons.Filled.Language),
            AgentData("Vision", "Active", 0.33f, 0.52f, "Screenshot analysis", Icons.Filled.Visibility),
            AgentData("Voice", "Offline", 0f, 0f, null, Icons.Filled.Mic),
            AgentData("Memory", "Active", 0.15f, 0.33f, "Indexing experiences", Icons.Filled.Storage),
            AgentData("Research", "Idle", 0f, 0.12f, null, Icons.Filled.Search),
            AgentData("Security", "Active", 0.09f, 0.18f, "Monitoring access", Icons.Filled.Security),
            AgentData("Automation", "Active", 0.28f, 0.45f, "Running workflow", Icons.Filled.Settings),
            AgentData("Supervisor", "Active", 0.05f, 0.22f, "Monitoring agents", Icons.Filled.SupervisorAccount),
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Agents", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        "${agents.count { it.status == "Active" }}/${agents.size} active",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(end = 16.dp)
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(agents) { agent ->
                AgentCard(agent)
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun AgentCard(agent: AgentData) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(agent.icon, contentDescription = agent.name, modifier = Modifier.size(28.dp))
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(agent.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Text(agent.currentTask ?: "No task", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                val badgeStatus = when (agent.status) {
                    "Active" -> BadgeStatus.SUCCESS
                    "Idle" -> BadgeStatus.INFO
                    else -> BadgeStatus.DISABLED
                }
                StatusBadge(status = badgeStatus, text = agent.status)
            }
            if (agent.status != "Offline") {
                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Column(Modifier.weight(1f)) {
                        Text("CPU ${"%.0f".format(agent.cpu * 100)}%", style = MaterialTheme.typography.labelSmall)
                        LinearProgressIndicator(progress = { agent.cpu }, modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp))
                    }
                    Column(Modifier.weight(1f)) {
                        Text("RAM ${"%.0f".format(agent.ram * 100)}%", style = MaterialTheme.typography.labelSmall)
                        LinearProgressIndicator(progress = { agent.ram }, modifier = Modifier.fillMaxWidth().height(4.dp).padding(top = 2.dp))
                    }
                }
            }
        }
    }
}

private data class AgentData(
    val name: String, val status: String, val cpu: Float, val ram: Float,
    val currentTask: String?, val icon: androidx.compose.ui.graphics.vector.ImageVector
)