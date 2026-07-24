package com.hermes.companion.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Composable showing mission execution timeline.
 *
 * Displays the following phases as a vertical timeline:
 *   Goal → Planner → Reasoning → Agents → Tools → Execution → Verification → Recovery → Completion
 */
@Composable
fun MissionTimeline(
    steps: List<MissionStep>,
    currentStep: Int = -1,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(0.dp)
    ) {
        itemsIndexed(steps) { index, step ->
            MissionTimelineItem(
                step = step,
                index = index,
                isCurrent = index == currentStep,
                isCompleted = index < currentStep,
                isLast = index == steps.lastIndex
            )
        }
    }
}

data class MissionStep(
    val label: String,
    val description: String = "",
    val status: StepStatus = StepStatus.PENDING,
    val timestamp: Long? = null
)

enum class StepStatus {
    PENDING, ACTIVE, COMPLETED, FAILED, SKIPPED
}

@Composable
private fun MissionTimelineItem(
    step: MissionStep,
    index: Int,
    isCurrent: Boolean,
    isCompleted: Boolean,
    isLast: Boolean
) {
    val lineColor = when {
        isCompleted -> Color(0xFF4CAF50)
        isCurrent -> Color(0xFF2196F3)
        else -> Color(0xFF37474F)
    }

    val dotColor = when {
        step.status == StepStatus.FAILED -> Color(0xFFF44336)
        step.status == StepStatus.COMPLETED || isCompleted -> Color(0xFF4CAF50)
        isCurrent || step.status == StepStatus.ACTIVE -> Color(0xFF2196F3)
        step.status == StepStatus.SKIPPED -> Color(0xFF9E9E9E)
        else -> Color(0xFF546E7A)
    }

    val textColor = when {
        isCurrent -> Color.White
        isCompleted -> Color(0xFFB0BEC5)
        else -> Color(0xFF607D8B)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .padding(vertical = 2.dp)
    ) {
        // Timeline column (dot + line)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            // Dot
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(dotColor),
                contentAlignment = Alignment.Center
            ) {
                if (isCurrent) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                    )
                } else if (isCompleted) {
                    Text(
                        text = "✓",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                } else if (step.status == StepStatus.FAILED) {
                    Text(
                        text = "✕",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }

            // Line
            if (!isLast) {
                Canvas(
                    modifier = Modifier
                        .width(2.dp)
                        .height(32.dp)
                ) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 0f),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2f,
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (isLast) 0.dp else 4.dp)
        ) {
            Text(
                text = step.label,
                color = textColor,
                fontSize = 13.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
            )
            if (step.description.isNotEmpty()) {
                Text(
                    text = step.description,
                    color = textColor.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 2
                )
            }
        }
    }
}

// ── Preview Helper ─────────────────────────────────────

@Composable
fun MissionTimelinePreview() {
    val steps = listOf(
        MissionStep("Goal", "Set mission objectives"),
        MissionStep("Planner", "Creating execution plan"),
        MissionStep("Reasoning", "Analyzing approach"),
        MissionStep("Agents", "Assigning to agents"),
        MissionStep("Tools", "Selecting tools"),
        MissionStep("Execution", "Running mission"),
        MissionStep("Verification", "Checking results"),
        MissionStep("Recovery", "Handling errors"),
        MissionStep("Completion", "Mission finished")
    )

    Box(modifier = Modifier.background(Color(0xFF0D1117)).padding(16.dp)) {
        MissionTimeline(steps = steps, currentStep = 3)
    }
}
