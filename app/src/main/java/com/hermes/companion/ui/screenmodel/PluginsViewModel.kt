1|package com.hermes.companion.ui.screenmodel
2|
3|import androidx.lifecycle.ViewModel
4|import androidx.compose.material.icons.Icons
5|import androidx.compose.material.icons.filled.*
6|import kotlinx.coroutines.flow.MutableStateFlow
7|import kotlinx.coroutines.flow.StateFlow
8|import kotlinx.coroutines.flow.asStateFlow
9|import kotlinx.coroutines.flow.update
10|
11|class PluginsViewModel : ViewModel() {
12|
13|    private val _uiState = MutableStateFlow(PluginsUiState())
14|    val uiState: StateFlow<PluginsUiState> = _uiState.asStateFlow()
15|
16|    init {
17|        loadPlugins()
18|    }
19|
20|    private fun loadPlugins() {
21|        _uiState.update {
22|            it.copy(
23|                installedPlugins = listOf(
24|                    PluginInfo("core_web", "Core Web Tools", "Base web scraping and HTTP tools", "1.2.0", "Hermes", Icons.Filled.Language, true, true, "1.3.0"),
25|                    PluginInfo("dev_tools", "Dev Tools Suite", "Code editing, git, and debug tools", "0.9.1", "Hermes", Icons.Filled.Code, true, false),
26|                    PluginInfo("media_tools", "Media Tools", "Image, video, audio processing", "2.0.0", "Nous Research", Icons.Filled.AudioFile, true, false),
27|                    PluginInfo("schedule", "Mission Scheduler", "Schedule and automate mission execution", "1.0.0", "Hermes", Icons.Filled.Schedule, true, true, "1.1.0")
28|                ),
29|                availablePlugins = listOf(
30|                    PluginInfo("notion", "Notion Integration", "Connect to Notion workspaces", "0.5.0", "Community", Icons.Filled.Article, downloads = 1342),
31|                    PluginInfo("slack", "Slack Connector", "Post and read Slack messages", "1.0.0", "Nous Research", Icons.Filled.Forum, downloads = 892),
32|                    PluginInfo("discord", "Discord Bot", "Discord presence and commands", "0.8.0", "Community", Icons.Filled.Chat, downloads = 456),
33|                    PluginInfo("spotify", "Spotify Controller", "Control Spotify playback", "0.3.0", "Community", Icons.Filled.MusicNote, downloads = 234),
34|                    PluginInfo("google_drive", "Google Drive", "Access Google Drive files", "1.1.0", "Nous Research", Icons.Filled.Cloud, downloads = 1567),
35|                    PluginInfo("slack_analytics", "Slack Analytics", "Analyze Slack channel activity", "0.2.0", "Community", Icons.Filled.Analytics, downloads = 89)
36|                )
37|            )
38|        }
39|    }
40|
41|    fun install(pluginId: String) {
42|        _uiState.update {
43|            val plugin = it.availablePlugins.find { p -> p.id == pluginId } ?: return@update it
44|            it.copy(
45|                availablePlugins = it.availablePlugins.filter { p -> p.id != pluginId },
46|                installedPlugins = it.installedPlugins + plugin.copy(isInstalled = true)
47|            )
48|        }
49|    }
50|
51|    fun uninstall(pluginId: String) {
52|        _uiState.update {
53|            val plugin = it.installedPlugins.find { p -> p.id == pluginId } ?: return@update it
54|            it.copy(
55|                installedPlugins = it.installedPlugins.filter { p -> p.id != pluginId },
56|                availablePlugins = it.availablePlugins + plugin.copy(isInstalled = false)
57|            )
58|        }
59|    }
60|
61|    fun update(pluginId: String) {
62|        // Mark as updated
63|        _uiState.update {
64|            it.copy(
65|                installedPlugins = it.installedPlugins.map { p ->
66|                    if (p.id == pluginId) p.copy(
67|                        version = p.newVersion ?: p.version,
68|                        isUpdateAvailable = false,
69|                        newVersion = null
70|                    ) else p
71|                }
72|            )
73|        }
74|    }
75|
76|    fun refresh() {
77|        loadPlugins()
78|    }
79|}