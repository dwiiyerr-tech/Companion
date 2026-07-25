package com.hermes.companion.ui.screenmodel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class PluginsUiState(
    val installedPlugins: List<PluginInfo> = emptyList(),
    val availablePlugins: List<PluginInfo> = emptyList()
)

data class PluginInfo(
    val id: String,
    val name: String,
    val description: String,
    val version: String,
    val author: String,
    val icon: ImageVector = Icons.Filled.Extension,
    val isInstalled: Boolean = false,
    val isUpdateAvailable: Boolean = false,
    val newVersion: String? = null,
    val downloads: Int = 0
)

class PluginsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(PluginsUiState())
    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()

    init { loadPlugins() }

    private fun loadPlugins() {
        _uiState.update {
            it.copy(
                installedPlugins = listOf(
                    PluginInfo("core_web", "Core Web Tools", "Base web scraping and HTTP tools", "1.2.0", "Hermes", Icons.Filled.Language, true, true, "1.3.0"),
                    PluginInfo("dev_tools", "Dev Tools Suite", "Code editing, git, and debug tools", "0.9.1", "Hermes", Icons.Filled.Code, true),
                    PluginInfo("media_tools", "Media Tools", "Image, video, audio processing", "2.0.0", "Nous Research", Icons.Filled.AudioFile, true),
                    PluginInfo("schedule", "Mission Scheduler", "Schedule and automate mission execution", "1.0.0", "Hermes", Icons.Filled.Schedule, true, true, "1.1.0")
                ),
                availablePlugins = listOf(
                    PluginInfo("notion", "Notion Integration", "Connect to Notion workspaces", "0.5.0", "Community", Icons.Filled.Article, downloads = 1342),
                    PluginInfo("slack", "Slack Connector", "Post and read Slack messages", "1.0.0", "Nous Research", Icons.Filled.Forum, downloads = 892),
                    PluginInfo("discord", "Discord Bot", "Discord presence and commands", "0.8.0", "Community", Icons.Filled.Chat, downloads = 456),
                    PluginInfo("spotify", "Spotify Controller", "Control Spotify playback", "0.3.0", "Community", Icons.Filled.MusicNote, downloads = 234),
                    PluginInfo("google_drive", "Google Drive", "Access Google Drive files", "1.1.0", "Nous Research", Icons.Filled.Cloud, downloads = 1567)
                )
            )
        }
    }

    fun install(pluginId: String) {
        _uiState.update {
            val plugin = it.availablePlugins.find { p -> p.id == pluginId } ?: return@update it
            it.copy(
                availablePlugins = it.availablePlugins.filter { p -> p.id != pluginId },
                installedPlugins = it.installedPlugins + plugin.copy(isInstalled = true)
            )
        }
    }

    fun uninstall(pluginId: String) {
        _uiState.update {
            val plugin = it.installedPlugins.find { p -> p.id == pluginId } ?: return@update it
            it.copy(
                installedPlugins = it.installedPlugins.filter { p -> p.id != pluginId },
                availablePlugins = it.availablePlugins + plugin.copy(isInstalled = false)
            )
        }
    }
}
