package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import com.cubeguide.rendering.CubeView

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun Guide(vm: CubeViewModel,onBack:()->Unit) {
 val feedback=rememberTouchFeedback()
 val preferences=LocalAppPreferences.current
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
 var animationProgress by remember(vm.cube,vm.step) { mutableFloatStateOf(0f) }
 var autoPlay by rememberSaveable { mutableStateOf(true) }
 val autoAdvance=remember { Animatable(0f) }
 val move=vm.moves.getOrNull(vm.step) ?: return
 LaunchedEffect(vm.step,animationProgress,autoPlay,vm.busy) {
  if(autoPlay && !vm.busy && animationProgress>=0.99f) {
   autoAdvance.snapTo(0f)
   autoAdvance.animateTo(1f,tween(preferences.guideDelayMillis,easing=LinearEasing))
   if(autoPlay && !vm.busy && animationProgress>=0.99f) { feedback();vm.next() }
  } else autoAdvance.snapTo(0f)
 }
 BoxWithConstraints(Modifier.fillMaxSize()) {
  val cubeHeight=(maxHeight*0.44f).coerceIn(160.dp,280.dp)
  Column(Modifier.fillMaxSize()) {
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
    IconButton(onClick={feedback();autoPlay=false;onBack()},modifier=Modifier.offset(x=(-14).dp).size(48.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back",Modifier.size(26.dp)) }
    Text("${vm.step+1} of ${vm.moves.size}",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f),textAlign=TextAlign.Center)
    Row(verticalAlignment=Alignment.CenterVertically) {
     IconButton(onClick={feedback();autoPlay=!autoPlay}) { Icon(if(autoPlay) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,if(autoPlay) "Pause autoplay" else "Start autoplay") }
     IconButton(onClick={autoPlay=false;menu=true}) { Icon(Icons.Rounded.MoreVert,"More options") }
    }
   }
   LinearProgressIndicator(progress={((vm.step+animationProgress)/vm.moves.size).coerceIn(0f,1f)},modifier=Modifier.fillMaxWidth(),color=MaterialTheme.colorScheme.primary,trackColor=MaterialTheme.colorScheme.surfaceContainerHighest)
   if(autoPlay && animationProgress>=0.99f) LinearProgressIndicator(progress={autoAdvance.value},modifier=Modifier.fillMaxWidth().padding(top=4.dp),color=MaterialTheme.colorScheme.secondary,trackColor=MaterialTheme.colorScheme.surface)
   Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
    CubeView(vm.cube,Modifier.fillMaxWidth().height(cubeHeight).semantics { contentDescription=move.instruction+". Drag to inspect the cube." },move,replay,viewReset,onAnimationProgress={animationProgress=it})
    Text("${vm.cube.stickers[move.face.ordinal*9+4].label.uppercase()} / ${move.face.label.uppercase()} FACE",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary,textAlign=TextAlign.Center)
    Spacer(Modifier.height(12.dp))
    Text(move.instruction,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center)
    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center,modifier=Modifier.fillMaxWidth().padding(vertical=10.dp)) {
     TurnArrow(move.turns==3,Modifier.size(36.dp))
     Spacer(Modifier.width(10.dp)); Text(move.notation,style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
    if(move.turns==2) {
     Text("2 quarter-turns / 180 degrees",style=MaterialTheme.typography.labelLarge)
     Row(horizontalArrangement=Arrangement.spacedBy(10.dp),modifier=Modifier.padding(vertical=12.dp)) {
      (1..2).forEach { quarter ->
       val reached=animationProgress>=if(quarter==1) 0.5f else 0.99f
       Surface(shape=RoundedCornerShape(12.dp),color=if(reached) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer) {
        Row(Modifier.padding(12.dp),verticalAlignment=Alignment.CenterVertically) { TurnArrow(false,Modifier.size(24.dp));Spacer(Modifier.width(8.dp));Text("Turn $quarter of 2",style=MaterialTheme.typography.labelMedium) }
       }
      }
     }
    } else Text(if(move.turns==3) "One counter-clockwise quarter-turn" else "One clockwise quarter-turn",style=MaterialTheme.typography.bodySmall)
    if(vm.isReplay) Text("Replay only - no physical turns needed.",style=MaterialTheme.typography.bodySmall,textAlign=TextAlign.Center)
    if(vm.busy) { CircularProgressIndicator(Modifier.size(24.dp)); Text("Updating the solution...",style=MaterialTheme.typography.bodySmall) }
    if(vm.solveError!=null) Text(vm.solveError!!,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.error)
   }
   Column(Modifier.fillMaxWidth().padding(vertical=12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
    Text("${vm.initial.stickers[22].label} facing you / ${vm.initial.stickers[4].label} on top",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,textAlign=TextAlign.Center)
    Spacer(Modifier.height(10.dp))
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
     OutlinedButton(onClick={feedback();val wasAuto=autoPlay;autoPlay=false;if(vm.isReplay || wasAuto) vm.back() else previous=true},enabled=vm.step>0 && !vm.busy,modifier=Modifier.weight(1f).heightIn(min=54.dp),shape=RoundedCornerShape(17.dp)) { Text("Previous",maxLines=1) }
     Button(onClick={feedback();autoPlay=false;vm.next()},enabled=!vm.busy && animationProgress>=0.99f,modifier=Modifier.weight(1f).heightIn(min=54.dp),shape=RoundedCornerShape(17.dp)) { Text("Next",maxLines=1) }
    }
   }
  }
 }
 if(menu) ModalBottomSheet(onDismissRequest={menu=false},shape=RubixTokens.modalShape) {
  Column(Modifier.fillMaxWidth().padding(horizontal=20.dp).navigationBarsPadding()) {
   Text("Guide tools",style=MaterialTheme.typography.headlineSmall)
   Spacer(Modifier.height(12.dp))
   GuideAction(Icons.Rounded.Replay,"Replay move") { menu=false;replay++ }
   GuideAction(Icons.Rounded.CenterFocusStrong,"Reset cube view") { menu=false;viewReset++ }
   GuideAction(Icons.Rounded.Tune,"Guide settings") { menu=false;settings=true }
   GuideAction(Icons.AutoMirrored.Rounded.HelpOutline,"Holding help") { menu=false;holdHelp=true }
   if(!vm.isReplay) GuideAction(Icons.Rounded.Edit,"Correct colors",!vm.busy) { menu=false;vm.beginCorrection() }
   if(!vm.isReplay) GuideAction(Icons.Rounded.ReportProblem,"Fix a mistake",!vm.busy) { menu=false;recovery="kind" }
   GuideAction(Icons.Rounded.RestartAlt,"Restart guide") { menu=false;if(vm.isReplay) vm.restart() else restart=true }
   Spacer(Modifier.height(16.dp))
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

@Composable internal fun GuideAction(icon:androidx.compose.ui.graphics.vector.ImageVector,label:String,enabled:Boolean=true,onClick:()->Unit) {
 TextButton(onClick=onClick,enabled=enabled,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RubixTokens.controlShape) {
  Icon(icon,null,Modifier.size(22.dp));Spacer(Modifier.width(14.dp));Text(label,modifier=Modifier.weight(1f),textAlign=TextAlign.Start)
 }
}
