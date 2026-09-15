package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun ProgressScreen(preferences: AppPreferences) {
    val allTimes = (2..7).flatMap(preferences::timerRecords)
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Practice records stored on this device.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProgressCard("Timed solves", allTimes.size.toString(), Modifier.weight(1f))
            ProgressCard("Best time", allTimes.minOrNull()?.let(::formatTime) ?: "—", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        ProgressCard("Lessons", "${preferences.completedLessons.size} of 7 complete", Modifier.fillMaxWidth())
        Spacer(Modifier.height(22.dp))
        Text("By cube size", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(10.dp))
        (2..7).forEach { size ->
            val records = preferences.timerRecords(size)
            ListItem(
                headlineContent = { Text("${size}×${size}") },
                supportingContent = { Text("${records.size} timed solve${if (records.size == 1) "" else "s"}") },
                trailingContent = { Text(records.minOrNull()?.let(::formatTime) ?: "—", maxLines = 1) },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ProgressCard(label: String, value: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.Start) {
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
