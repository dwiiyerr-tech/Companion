package com.hermes.companion.ui.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WorkflowCanvas(
    nodes: List<WorkflowNode> = exampleNodes()
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Workflow", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                StatusBadge(text = "${nodes.size} blocks", color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(12.dp))

            // Simplified workflow graph using Canvas
            Canvas(modifier = Modifier.fillMaxWidth().height(120.dp)) {
                val spacing = size.width / (nodes.size)
                // Draw connection lines
                for (i in 0 until nodes.size - 1) {
                    val x1 = spacing * i + spacing / 2
                    val y1 = size.height / 2
                    val x2 = spacing * (i + 1) + spacing / 2
                    val y2 = size.height / 2
                    drawLine(
                        color = Color.Gray.copy(alpha = 0.3f),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                // Draw node indicators
                nodes.forEachIndexed { index, node ->
                    val cx = spacing * index + spacing / 2
                    val cy = size.height / 2
                    drawCircle(
                        color = if (node.active) MaterialTheme.colorScheme().primary else MaterialTheme.colorScheme().outlineVariant,
                        radius = 16.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 12.dp.toPx(),
                        center = Offset(cx, cy)
                    )
                }
            }

            // Node labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                nodes.forEach { node ->
                    Text(
                        node.label,
                        fontSize = 9.sp,
                        maxLines = 1,
                        color = if (node.active) MaterialTheme.colorScheme().onSurface else MaterialTheme.colorScheme().onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class WorkflowNode(
    val id: String,
    val label: String,
    val type: String,
    val active: Boolean = false
)

fun exampleNodes(): List<WorkflowNode> = listOf(
    WorkflowNode("1", "Goal", "goal", false),
    WorkflowNode("2", "Browser", "browser", true),
    WorkflowNode("3", "AI", "ai", false),
    WorkflowNode("4", "Memory", "memory", false),
)
