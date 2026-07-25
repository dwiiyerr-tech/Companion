package com.hermes.companion.ui.screen

import androidx.compose.animation.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.canvas.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hermes.companion.ui.screenmodel.PerformanceViewModel
import com.hermes.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onBack: () -> Unit = {},
    viewModel: PerformanceViewModel = org.koin.androidx.compose.koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Performance", style = MaterialTheme.typography.headlineMedium, color = Purple80)
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 12.dp)
        ) {
            item {
                GaugeCard(
                    title = "CPU Usage",
                    value = state.cpuUsage,
                    maxValue = 100f,
                    unit = "%",
                    icon = Icons.Filled.Memory,
                    color = StatusCyan,
                    gaugeColor = when {
                        state.cpuUsage > 80 -> StatusRed
                        state.cpuUsage > 60 -> StatusYellow
                        else -> StatusCyan
                    }
                )
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
                                Icon(Icons.Filled.Memory, "RAM", tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("RAM Usage", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            Text(
                                "${state.ramUsed} / ${state.ramTotal} GB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkOnBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.ramUsage / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = when {
                                state.ramUsage > 90 -> StatusRed
                                state.ramUsage > 75 -> StatusYellow
                                else -> HermesPurpleLight
                            },
                            trackColor = DarkSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${state.ramUsage.toInt()}% used", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
                    }
                }
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
                                Icon(
                                    if (state.isCharging) Icons.Filled.BatteryChargingFull else Icons.Filled.BatteryStd,
                                    "Battery",
                                    tint = when {
                                        state.batteryLevel < 20 -> StatusRed
                                        state.batteryLevel < 40 -> StatusYellow
                                        else -> StatusGreen
                                    },
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Battery", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            Text(
                                "${state.batteryLevel}%${if (state.isCharging) " ⚡" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = when {
                                    state.batteryLevel < 20 -> StatusRed
                                    state.batteryLevel < 40 -> StatusYellow
                                    else -> StatusGreen
                                },
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.batteryLevel / 100f },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = when {
                                state.batteryLevel < 20 -> StatusRed
                                state.batteryLevel < 40 -> StatusYellow
                                else -> StatusGreen
                            },
                            trackColor = DarkSurface
                        )
                    }
                }
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
                                Icon(Icons.Filled.Storage, "Storage", tint = HermesPurpleLight, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Storage", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            Text(
                                "${state.storageUsed} / ${state.storageTotal} GB",
                                style = MaterialTheme.typography.bodyMedium,
                                color = DarkOnBackground,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { state.storageUsed / state.storageTotal },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = HermesPurpleLight,
                            trackColor = DarkSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("${(state.storageUsed / state.storageTotal * 100).toInt()}% used", style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
                    }
                }
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
                                Icon(Icons.Filled.NetworkCheck, "Network", tint = StatusCyan, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Network", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            NetworkMetric(label = "Download", value = "${state.networkRx} MB/s", icon = Icons.Filled.ArrowDownward, color = StatusGreen)
                            NetworkMetric(label = "Upload", value = "${state.networkTx} MB/s", icon = Icons.Filled.ArrowUpward, color = StatusBlue)
                        }
                    }
                }
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
                                Icon(Icons.Filled.Timeline, "Latency", tint = StatusYellow, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Text("Mission Latency", style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                            }
                            Text(
                                "Avg: ${state.missionLatency.average().toInt()}ms",
                                style = MaterialTheme.typography.bodyMedium,
                                color = StatusYellow,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Canvas(
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                        ) {
                            val points = state.missionLatency
                            val maxLatency = points.maxOrNull() ?: 100f
                            val w = size.width
                            val h = size.height
                            val stepX = w / (points.size - 1).coerceAtLeast(1)

                            val path = androidx.compose.ui.graphics.Path()
                            path.moveTo(0f, h)
                            points.forEachIndexed { i, latency ->
                                val x = i * stepX
                                val y = h - (latency / maxLatency) * h * 0.8f
                                path.lineTo(x, y)
                            }
                            path.lineTo(w, h)
                            path.close()
                            drawPath(path, color = StatusYellow.copy(alpha = 0.15f))

                            val linePath = androidx.compose.ui.graphics.Path()
                            points.forEachIndexed { i, latency ->
                                val x = i * stepX
                                val y = h - (latency / maxLatency) * h * 0.8f
                                if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                            }
                            drawPath(
                                linePath,
                                color = StatusYellow,
                                style = androidx.compose.ui.graphics.Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                            )

                            points.forEachIndexed { i, latency ->
                                val x = i * stepX
                                val y = h - (latency / maxLatency) * h * 0.8f
                                drawCircle(color = StatusYellow, radius = 4.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GaugeCard(
    title: String,
    value: Float,
    maxValue: Float,
    unit: String,
    icon: ImageVector,
    color: Color,
    gaugeColor: Color
) {
    val progress = value / maxValue
    val animatedProgress by animateFloatAsState(progress)

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
                    Icon(icon, title, tint = color, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, color = DarkOnBackground)
                }
                Text(
                    "${value.toInt()}$unit",
                    style = MaterialTheme.typography.headlineMedium,
                    color = gaugeColor,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.fillMaxWidth().height(100.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val s = size.minDimension
                    val center = androidx.compose.ui.geometry.Offset(s / 2, s / 2)
                    val radius = s / 2 - 8.dp.toPx()

                    drawArc(
                        color = color.copy(alpha = 0.15f),
                        startAngle = -90f, sweepAngle = 360f, useCenter = false,
                        topLeft = center - androidx.compose.ui.geometry.Offset(radius, radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = androidx.compose.ui.graphics.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = gaugeColor,
                        startAngle = -90f, sweepAngle = 360f * animatedProgress, useCenter = false,
                        topLeft = center - androidx.compose.ui.geometry.Offset(radius, radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = androidx.compose.ui.graphics.Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
        }
    }
}

@Composable
private fun NetworkMetric(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(icon, label, tint = color, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, color = color, fontWeight = FontWeight.Medium)
        Text(label, style = MaterialTheme.typography.bodySmall, color = DarkOnSurfaceVariant)
    }
}
