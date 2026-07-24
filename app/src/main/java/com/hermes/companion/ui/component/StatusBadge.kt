package com.hermes.companion.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Reusable status badge composable.
 *
 * Supports success, error, warning, info, and neutral states
 * with customizable text and optional icon.
 */
@Composable
fun StatusBadge(
    status: BadgeStatus,
    text: String,
    modifier: Modifier = Modifier,
    showDot: Boolean = true
) {
    val (bgColor, textColor) = when (status) {
        BadgeStatus.SUCCESS -> Color(0xFF1B5E20) to Color(0xFF81C784)
        BadgeStatus.ERROR -> Color(0xFF4E1212) to Color(0xFFEF5350)
        BadgeStatus.WARNING -> Color(0xFF4E3A00) to Color(0xFFFFB74D)
        BadgeStatus.INFO -> Color(0xFF0D2F5E) to Color(0xFF64B5F6)
        BadgeStatus.NEUTRAL -> Color(0xFF263238) to Color(0xFFB0BEC5)
        BadgeStatus.DISABLED -> Color(0xFF1A1A1A) to Color(0xFF616161)
    }

    val dotColor = when (status) {
        BadgeStatus.SUCCESS -> Color(0xFF4CAF50)
        BadgeStatus.ERROR -> Color(0xFFF44336)
        BadgeStatus.WARNING -> Color(0xFFFF9800)
        BadgeStatus.INFO -> Color(0xFF2196F3)
        BadgeStatus.NEUTRAL -> Color(0xFF9E9E9E)
        BadgeStatus.DISABLED -> Color(0xFF424242)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

enum class BadgeStatus {
    SUCCESS, ERROR, WARNING, INFO, NEUTRAL, DISABLED
}

// ── Convenience Composables ──────────────────────────────

@Composable
fun SuccessBadge(text: String, modifier: Modifier = Modifier) {
    StatusBadge(status = BadgeStatus.SUCCESS, text = text, modifier = modifier)
}

@Composable
fun ErrorBadge(text: String, modifier: Modifier = Modifier) {
    StatusBadge(status = BadgeStatus.ERROR, text = text, modifier = modifier)
}

@Composable
fun WarningBadge(text: String, modifier: Modifier = Modifier) {
    StatusBadge(status = BadgeStatus.WARNING, text = text, modifier = modifier)
}

@Composable
fun InfoBadge(text: String, modifier: Modifier = Modifier) {
    StatusBadge(status = BadgeStatus.INFO, text = text, modifier = modifier)
}

// ── Preview ──────────────────────────────────────────────

@Composable
fun StatusBadgePreview() {
    Column(
        modifier = Modifier
            .background(Color(0xFF0D1117))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusBadge(status = BadgeStatus.SUCCESS, text = "Connected")
        StatusBadge(status = BadgeStatus.ERROR, text = "Failed")
        StatusBadge(status = BadgeStatus.WARNING, text = "Degraded")
        StatusBadge(status = BadgeStatus.INFO, text = "Running")
        StatusBadge(status = BadgeStatus.NEUTRAL, text = "Idle")
        StatusBadge(status = BadgeStatus.DISABLED, text = "Disabled")
    }
}
