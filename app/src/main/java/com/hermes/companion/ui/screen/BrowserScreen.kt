package com.hermes.companion.ui.screen

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.hermes.companion.core.domain.LogEntry
import com.hermes.companion.core.domain.LogLevel
import com.hermes.companion.ui.screenmodel.BrowserViewModel
import com.hermes.companion.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BrowserScreen(
    onBack: () -> Unit = {},
    viewModel: BrowserViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Browser", style = MaterialTheme.typography.headlineMedium, color = Purple80)
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
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            // CDP Connection Status Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isCdpConnected) StatusGreen.copy(alpha = 0.15f) else StatusRed.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Web,
                            "CDP",
                            tint = if (state.isCdpConnected) StatusGreen else StatusRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            if (state.isCdpConnected) "CDP Connected" else "CDP Disconnected",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (state.isCdpConnected) StatusGreen else StatusRed
                        )
                    }
                }
            }

            // Browser Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControlButton("Navigate", Icons.Filled.Search, onClick = { /* navigate to URL */ })
                            ControlButton("Back", Icons.Filled.ArrowBack, onClick = { /* navigate back */ })
                            ControlButton("Forward", Icons.Filled.ArrowForward, onClick = { /* navigate forward */ })
                            ControlButton("Screenshot", Icons.Filled.Crop, onClick = { /* take screenshot */ })
                        }
                    }
                }
            }

            // URL Display
            item {
                OutlinedTextField(
                    value = state.currentUrl,
                    onValueChange = { /* update URL */ },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter URL", color = DarkOnSurfaceVariant) },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = HermesPurple,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        containerColor = DarkSurface
                    ),
                    singleLine = true
                )
            }

            // Status Indicator
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Tabs: ${state.tabCount}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = DarkOnBackground
                    )
                    if (state.isLoading) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = HermesPurpleLight
                            )
                            Text("Loading...", style = MaterialTheme.typography.bodySmall, color = HermesPurpleLight)
                        }
                    }
                }
            }

            // Log Entries
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Browser Logs",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkOnBackground
                    )
                    FilterChip(
                        selected = false,
                        onClick = { /* toggle auto scroll */ },
                        label = { Text("Auto Scroll") }
                    )
                }
            }

            items(state.logEntries) { log ->
                LogEntryItem(log = log)
            }
        }
    }
}

@Composable
private fun ControlButton(label: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .weight(1f)
            .height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = HermesPurple.copy(alpha = 0.15f),
            contentColor = HermesPurpleLight
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun LogEntryItem(log: LogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val levelColor = when (log.level) {
                        LogLevel.ERROR, LogLevel.CRITICAL -> StatusRed
                        LogLevel.WARNING -> StatusYellow
                        LogLevel.DEBUG -> StatusBlue
                        LogLevel.INFO -> StatusGreen,
                        LogLevel.FATAL -> StatusRed
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(levelColor)
                    )
                    Text(
                        log.level.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = levelColor
                    )
                }
                Text(
                    log.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = DarkOnSurfaceVariant
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