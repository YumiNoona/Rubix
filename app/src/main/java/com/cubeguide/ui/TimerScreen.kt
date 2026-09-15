package com.cubeguide.ui

import android.os.SystemClock
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubeguide.play.virtualScramble
import kotlinx.coroutines.delay

@Composable
internal fun TimerScreen() {
    val preferences = LocalAppPreferences.current
    val feedback = rememberTouchFeedback()
    val size = preferences.puzzleSize
    var start by rememberSaveable { mutableStateOf<Long?>(null) }
    var elapsed by rememberSaveable { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var scrambleKey by rememberSaveable { mutableIntStateOf(0) }
    val scramble = remember(size, scrambleKey) {
        virtualScramble(size, if (size == 2) 9 else 20).joinToString(" ") { it.notation }
    }
    LaunchedEffect(start) {
        while (start != null) {
            now = SystemClock.elapsedRealtime()
            delay(31)
        }
    }
    val display = elapsed + (start?.let { now - it } ?: 0L)
    val records = preferences.timerRecords(size)

    fun toggle() {
        feedback()
        if (start == null) {
            elapsed = 0
            now = SystemClock.elapsedRealtime()
            start = now
        } else {
            elapsed += SystemClock.elapsedRealtime() - start!!
            start = null
            preferences.addTimerRecord(size, elapsed)
        }
    }

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (start == null) "Tap the timer to start" else "Tap anywhere below to stop",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
            if (start == null) PuzzleSizeMenu(size, preferences::updatePuzzleSize)
            else Text("${size}×${size}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(14.dp))
        Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
            Text(
                scramble,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 2,
            )
        }
        Spacer(Modifier.height(14.dp))
        Surface(
            onClick = ::toggle,
            shape = RoundedCornerShape(24.dp),
            color = if (start == null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,
            modifier = Modifier.fillMaxWidth().weight(1f),
        ) {
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                Text(
                    formatTime(display),
                    fontSize = if (maxWidth < 340.dp) 46.sp else 56.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    softWrap = false,
                    color = if (start == null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Last", records.firstOrNull()?.let(::formatTime) ?: "—", Modifier.weight(1f))
            StatCard("Best", records.minOrNull()?.let(::formatTime) ?: "—", Modifier.weight(1f))
            StatCard("Avg 5", records.take(5).takeIf { it.size == 5 }?.average()?.toLong()?.let(::formatTime) ?: "—", Modifier.weight(1f))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = { feedback(); scrambleKey++ }, enabled = start == null) { Text("New scramble", maxLines = 1) }
            TextButton(onClick = { feedback(); elapsed = 0; start = null }, enabled = start == null && elapsed > 0) { Text("Reset", maxLines = 1) }
        }
        Spacer(Modifier.height(6.dp))
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier) {
    Surface(modifier, shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.padding(horizontal = 8.dp, vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            Text(value, fontSize = 13.sp, lineHeight = 16.sp, fontWeight = FontWeight.SemiBold, maxLines = 1, softWrap = false)
        }
    }
}

internal fun formatTime(milliseconds: Long): String {
    val minutes = milliseconds / 60_000
    val seconds = (milliseconds / 1_000) % 60
    val hundredths = (milliseconds / 10) % 100
    return "%02d:%02d.%02d".format(minutes, seconds, hundredths)
}
