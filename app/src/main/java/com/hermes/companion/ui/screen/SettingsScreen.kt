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
import com.hermes.companion.ui.theme.*
import kotlinx.coroutines.launch

data class SettingsSection(
    val title: String,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val id: String,
    val label: String,
    val description: String = "",
    val icon: ImageVector = Icons.Filled.Settings,
    val type: SettingsItemType = SettingsItemType.NAVIGATE,
    val value: String = "",
    val isToggleOn: Boolean = false
)

enum class SettingsItemType {
    NAVIGATE, TOGGLE, TEXT, PASSWORD
}

data class SettingsUiState(
    val brainHost: String = "ws://192.168.1.100",
    val brainPort: String = "8765",
    val authToken: String = "••••••••••••••••",
    val notificationsEnabled: Boolean = true,
    val darkTheme: Boolean = true,
    val dataCollection: Boolean = false,
    val crashReporting: Boolean = true,
    val isLoading: Boolean = false
)

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
                        ConnectionField(
                            label = "Host",
                            value = state.brainHost,
                            onValueChange = { viewModel.updateHost(it) },
                            icon = Icons.Filled.Link
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ConnectionField(
                            label = "Port",
                            value = state.brainPort,
                            onValueChange = { viewModel.updatePort(it) },
                            icon = Icons.Filled.Numbers
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ConnectionField(
                            label = "Auth Token",
                            value = state.authToken,
                            onValueChange = { viewModel.updateAuthToken(it) },
                            icon = Icons.Filled.Key,
                            isPassword = true
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = { /* test connection */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = HermesPurple,
                                contentColor = Purple80
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, "Test", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Test Connection")
                        }
                    }
                }
            }

            // Notification Preferences
            item {
                SectionHeader("Notifications")
            }

            item {
                ToggleCard(
                    label = "Push Notifications",
                    description = "Receive notifications for mission updates, errors, and agent events",
                    icon = Icons.Filled.Notifications,
                    checked = state.notificationsEnabled,
                    onCheckedChange = { viewModel.toggleNotifications() }
                )
            }

            item {
                ToggleCard(
                    label = "Mission Complete Alerts",
                    description = "Notify when a mission completes or fails",
                    icon = Icons.Filled.CheckCircle,
                    checked = true,
                    onCheckedChange = { /* toggle */ }
                )
            }

            item {
                ToggleCard(
                    label = "Error Alerts",
                    description = "Notify on agent errors and connection drops",
                    icon = Icons.Filled.ErrorOutline,
                    checked = true,
                    onCheckedChange = { /* toggle */ }
                )
            }

            // Theme
            item {
                SectionHeader("Appearance")
            }

            item {
                ToggleCard(
                    label = "Dark Theme",
                    description = "Use Material3 dark theme (always enabled for Hermes)",
                    icon = Icons.Filled.DarkMode,
                    checked = state.darkTheme,
                    onCheckedChange = { viewModel.toggleDarkTheme() }
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Palette, "Theme Color", tint = HermesPurpleLight)
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Theme Color", style = MaterialTheme.typography.titleSmall, color = DarkOnBackground)
                            Text("Hermes Purple (#6B21A8)", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(RoundedCornerShape(50))
                                .background(HermesPurple)
                        )
                    }
                }
            }

            // Data & Privacy
            item {
                SectionHeader("Data & Privacy")
            }

            item {
                ToggleCard(
                    label = "Anonymous Usage Data",
                    description = "Help improve Hermes by sending anonymous usage data",
                    icon = Icons.Filled.Analytics,
                    checked = state.dataCollection,
                    onCheckedChange = { viewModel.toggleDataCollection() }
                )
            }

            item {
                ToggleCard(
                    label = "Crash Reporting",
                    description = "Automatically send crash reports",
                    icon = Icons.Filled.BugReport,
                    checked = state.crashReporting,
                    onCheckedChange = { viewModel.toggleCrashReporting() }
                )
            }

            // Tools / Navigation
            item {
                SectionHeader("Diagnostics & Debug")
            }

            item {
                NavigationCard(
                    label = "Log Viewer",
                    description = "View real-time application logs",
                    icon = Icons.Filled.ListAlt,
                    onClick = onNavigateToLogs
                )
            }

            item {
                NavigationCard(
                    label = "Performance Monitor",
                    description = "CPU, RAM, battery, network usage",
                    icon = Icons.Filled.TrendingUp,
                    onClick = onNavigateToPerformance
                )
            }

            item {
                NavigationCard(
                    label = "Developer Tools",
                    description = "Event bus, debug console, memory inspector",
                    icon = Icons.Filled.DeveloperMode,
                    onClick = onNavigateToDeveloper
                )
            }

            // About
            item {
                SectionHeader("About")
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        InfoRow("App Version", "1.0.0-beta")
                        InfoRow("Build", "2026.07.24")
                        InfoRow("Hermes Protocol", "v2.1")
                        InfoRow("Compose UI", "Material 3")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        title,
        style = MaterialTheme.typography.titleMedium,
        color = Purple80,
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
private fun ConnectionField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label, color = DarkOnSurfaceVariant) },
        leadingIcon = { Icon(icon, label, tint = HermesPurpleLight) },
        visualTransformation = if (isPassword) androidx.compose.ui.text.input.PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        singleLine = true,
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = HermesPurple,
            unfocusedBorderColor = DarkSurfaceVariant,
            containerColor = DarkSurface,
            focusedTextColor = DarkOnBackground,
            unfocusedTextColor = DarkOnBackground
        )
    )
}

@Composable
private fun ToggleCard(
    label: String,
    description: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, label, tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = DarkOnBackground)
                Text(description, style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = HermesPurpleLight,
                    checkedTrackColor = HermesPurple,
                    uncheckedThumbColor = DarkOnSurfaceVariant,
                    uncheckedTrackColor = DarkSurface
                )
            )
        }
    }
}

@Composable
private fun NavigationCard(
    label: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, label, tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleSmall, color = DarkOnBackground)
                Text(description, style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, "Navigate", tint = DarkOnSurfaceVariant)
        }
    }
}