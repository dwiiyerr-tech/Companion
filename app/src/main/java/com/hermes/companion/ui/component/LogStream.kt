package com.hermes.companion.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.companion.core.domain.LogEntry
import com.hermes.companion.core.domain.LogLevel

@Composable
fun LogStream(
    logs: List<LogEntry>,
    autoScroll: Boolean = true,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        logs.forEach { log ->
            val color = when (log.level) {
                LogLevel.ERROR, LogLevel.CRITICAL -> MaterialTheme.colorScheme.error
                LogLevel.WARNING -> Color(0xFFFFC107)
                LogLevel.DEBUG -> MaterialTheme.colorScheme.onSurfaceVariant
                LogLevel.INFO -> MaterialTheme.colorScheme.onSurface
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(log.timestamp.toString(), fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(50.dp))
                Spacer(Modifier.width(4.dp))
                Text(log.level.name, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    color = color, modifier = Modifier.width(35.dp))
                Spacer(Modifier.width(4.dp))
                Text(log.source, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.width(60.dp))
                Spacer(Modifier.width(4.dp))
                Text(log.message, fontSize = 10.sp, fontFamily = FontFamily.Monospace,
                    modifier = Modifier.weight(1f))
            }
        }
    }
}
