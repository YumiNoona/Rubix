package com.cubeguide.ui

import android.os.SystemClock
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cubeguide.play.virtualScramble
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope

private enum class TimerReadyState { IDLE, HOLDING, READY }

@Composable
internal fun TimerScreen() {
    val preferences = LocalAppPreferences.current
    val feedback = rememberTouchFeedback()
    var start by rememberSaveable { mutableStateOf<Long?>(null) }
    var elapsed by rememberSaveable { mutableLongStateOf(0L) }
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var scrambleKey by rememberSaveable { mutableIntStateOf(0) }
    var readyState by remember { mutableStateOf(TimerReadyState.IDLE) }
    val scramble = remember(scrambleKey) {
        virtualScramble(3, 20).joinToString(" ") { it.notation }
    }
    val records = preferences.timerRecords()

    LaunchedEffect(start) {
        while (start != null) {
            now = SystemClock.elapsedRealtime()
            delay(16)
        }
    }

    val display = elapsed + (start?.let { now - it } ?: 0L)

    fun stopTimer() {
        val started = start ?: return
        elapsed += SystemClock.elapsedRealtime() - started
        start = null
        readyState = TimerReadyState.IDLE
        preferences.addTimerRecord(elapsed)
        feedback()
    }

    fun startTimer() {
        elapsed = 0L
        now = SystemClock.elapsedRealtime()
        start = now
        readyState = TimerReadyState.IDLE
        feedback()
    }

    Column(Modifier.fillMaxSize()) {
        Text(if (start == null) "Hold, then release" else "Tap to stop",color=MaterialTheme.colorScheme.onSurfaceVariant,style=MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.height(14.dp))
        Surface(shape = RubixTokens.cardShape, color = MaterialTheme.colorScheme.surfaceContainer) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("3×3 scramble", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.weight(1f))
                    Icon(Icons.Rounded.Shuffle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.height(7.dp))
                Text(
                    scramble,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 2,
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        val timerColor = when {
            start != null -> MaterialTheme.colorScheme.primaryContainer
            readyState == TimerReadyState.READY -> MaterialTheme.colorScheme.tertiary.copy(alpha=.26f)
            readyState == TimerReadyState.HOLDING -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.surfaceContainerHighest
        }
        Surface(
            shape = RubixTokens.cardShape,
            color = timerColor,
            modifier = Modifier.fillMaxWidth().weight(1f).pointerInput(start) {
                detectTapGestures(
                    onPress = {
                        if (start != null) {
                            stopTimer()
                            tryAwaitRelease()
                        } else {
                            readyState = TimerReadyState.HOLDING
                            coroutineScope {
                                val arm = launch {
                                    delay(550)
                                    readyState = TimerReadyState.READY
                                    feedback()
                                }
                                val released = tryAwaitRelease()
                                arm.cancel()
                                if (released && readyState == TimerReadyState.READY) startTimer()
                                else readyState = TimerReadyState.IDLE
                            }
                        }
                    },
                )
            },
        ) {
            BoxWithConstraints(contentAlignment = Alignment.Center) {
                val timerFontSize = if (this.maxWidth < 340.dp) 46.sp else 58.sp
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        formatTime(display),
                        fontSize = timerFontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                    Text(
                        when {
                            start != null -> "RUNNING"
                            readyState == TimerReadyState.READY -> "RELEASE"
                            readyState == TimerReadyState.HOLDING -> "KEEP HOLDING"
                            else -> "HOLD TO READY"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatCard("Last", records.firstOrNull()?.let(::formatTime) ?: "—", Modifier.weight(1f))
            StatCard("Best", records.minOrNull()?.let(::formatTime) ?: "—", Modifier.weight(1f))
            StatCard("Avg 5", records.take(5).takeIf { it.size == 5 }?.average()?.toLong()?.let(::formatTime) ?: "—", Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        TimerActions(
            canScramble = start == null,
            canReset = start == null && elapsed > 0,
            onScramble = { feedback(); scrambleKey++ },
            onReset = { feedback(); elapsed = 0; readyState = TimerReadyState.IDLE },
        )
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun TimerActions(canScramble:Boolean,canReset:Boolean,onScramble:()->Unit,onReset:()->Unit) {
    @Composable fun Scramble(modifier:Modifier) {
        Button(onClick=onScramble,enabled=canScramble,modifier=modifier.height(52.dp),shape=RubixTokens.controlShape,contentPadding=PaddingValues(horizontal=10.dp)) {
            Icon(Icons.Rounded.Shuffle,null,Modifier.size(20.dp));Spacer(Modifier.width(7.dp));Text("New scramble",maxLines=1)
        }
    }
    @Composable fun Reset(modifier:Modifier) {
        OutlinedButton(onClick=onReset,enabled=canReset,modifier=modifier.height(52.dp),shape=RubixTokens.controlShape,contentPadding=PaddingValues(horizontal=10.dp)) {
            Icon(Icons.Rounded.RestartAlt,null,Modifier.size(20.dp));Spacer(Modifier.width(7.dp));Text("Reset timer",maxLines=1)
        }
    }
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        if(maxWidth<390.dp) Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
            Scramble(Modifier.fillMaxWidth());Reset(Modifier.fillMaxWidth())
        } else Row(horizontalArrangement=Arrangement.spacedBy(10.dp)) {
            Scramble(Modifier.weight(1f));Reset(Modifier.weight(1f))
        }
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
