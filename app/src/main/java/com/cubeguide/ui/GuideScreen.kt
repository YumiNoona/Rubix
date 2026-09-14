package com.cubeguide.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cubeguide.camera.CameraPreview
import com.cubeguide.core.*
import com.cubeguide.rendering.CubeView


@Composable internal fun Guide(vm: CubeViewModel) {
 val feedback=rememberTouchFeedback()
 var viewReset by remember { mutableIntStateOf(0) }
 var settings by remember { mutableStateOf(false) }
 var replay by remember { mutableIntStateOf(0) }
 var menu by remember { mutableStateOf(false) }
 var previous by remember { mutableStateOf(false) }
 var restart by remember { mutableStateOf(false) }
 var holdHelp by remember { mutableStateOf(false) }
 var recovery by remember { mutableStateOf<String?>(null) }
 var correctingPrevious by remember { mutableStateOf(false) }
 var recoveryFace by remember { mutableStateOf(Face.F) }
 var recoveryKind by remember { mutableStateOf("direction") }
 val move=vm.moves.getOrNull(vm.step) ?: return
 BoxWithConstraints(Modifier.fillMaxSize()) {
  val cubeHeight=(maxHeight*0.44f).coerceIn(160.dp,280.dp)
  Column(Modifier.fillMaxSize()) {
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    Text("${if(vm.isReplay) "REPLAY / " else ""}STEP ${vm.step+1} OF ${vm.moves.size}",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary,modifier=Modifier.weight(1f))
    Box {
     TextButton(onClick={menu=true}) { Text("More") }
     DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
      DropdownMenuItem(text={Text("Replay animation")},onClick={menu=false;replay++})
      DropdownMenuItem(text={Text("Reset cube view")},onClick={menu=false;viewReset++})
      DropdownMenuItem(text={Text("Display & feedback")},onClick={menu=false;settings=true})
      if(!vm.isReplay) DropdownMenuItem(text={Text("Correct cube colors")},onClick={menu=false;vm.beginCorrection()},enabled=!vm.busy)
      DropdownMenuItem(text={Text("How to hold the cube")},onClick={menu=false;holdHelp=true})
      DropdownMenuItem(text={Text("Restart guide")},onClick={menu=false;if(vm.isReplay) vm.restart() else restart=true})
      if(!vm.isReplay) DropdownMenuItem(text={Text("I made a mistake")},onClick={menu=false;recovery="kind"},enabled=!vm.busy)
     }
    }
   }
   LinearProgressIndicator(progress={vm.step.toFloat()/vm.moves.size},modifier=Modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.surfaceContainerHighest)
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
    CubeView(vm.cube,Modifier.fillMaxWidth().height(cubeHeight).semantics { contentDescription=move.instruction+". Drag to inspect the cube." },move,replay,viewReset)
    Text("${vm.cube.stickers[move.face.ordinal*9+4].label.uppercase()} / ${move.face.label.uppercase()} FACE",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary,textAlign=TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text(move.instruction,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center)
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center,modifier=Modifier.fillMaxWidth().padding(vertical=10.dp)) {
     TurnArrow(move.turns==3,Modifier.size(36.dp))
     Spacer(Modifier.width(10.dp)); Text(move.notation,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if(vm.isReplay) Text("Replay only - no physical turns needed.",style=MaterialTheme.typography.bodySmall,textAlign=TextAlign.Center)
    if(vm.busy) { CircularProgressIndicator(Modifier.size(24.dp)); Text("Updating the solution...",style=MaterialTheme.typography.bodySmall) }
    if(vm.solveError!=null) Text(vm.solveError!!,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
   }
   Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
    Text("${vm.initial.stickers[22].label} facing you / ${vm.initial.stickers[4].label} on top",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
     OutlinedButton(onClick={feedback();if(vm.isReplay) vm.back() else previous=true},enabled=vm.step>0 && !vm.busy,modifier=Modifier.weight(1f).heightIn(min=56.dp),shape=RoundedCornerShape(18.dp)) { Text("Previous") }
     Button(onClick={feedback();vm.next()},enabled=!vm.busy,modifier=Modifier.weight(1f).heightIn(min=56.dp),shape=RoundedCornerShape(18.dp)) { Text("Next") }
    }
   }
  }
 }
 if(settings) DisplaySettings { settings=false }
 if(holdHelp) AlertDialog(onDismissRequest={holdHelp=false},title={Text("Keep this position")},text={Column(Modifier.heightIn(max=360.dp).verticalScroll(rememberScrollState())) {
  HoldingLabels(vm.initial.stickers[22],vm.initial.stickers[4])
  Spacer(Modifier.height(16.dp)); Text("Clockwise is viewed straight at the face you are turning. Drag the 3D cube to see hidden layers.")
 }},confirmButton={TextButton(onClick={holdHelp=false}) { Text("Got it") }})
 if(previous) AlertDialog(onDismissRequest={previous=false},title={Text("Undo the last turn")},text={Column(Modifier.heightIn(max=360.dp).verticalScroll(rememberScrollState())) {
  CubeView(vm.cube,modifier=Modifier.fillMaxWidth().height(160.dp),move=vm.moves[vm.step-1].inverse())
  Text(vm.moves[vm.step-1].inverse().instruction+". Confirm once your physical cube matches.")
 }},confirmButton={TextButton(onClick={previous=false;vm.back()}) { Text("I've undone it") }},dismissButton={TextButton(onClick={previous=false}) { Text("Cancel") }})
 if(restart) AlertDialog(onDismissRequest={restart=false},title={Text("Restart from your current cube?")},text={Text("Rescan your current cube for a fresh guide. To replay the original solution, first undo all confirmed turns.")},confirmButton={TextButton(onClick={restart=false;vm.scan()}) { Text("Rescan cube") }},dismissButton={TextButton(onClick={restart=false;recovery="undo-list"}) { Text("Show undo turns") }})
 if(recovery!=null) AlertDialog(onDismissRequest={recovery=null},title={Text(when(recovery) {
  "kind" -> "What happened?"
  "when" -> "Which instruction?"
  "face" -> "Which face did you turn?"
  "turn" -> "Which direction?"
  else -> "Return to the start"
 })},text={Column(Modifier.heightIn(max=320.dp).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(6.dp)) {
  fun pick(kind: String) { recoveryKind=kind; correctingPrevious=false; recovery=if(vm.step>0) "when" else if(kind=="direction") null else "face"; if(vm.step==0 && kind=="direction") vm.recover(move.inverse()) }
  when(recovery) {
   "kind" -> {
    RecoveryOption("Wrong direction") { pick("direction") }
    if(vm.step>0) RecoveryOption("Repeated the previous turn") { recovery=null;vm.recover(vm.moves[vm.step-1]) }
    RecoveryOption("A different turn") { pick("different") }
    RecoveryOption("I'm not sure - rescan") { recovery=null;vm.scan() }
   }
   "when" -> {
    RecoveryOption("The instruction on screen") { correctingPrevious=false; if(recoveryKind=="direction") { recovery=null;vm.recover(move.inverse()) } else recovery="face" }
    RecoveryOption("The last turn I confirmed") { correctingPrevious=true; if(recoveryKind=="direction") { recovery=null;vm.recoverPrevious(vm.moves[vm.step-1].inverse()) } else recovery="face" }
   }
   "face" -> Face.entries.forEach { f -> RecoveryOption("${vm.cube.stickers[f.ordinal*9+4].label} / ${f.label} face") { recoveryFace=f;recovery="turn" } }
   "turn" -> listOf("Clockwise" to 1,"Counter-clockwise" to 3,"Twice" to 2).forEach { (label,turns) -> RecoveryOption(label) { recovery=null;val actual=Move(recoveryFace,turns);if(correctingPrevious) vm.recoverPrevious(actual) else vm.recover(actual) } }
   "undo-list" -> {
    Text(vm.moves.take(vm.step).asReversed().joinToString("  ") { it.inverse().notation })
    RecoveryOption("All turns undone - restart") { recovery=null;vm.restart() }
   }
  }
 }},confirmButton={TextButton(onClick={recovery=null}) { Text("Cancel") }})
}
