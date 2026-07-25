package com.hermes.companion.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.companion.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class EventLevel(val color: androidx.compose.ui.graphics.Color) {
    DEBUG(StatusBlue), INFO(StatusGreen), WARN(StatusYellow), ERROR(StatusRed)
}

data class BusEvent(
    val id: String,
    val level: EventLevel,
    val source: String,
    val event: String,
    val data: String,
    val timestamp: Long,
    val isExpanded: Boolean = false
)

data class MemoryInfo(
    val label: String,
    val value: String,
    val unit: String = "",
    val trend: Trend = Trend.UP
)

enum class Trend {
    UP, DOWN, STEADY
}

data class LogConsoleEntry(
    val id: String,
    val level: EventLevel,
    val message: String,
    val timestamp: Long
)

data class MemoryInfoData(
    val heapSize: String = "72",
    val allocated: String = "48",
    val freeMemory: String = "24",
    val gcCount: Int = 0,
    val lastGC: Long = 0L
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(
    onBack: () -> Unit = {},
    viewModel: DeveloperViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Developer", style = MaterialTheme.typography.headlineMedium, color = Purple80)
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back", tint = DarkOnSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refresh() }) {
                        Icon(Icons.Filled.Refresh, "Refresh", tint = DarkOnSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            // Debug Console
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.DeveloperMode, "Console", tint = HermesPurpleLight)
                                Text("Debug Console", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { /* clear console */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = StatusRed.copy(alpha = 0.15f),
                                        contentColor = StatusRed
                                    ),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Clear")
                                }
                                Button(
                                    onClick = { /* run command */ },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = HermesPurple,
                                        contentColor = Purple80
                                    ),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Run")
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Command input
                        OutlinedTextField(
                            value = "",
                            onValueChange = { /* update command */ },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter command...", color = DarkOnSurfaceVariant) },
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = HermesPurple,
                                unfocusedBorderColor = DarkSurfaceVariant,
                                containerColor = DarkSurface
                            ),
                            trailingIcon = {
                                IconButton(onClick = { /* run command */ }) {
                                    Icon(Icons.Filled.ArrowForward, "Run", tint = HermesPurpleLight)
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Console output area
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomStart) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    items(state.consoleEntries.takeLast(20)) { entry ->
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(RoundedCornerShape(3.dp))
                                                    .background(entry.level.color)
                                            )
                                            Text(
                                                entry.level.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = entry.level.color
                                            )
                                            Text(
                                                "${entry.timestamp}ms",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = DarkOnSurfaceVariant,
                                                fontSize = 9.sp
                                            )
                                            Text(
                                                entry.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DarkOnBackground,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Event Bus Inspector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Router, "Bus", tint = StatusBlue)
                                Text("Event Bus", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            Text(
                                "${state.busEvents.size} events",
                                style = MaterialTheme.typography.bodySmall,
                                color = DarkOnSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = state.filterLevel == null,
                                onClick = { viewModel.filterLevel(null) },
                                label = { Text("All Levels") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = HermesPurple.copy(alpha = 0.3f),
                                    selectedLabelColor = HermesPurpleLight
                                )
                            )
                            EventLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = state.filterLevel == level,
                                    onClick = { viewModel.filterLevel(level) },
                                    label = { Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(8.dp).clip(RoundedCornerShape(4.dp)).background(level.color))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(level.name)
                                    } },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = level.color.copy(alpha = 0.2f),
                                        selectedLabelColor = level.color
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Event entries
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopStart) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    val filteredEvents = state.busEvents.filter {
                                        state.filterLevel == null || it.level == state.filterLevel
                                    }

                                    items(filteredEvents) { event ->
                                        Row(
                                            modifier = Modifier.clickable { viewModel.toggleEventExpanded(event.id) },
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(RoundedCornerShape(4.dp))
                                                    .background(event.level.color)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    event.source,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = DarkOnBackground,
                                                    fontWeight = FontWeight.Medium
                                                )
                                                Text(
                                                    event.event,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = DarkOnSurfaceVariant,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Icon(
                                                if (event.isExpanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                                                "Expand/collapse",
                                                tint = DarkOnSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }

                                        if (event.isExpanded) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "Data: ${event.data}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DarkOnSurfaceVariant.copy(alpha = 0.8f),
                                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                            )
                                            Text(
                                                "${event.timestamp}ms",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = DarkOnSurfaceVariant,
                                                fontSize = 9.sp
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                        } else {
                                            Spacer(modifier = Modifier.height(8.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Memory Inspector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Memory, "Memory", tint = HermesPurpleLight)
                                Text("Memory Inspector", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            MemoryIndicator("Heap Size", state.memoryInfo.heapSize, "%")
                            MemoryIndicator("Allocated", state.memoryInfo.allocated, "MB")
                            MemoryIndicator("Free Memory", state.memoryInfo.freeMemory, "MB")
                            MemoryIndicator("GC Count", state.memoryInfo.gcCount.toString(), "")
                            MemoryIndicator("Last GC", formatDuration(state.memoryInfo.lastGC), "")
                        }
                    }
                }
            }

            // API Inspector
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Filled.Api, "API", tint = StatusGreen)
                                Text("API Inspector", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = DarkSurface.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Active Endpoints", style = MaterialTheme.typography.titleSmall, color = Purple80)
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ApiEndpointCard("GET", "/api/brain/status", "127.0.0.1:8765", "200")
                                    ApiEndpointCard("POST", "/api/mission/create", "127.0.0.1:8765", "201")
                                    ApiEndpointCard("GET", "/api/agent/list", "127.0.0.1:8765", "200")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MemoryIndicator(label: String, value: String, unit: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = DarkOnSurfaceVariant)
        Text(
            "$value$unit",
            style = MaterialTheme.typography.bodyMedium,
            color = DarkOnBackground,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ApiEndpointCard(method: String, path: String, host: String, status: String) {
    Card(
        modifier = Modifier.weight(1f),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    method,
                    style = MaterialTheme.typography.labelMedium,
                    color = when (method) {
                        "GET" -> StatusBlue
                        "POST" -> StatusGreen
                        "PUT" -> StatusYellow
                        "DELETE" -> StatusRed
                        else -> StatusGray
                    }
                )
                Icon(Icons.Filled.Http, contentDescription = null, tint = DarkOnSurfaceVariant, modifier = Modifier.size(12.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                path,
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnBackground,
                fontWeight = FontWeight.Medium
            )
            Text(
                host,
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Status: $status",
                style = MaterialTheme.typography.labelSmall,
                color = if (status.startsWith("2")) StatusGreen else StatusRed
            )
        }
    }
}

private fun formatDuration(duration: Long): String {
    return when {
        duration < 1000 -> "${duration}ms"
        duration < 60000 -> "${duration / 1000}s"
        duration < 3600000 -> "${duration / 60000}m"
        else -> "${duration / 3600000}h"
    }
}
