package com.hermes.companion.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hermes.companion.core.domain.LogEntry
import com.hermes.companion.core.domain.LogLevel
import com.hermes.companion.ui.screenmodel.LogsViewModel
import com.hermes.companion.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** UI-level color mapping for LogLevel (view concern, not domain) */
@Composable
fun LogLevel.displayColor(): Color = when (this) {
    LogLevel.DEBUG -> StatusBlue
    LogLevel.INFO -> StatusGreen
    LogLevel.WARNING -> StatusYellow
    LogLevel.ERROR -> StatusRed
    LogLevel.CRITICAL -> Color(0xFFFF1744)
}

data class LogsUiState(
    val entries: List<LogEntry> = emptyList(),
    val filterLevel: LogLevel? = null,
    val filterSource: String = "",
    val autoScroll: Boolean = true,
    val isLoading: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBack: () -> Unit = {},
    viewModel: LogsViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = state.entries.size) {
        if (state.autoScroll) {
            scope.launch {
                listState.animateScrollToItem(state.entries.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Logs", style = MaterialTheme.typography.headlineMedium, color = Purple80)
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            state = listState
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            LogLevel.entries.forEach { level ->
                                FilterChip(
                                    selected = state.filterLevel == level,
                                    onClick = {
                                        viewModel.filterLevel(if (state.filterLevel == level) null else level)
                                    },
                                    label = { Text(level.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = level.displayColor().copy(alpha = 0.3f),
                                        selectedLabelColor = level.displayColor()
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Auto-scroll", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
                                Switch(
                                    checked = state.autoScroll,
                                    onCheckedChange = { viewModel.setAutoScroll(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = HermesPurpleLight,
                                        checkedTrackColor = HermesPurple
                                    )
                                )
                            }
                            TextButton(onClick = { viewModel.exportLogs() }) {
                                Text("Export", color = HermesPurpleLight)
                            }
                        }
                    }
                }
            }

            val filteredEntries = state.entries.filter { entry ->
                (state.filterLevel == null || entry.level == state.filterLevel)
            }

            if (filteredEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No logs match current filters", color = DarkOnSurfaceVariant)
                    }
                }
            } else {
                items(filteredEntries) { log ->
                    LogEntryItem(log = log)
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(log: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(log.level.displayColor())
                    )
                    Text(
                        log.level.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = log.level.displayColor()
                    )
                    Text(
                        log.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkOnSurfaceVariant
                    )
                }
                Text(
                    formatTime(log.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkOnSurfaceVariant,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                log.message,
                style = MaterialTheme.typography.bodySmall,
                color = DarkOnBackground
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val date = Date(timestamp)
    return SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(date)
}
