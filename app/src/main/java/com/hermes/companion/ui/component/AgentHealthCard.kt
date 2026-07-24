package com.hermes.companion.ui.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AgentHealthCard(
    name: String,
    type: String,
    status: String,
    cpu: Float,
    ram: Float,
    currentTask: String?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(type, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                StatusBadge(
                    text = status,
                    color = when (status) {
                        "Active" -> MaterialTheme.colorScheme.primary
                        "Idle" -> MaterialTheme.colorScheme.tertiary
                        "Error" -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(Modifier.weight(1f)) {
                    Text("CPU", style = MaterialTheme.typography.labelSmall)
                    LinearProgressIndicator(progress = { cpu }, modifier = Modifier.fillMaxWidth().height(4.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("RAM", style = MaterialTheme.typography.labelSmall)
                    LinearProgressIndicator(progress = { ram }, modifier = Modifier.fillMaxWidth().height(4.dp))
                }
            }
            if (currentTask != null) {
                Spacer(Modifier.height(8.dp))
                Text("Current: $currentTask", style = MaterialTheme.typography.bodySmall, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
