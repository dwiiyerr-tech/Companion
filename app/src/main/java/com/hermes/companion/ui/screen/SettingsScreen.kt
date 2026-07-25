package com.hermes.companion.ui.screen

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
import androidx.compose.ui.unit.dp
import com.hermes.companion.ui.screenmodel.SettingsViewModel
import com.hermes.companion.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToLogs: () -> Unit = {},
    onNavigateToPerformance: () -> Unit = {},
    onNavigateToDeveloper: () -> Unit = {},
    viewModel: SettingsViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Settings", style = MaterialTheme.typography.headlineMedium, color = Purple80)
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
            // Brain Connection
            item {
                SectionHeader("Brain Connection")
            }

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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Globe, "Host", tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Host", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            TextField(
                                value = state.brainHost,
                                onValueChange = { viewModel.updateHost(it) },
                                modifier = Modifier.width(200.dp),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = DarkSurface,
                                    focusedContainerColor = DarkSurface
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Phonelink, "Port", tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Port", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            TextField(
                                value = state.brainPort,
                                onValueChange = { viewModel.updatePort(it) },
                                modifier = Modifier.width(200.dp),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = DarkSurface,
                                    focusedContainerColor = DarkSurface
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Lock, "Auth Token", tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Auth Token", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            TextField(
                                value = state.authToken,
                                onValueChange = { viewModel.updateAuthToken(it) },
                                modifier = Modifier.width(200.dp),
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = DarkSurface,
                                    focusedContainerColor = DarkSurface
                                )
                            )
                        }
                    }
                }
            }

            item {
                SectionHeader("Display")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ToggleItem(
                            label = "Dark Theme",
                            checked = state.darkTheme,
                            onCheckedChange = { viewModel.toggleDarkTheme() }
                        )
                    }
                }
            }

            item {
                SectionHeader("Privacy & Data")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ToggleItem(
                            label = "Data Collection",
                            checked = state.dataCollection,
                            onCheckedChange = { viewModel.toggleDataCollection() },
                            description = "Allow anonymous usage analytics"
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ToggleItem(
                            label = "Crash Reporting",
                            checked = state.crashReporting,
                            onCheckedChange = { viewModel.toggleCrashReporting() },
                            description = "Report crashes to help fix bugs"
                        )
                    }
                }
            }

            item {
                SectionHeader("Notifications")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        ToggleItem(
                            label = "Enable Notifications",
                            checked = state.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications() },
                            description = "Receive notifications from the brain"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
}

@Composable
private fun ToggleItem(
    label: String,
    checked: Boolean,
    onCheckedChange: () -> Unit,
    description: String = ""
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.bodyLarge, color = DarkOnBackground)
            if (description.isNotEmpty()) {
                Text(description, style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = { onCheckedChange() },
            colors = SwitchDefaults.colors(
                checkedTrackColor = HermesPurple,
                uncheckedTrackColor = DarkSurfaceVariant,
                checkedThumbColor = MaterialTheme.colorScheme.surface
            )
        )
    }
}

@Composable
private fun TextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    colors: TextFieldDefaults.TextFieldColors = TextFieldDefaults.colors()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        colors = colors,
        singleLine = true
    )
}
