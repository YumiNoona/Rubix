package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import com.cubeguide.rendering.CubeView

@Composable internal fun Review(vm: CubeViewModel) {
 val preferences=LocalAppPreferences.current
 val feedback=rememberTouchFeedback()
 val correcting=vm.screen==Screen.CORRECT
 var selected by rememberSaveable { mutableStateOf<Int?>(null) }
 var faceOrdinal by rememberSaveable { mutableIntStateOf(vm.lowConfidence.firstOrNull()?.div(9) ?: Face.U.ordinal) }
 val face=Face.entries[faceOrdinal]
 var editing by rememberSaveable { mutableStateOf(vm.manualEntry || correcting) }
 var paint by rememberSaveable { mutableStateOf(vm.manualEntry) }
 var brushOrdinal by rememberSaveable { mutableIntStateOf(CubeColor.WHITE.ordinal) }
 val brush=CubeColor.entries[brushOrdinal]
 val selectSticker: (Int)->Unit = { index -> if(!vm.busy) { feedback(); faceOrdinal=index/9; if(vm.manualEntry && index%9==4) Unit else if(paint) vm.edit(index,brush) else selected=index } }
 val issue=vm.validationIssue
 val highlighted=vm.lowConfidence+(issue?.suspectStickers ?: emptyList())
 val uncertainCount=vm.lowConfidence.size
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
     CubeColor.entries.forEach { color -> Box(Modifier.weight(1f).aspectRatio(1f).background(Color(preferences.color(color)),RoundedCornerShape(8.dp)).border(if(brush==color) 3.dp else 0.dp,MaterialTheme.colorScheme.primary,RoundedCornerShape(8.dp)).clickable(enabled=!vm.busy) { brushOrdinal=color.ordinal;feedback() }.semantics { contentDescription="Paint ${color.label.lowercase()} stickers" },contentAlignment=Alignment.Center) { Text(color.initial,color=Color(preferences.ink(color)),fontWeight=FontWeight.Bold) } }
    }
    TextButton(onClick=vm::undoEdit,enabled=vm.canUndoEdit && !vm.busy) { Text("Undo color change") }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
     Face.entries.forEach { f -> FilterChip(selected=face==f,onClick={faceOrdinal=f.ordinal},enabled=!vm.busy,label={Text("${f.name} / ${vm.cube.stickers[f.ordinal*9+4].label}")}) }
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
    Text(if(vm.busy) "Preparing your guide..." else if(issue!=null) "Your scan needs a check" else if(uncertainCount>0) "$uncertainCount sticker${if(uncertainCount==1) "" else "s"} need a closer look" else "Ready to solve",fontWeight=FontWeight.SemiBold,color=if(issue==null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
    Spacer(Modifier.height(4.dp))
    Text(if(vm.busy) vm.message else issue?.message ?: if(uncertainCount>0) "Glare made the outlined colors less certain. Check them, or solve if the cube preview matches." else if(correcting) "Colors are valid. Recalculate from your current cube." else "All six faces checked. Choose your starting position next.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.heightIn(max=120.dp).verticalScroll(rememberScrollState()))
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
   confirmButton={TextButton(onClick={vm.dismissSolveError(); editing=true; highlighted.firstOrNull()?.let { faceOrdinal=it/9 }}) { Text("Check colors") }},
   dismissButton={TextButton(onClick={vm.dismissSolveError(); vm.scan()}) { Text("Rescan cube") }}
  )
 }

 selected?.let { index -> StickerColorPicker(
  index=index,current=vm.cube.stickers[index],enabled=!vm.busy,
  onSelect={color -> feedback(); vm.edit(index,color);selected=null},onDismiss={selected=null}
 ) }
}
