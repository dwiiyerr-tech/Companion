package com.hermes.companion.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.hermes.companion.core.domain.LogEntry
import com.hermes.companion.core.domain.LogLevel
import com.hermes.companion.ui.component.BadgeStatus
import com.hermes.companion.ui.component.StatusBadge
import com.hermes.companion.ui.screenmodel.LogsViewModel
import com.hermes.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogsScreen(
    onBack: () -> Unit = {},
    viewModel: LogsViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
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
                title = { Text("Logs", style = MaterialTheme.typography.headlineMedium, color = Purple80) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Filter row
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusBadge(status = BadgeStatus.NEUTRAL, text = "All")
                LogLevel.entries.forEach { level ->
                    val badgeStatus = when (level) {
                        LogLevel.TRACE -> BadgeStatus.NEUTRAL
                        LogLevel.DEBUG -> BadgeStatus.INFO
                        LogLevel.INFO -> BadgeStatus.SUCCESS
                        LogLevel.WARNING -> BadgeStatus.WARNING
                        LogLevel.ERROR -> BadgeStatus.ERROR
                        LogLevel.CRITICAL -> BadgeStatus.ERROR
                    }
                    StatusBadge(status = badgeStatus, text = level.name)
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = listState
            ) {
                items(state.entries.filter { entry ->
                    state.filterLevel == null || entry.level == state.filterLevel
                }) { entry ->
                    LogEntryItem(entry)
                }
            }
        }
    }
}

@Composable
private fun LogEntryItem(entry: LogEntry) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(entry.source, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Text(formatTime(entry.timestamp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Text(entry.message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    return when {
        diff < 60000 -> "Just now"
        diff < 3600000 -> "${diff / 60000}m ago"
        diff < 86400000 -> "${diff / 3600000}h ago"
        else -> "${diff / 86400000}d ago"
    }
}
