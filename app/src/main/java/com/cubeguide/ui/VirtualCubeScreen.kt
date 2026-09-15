package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import com.cubeguide.play.*
import kotlinx.coroutines.delay

@Composable internal fun VirtualCubeScreen() {
 val preferences=LocalAppPreferences.current;val feedback=rememberTouchFeedback();val size=preferences.puzzleSize
 val cubeSaver=remember(size) { Saver<VirtualCube,String>(save={it.encode()},restore={VirtualCube.decode(it)?.takeIf { cube -> cube.size==size } ?: VirtualCube.solved(size)}) }
 var cube by rememberSaveable(size,stateSaver=cubeSaver) { mutableStateOf(VirtualCube.solved(size)) };var inverse by rememberSaveable { mutableStateOf(false) };var solving by remember { mutableStateOf(false) }
 LaunchedEffect(solving) { if(solving) { val undo=cube.history.asReversed().map { it.inverse() };undo.forEach { move -> cube=cube.apply(move,false);delay(190) };cube=cube.copy(history=emptyList());solving=false } }
 Column(Modifier.fillMaxSize()) {
  Row(verticalAlignment=Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text("Virtual cube",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("${size}x${size} playground",color=MaterialTheme.colorScheme.onSurfaceVariant) };PuzzleSizeMenu(size,preferences::updatePuzzleSize) }
  VirtualCubeView(cube,Modifier.fillMaxWidth().weight(1f).heightIn(min=260.dp))
  Surface(shape=RoundedCornerShape(24.dp),color=MaterialTheme.colorScheme.surfaceContainer) { Column(Modifier.padding(14.dp)) {
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) { Face.entries.forEach { face -> OutlinedButton(onClick={feedback();cube=cube.apply(Move(face,if(inverse)3 else 1))},enabled=!solving,modifier=Modifier.weight(1f),contentPadding=PaddingValues(0.dp)) { Text(face.name,fontWeight=FontWeight.Bold) } } }
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text("Counter-clockwise",modifier=Modifier.weight(1f));Switch(checked=inverse,onCheckedChange={inverse=it}) }
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
    OutlinedButton(onClick={ val turns=virtualScramble(size);cube=turns.fold(VirtualCube.solved(size)) { state,move -> state.apply(move) } },enabled=!solving,modifier=Modifier.weight(1f)) { Text("Scramble") }
    Button(onClick={solving=true},enabled=!solving && cube.history.isNotEmpty(),modifier=Modifier.weight(1f)) { Text(if(solving) "Solving..." else "Undo all") }
    OutlinedButton(onClick={cube=cube.reset()},enabled=!solving,modifier=Modifier.weight(1f)) { Text("Reset") }
   }
  } }
  Spacer(Modifier.height(12.dp))
 }
}
@Composable internal fun PuzzleSizeMenu(size:Int,onSelect:(Int)->Unit) {
 var open by remember { mutableStateOf(false) };Box { OutlinedButton(onClick={open=true}) { Text("${size}x${size}") };DropdownMenu(expanded=open,onDismissRequest={open=false}) { (2..7).forEach { value -> DropdownMenuItem(text={Text("${value}x${value} cube")},onClick={open=false;onSelect(value)}) } } }
}
