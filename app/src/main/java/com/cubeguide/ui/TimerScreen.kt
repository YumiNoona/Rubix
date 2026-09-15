package com.cubeguide.ui

import android.os.SystemClock
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.play.virtualScramble
import kotlinx.coroutines.delay

@Composable internal fun TimerScreen() {
 val preferences=LocalAppPreferences.current;val feedback=rememberTouchFeedback();val size=preferences.puzzleSize
 var start by rememberSaveable { mutableStateOf<Long?>(null) };var elapsed by rememberSaveable { mutableLongStateOf(0L) };var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) };var scrambleKey by rememberSaveable { mutableIntStateOf(0) }
 val scramble=remember(size,scrambleKey) { virtualScramble(size,if(size==2)9 else 20).joinToString(" ") { it.notation } }
 LaunchedEffect(start) { while(start!=null) { now=SystemClock.elapsedRealtime();delay(31) } }
 val display=elapsed+(start?.let { now-it } ?: 0L);val records=preferences.timerRecords(size)
 fun toggle() { feedback();if(start==null) { elapsed=0;now=SystemClock.elapsedRealtime();start=now } else { elapsed+=SystemClock.elapsedRealtime()-start!!;start=null;preferences.addTimerRecord(size,elapsed) } }
 Column(Modifier.fillMaxSize()) {
  Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Cube timer",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("Tap the timer to ${if(start==null) "start" else "stop"}",color=MaterialTheme.colorScheme.onSurfaceVariant) };PuzzleSizeMenu(size,preferences::updatePuzzleSize) }
  Spacer(Modifier.height(18.dp));Surface(shape=RoundedCornerShape(20.dp),color=MaterialTheme.colorScheme.surfaceContainer) { Text(scramble,modifier=Modifier.fillMaxWidth().padding(18.dp),textAlign=TextAlign.Center,style=MaterialTheme.typography.titleMedium) }
  Spacer(Modifier.height(18.dp));Surface(onClick={toggle()},shape=RoundedCornerShape(30.dp),color=if(start==null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.primary,modifier=Modifier.fillMaxWidth().weight(1f)) { Box(contentAlignment=Alignment.Center) { Text(formatTime(display),style=MaterialTheme.typography.displayMedium,fontWeight=FontWeight.Bold,color=if(start==null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onPrimary) } }
  Spacer(Modifier.height(14.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   StatCard("Last",records.firstOrNull()?.let(::formatTime) ?: "--",Modifier.weight(1f));StatCard("Best",records.minOrNull()?.let(::formatTime) ?: "--",Modifier.weight(1f));StatCard("Avg 5",records.take(5).takeIf { it.size==5 }?.average()?.toLong()?.let(::formatTime) ?: "--",Modifier.weight(1f))
  }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { TextButton(onClick={scrambleKey++},enabled=start==null) { Text("New scramble") };TextButton(onClick={elapsed=0;start=null},enabled=start==null && elapsed>0) { Text("Reset timer") } }
 }
}
@Composable private fun StatCard(label:String,value:String,modifier:Modifier) { Surface(modifier,shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer) { Column(Modifier.padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally) { Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(value,fontWeight=FontWeight.SemiBold) } } }
internal fun formatTime(milliseconds:Long):String { val minutes=milliseconds/60000;val seconds=(milliseconds/1000)%60;val hundredths=(milliseconds/10)%100;return "%02d:%02d.%02d".format(minutes,seconds,hundredths) }
