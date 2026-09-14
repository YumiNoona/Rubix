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


@Composable internal fun Review(vm: CubeViewModel) {
 val preferences=LocalAppPreferences.current
 val feedback=rememberTouchFeedback()
 val correcting=vm.screen==Screen.CORRECT
 var selected by remember { mutableStateOf<Int?>(null) }
 var face by remember { mutableStateOf(Face.U) }
 var editing by remember { mutableStateOf(vm.manualEntry || correcting) }
 var paint by remember { mutableStateOf(vm.manualEntry) }
 var brush by remember { mutableStateOf(CubeColor.WHITE) }
 val selectSticker: (Int)->Unit = { index -> if(!vm.busy) { feedback(); if(vm.manualEntry && index%9==4) Unit else if(paint) vm.edit(index,brush) else selected=index } }
 val issue=vm.validationIssue
 val highlighted=vm.lowConfidence+(issue?.suspectStickers ?: emptyList())
 LaunchedEffect(vm.lowConfidence) { vm.lowConfidence.firstOrNull()?.let { face=Face.entries[it/9] } }
 Column(Modifier.fillMaxSize()) {
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
   Heading(if(correcting) "CURRENT STATE" else if(vm.manualEntry) "COLOR ENTRY" else "SIX SIDES CAPTURED",if(correcting) "Match your cube." else if(vm.manualEntry) "Paint your cube." else "Does it match?",if(vm.manualEntry) "Choose a color, then tap stickers. Centers stay fixed." else "Check the stickers against the cube in your hands.")
   CubeView(vm.cube,modifier=Modifier.fillMaxWidth().height(210.dp).semantics { contentDescription="3D preview of your scanned cube. Drag to inspect all sides." })
   CubeNet(vm.cube,highlighted,selectSticker)
   Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text("Color initials",modifier=Modifier.weight(1f),style=MaterialTheme.typography.bodyMedium); Switch(checked=preferences.initials,onCheckedChange={feedback(); preferences.updateInitials(it)}) }
   Spacer(Modifier.height(16.dp))
   TextButton(onClick={editing=!editing},enabled=!vm.busy,modifier=Modifier.fillMaxWidth()) { Text(if(editing) "Done editing" else "Edit colors") }
   if(editing) {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text("Paint stickers",modifier=Modifier.weight(1f)); Switch(checked=paint,onCheckedChange={paint=it}) }
    if(paint) Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
     CubeColor.entries.forEach { color -> Box(Modifier.weight(1f).aspectRatio(1f).background(Color(preferences.color(color)),RoundedCornerShape(8.dp)).border(if(brush==color) 3.dp else 0.dp,MaterialTheme.colorScheme.primary,RoundedCornerShape(8.dp)).clickable(enabled=!vm.busy) { brush=color;feedback() }.semantics { contentDescription="Paint ${color.label.lowercase()} stickers" },contentAlignment=Alignment.Center) { Text(color.initial,color=Color(preferences.ink(color)),fontWeight=FontWeight.Bold) } }
    }
    TextButton(onClick=vm::undoEdit,enabled=vm.canUndoEdit && !vm.busy) { Text("Undo color change") }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
     Face.entries.forEach { f -> FilterChip(selected=face==f,onClick={face=f},enabled=!vm.busy,label={Text("${f.name} / ${vm.cube.stickers[f.ordinal*9+4].label}")}) }
    }
    Spacer(Modifier.height(8.dp))
    LargeFace(vm.cube,face,highlighted,selectSticker)
    if(!correcting) Row {
     TextButton(onClick={vm.rotateFace(face)},enabled=!vm.busy,modifier=Modifier.weight(1f)) { Text("Rotate scan") }
     TextButton(onClick={vm.scan(face)},enabled=!vm.busy,modifier=Modifier.weight(1f)) { Text("Rescan face") }
    }
   }
   Spacer(Modifier.height(12.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { CubeColor.entries.forEach { color -> val count=vm.cube.stickers.count { it==color }; Text("${color.initial} $count/9",style=MaterialTheme.typography.labelSmall,color=if(count==9) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error) } }
   Spacer(Modifier.height(16.dp))
  }
  Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=RoundedCornerShape(16.dp),modifier=Modifier.fillMaxWidth()) {
   Column(Modifier.padding(14.dp)) {
    Text(if(vm.busy) "Preparing your guide..." else if(issue==null) " Ready to solve" else "Your scan needs a check",fontWeight=FontWeight.SemiBold,color=if(issue==null || vm.busy) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    Spacer(Modifier.height(4.dp))
    Text(if(vm.busy) vm.message else issue?.message ?: if(correcting) "Colors are valid. Recalculate from your current cube." else "All six faces checked. Choose your starting position next.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.heightIn(max=120.dp).verticalScroll(rememberScrollState()))
    if(vm.busy) { Spacer(Modifier.height(8.dp)); LinearProgressIndicator(modifier=Modifier.fillMaxWidth()) }
   }
  }
  Spacer(Modifier.height(10.dp))
  Primary(if(vm.busy) "Preparing your guide..." else if(correcting) "Update solution" else "Solve this cube",!vm.busy) { selected=null; vm.solve() }
  if(correcting) TextButton(onClick=vm::cancelCorrection,enabled=!vm.busy,modifier=Modifier.fillMaxWidth()) { Text("Cancel changes") }
  Spacer(Modifier.height(12.dp))
 }
 vm.solveError?.let { error ->
  AlertDialog(
   onDismissRequest=vm::dismissSolveError,
   title={Text("Check the scan before solving")},
   text={Text(error)},
   confirmButton={TextButton(onClick={vm.dismissSolveError(); editing=true; highlighted.firstOrNull()?.let { face=Face.entries[it/9] }}) { Text("Check colors") }},
   dismissButton={TextButton(onClick={vm.dismissSolveError(); vm.scan()}) { Text("Rescan cube") }}
  )
 }

 selected?.let { index -> AlertDialog(onDismissRequest={selected=null},title={Text("${Face.entries[index/9].label.replaceFirstChar { it.uppercase() }}  /  row ${index%9/3+1}, column ${index%3+1}")},text={
  Column { CubeColor.entries.forEach { c -> TextButton(onClick={feedback(); vm.edit(index,c);selected=null},enabled=!vm.busy,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) { Box(Modifier.size(24.dp).background(Color(LocalAppPreferences.current.color(c)),RoundedCornerShape(6.dp))); Spacer(Modifier.width(16.dp)); Text(c.label) } } }
 },confirmButton={TextButton(onClick={selected=null}) { Text("Cancel") }}) }
}
