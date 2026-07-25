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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onNavigateToPlugins: () -> Unit = {}
) {
    val tools = remember {
        listOf(
            ToolEntry("Web Search", "Search the web", true, "web"),
            ToolEntry("Terminal", "Execute shell commands", true, "terminal"),
            ToolEntry("Browser", "Browser automation", true, "browser"),
            ToolEntry("Vision", "Image analysis", true, "vision"),
            ToolEntry("File System", "Read/write files", true, "file"),
            ToolEntry("Code Execution", "Run Python code", true, "code_execution"),
            ToolEntry("TTS", "Text-to-speech", false, "tts"),
            ToolEntry("Image Generation", "Generate images", false, "image_gen"),
            ToolEntry("Video", "Video analysis", false, "video"),
            ToolEntry("X Search", "X/Twitter search", false, "x_search"),
            ToolEntry("Delegation", "Subagent spawning", true, "delegation"),
            ToolEntry("Cron", "Scheduled tasks", true, "cronjob"),
        )
    }
    val categories = tools.map { it.category }.distinct()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Tools", fontWeight = FontWeight.Bold) }) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            categories.forEach { category ->
                item { Text(category.uppercase(), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
                items(tools.filter { it.category == category }) { tool ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(14.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Filled.Build, contentDescription = null, modifier = Modifier.size(20.dp),
                                tint = if (tool.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tool.name, style = MaterialTheme.typography.bodyMedium)
                                Text(tool.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Switch(checked = tool.enabled, onCheckedChange = null)
                        }
                    }
                }
            }
        }
    }
}

private data class ToolEntry(val name: String, val description: String, val enabled: Boolean, val category: String)
