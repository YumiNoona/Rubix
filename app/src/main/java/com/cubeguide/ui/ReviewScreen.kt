package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ErrorOutline
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
 if(vm.manualEntry && vm.screen!=Screen.CORRECT) { ManualEditor(vm);return }
 val feedback=rememberTouchFeedback()
 val preferences=LocalAppPreferences.current
 val correcting=vm.screen==Screen.CORRECT
 var selected by rememberSaveable { mutableStateOf<Int?>(null) }
 var faceOrdinal by rememberSaveable { mutableIntStateOf(vm.lowConfidence.firstOrNull()?.div(9) ?: Face.U.ordinal) }
 val face=Face.entries[faceOrdinal]
 val editing=correcting
 var show3D by rememberSaveable { mutableStateOf(false) }
 val selectSticker: (Int)->Unit = { index -> if(!vm.busy) { feedback();faceOrdinal=index/9;if(correcting && index%9!=4) selected=index else if(!correcting) vm.beginEdit(Face.entries[index/9]) } }
 val issue=vm.validationIssue
 val highlighted=vm.lowConfidence+(issue?.suspectStickers ?: emptyList())
 val uncertainCount=vm.lowConfidence.size
 Column(Modifier.fillMaxSize()) {
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) {
   Text(if(correcting) "Match this state to the cube in your hands." else "Check all six sides before solving.",color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2)
   Spacer(Modifier.height(12.dp))
   if(!editing) {
    SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
     SegmentedButton(selected=!show3D,onClick={show3D=false},shape=SegmentedButtonDefaults.itemShape(0,2),label={Text("Net")})
     SegmentedButton(selected=show3D,onClick={show3D=true},shape=SegmentedButtonDefaults.itemShape(1,2),label={Text("3D")})
    }
    if(show3D) CubeView(vm.cube,modifier=Modifier.fillMaxWidth().height(290.dp).semantics { contentDescription="3D preview of your scanned cube. Drag to inspect all sides." })
    else CubeNet(vm.cube,highlighted,selectSticker)
   } else {
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
     Text("Edit one face",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,modifier=Modifier.weight(1f))
     Spacer(Modifier.width(1.dp))
    }
    Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),horizontalArrangement=Arrangement.spacedBy(6.dp)) {
     Face.entries.forEach { f -> FilterChip(selected=face==f,onClick={faceOrdinal=f.ordinal},enabled=!vm.busy,label={Text("${f.name} / ${vm.cube.stickers[f.ordinal*9+4].label}")}) }
    }
    Spacer(Modifier.height(8.dp))
    LargeFace(vm.cube,face,highlighted,select=selectSticker)
    Text("Tap a sticker to choose its color. Centers stay fixed.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(vertical=8.dp))
    Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) { Text("Show color initials",modifier=Modifier.weight(1f));Switch(preferences.initials,{preferences.updateInitials(it)}) }
    TextButton(onClick=vm::undoEdit,enabled=vm.canUndoEdit && !vm.busy) { Text("Undo color change") }
    if(!correcting) Row {
     TextButton(onClick={vm.rotateFace(face)},enabled=!vm.busy,modifier=Modifier.weight(1f)) { Text("Rotate scan") }
     TextButton(onClick={vm.scan(face)},enabled=!vm.busy,modifier=Modifier.weight(1f)) { Text("Rescan face") }
    }
   }
   Spacer(Modifier.height(12.dp))
   Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { CubeColor.entries.forEach { color -> val count=vm.cube.stickers.count { it==color }; Text("${color.initial} $count",style=MaterialTheme.typography.labelSmall,color=if(count==9) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error,maxLines=1) } }
   Spacer(Modifier.height(16.dp))
  }
  Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=RubixTokens.cardShape,modifier=Modifier.fillMaxWidth(),border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.65f))) {
   Column(Modifier.padding(14.dp)) {
    Row(verticalAlignment=Alignment.CenterVertically) {
     Icon(if(issue==null) Icons.Rounded.CheckCircle else Icons.Rounded.ErrorOutline,null,Modifier.size(20.dp),tint=if(issue==null) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error)
     Spacer(Modifier.width(9.dp));Text(if(vm.busy) "Preparing guide" else if(issue!=null) "Check this scan" else if(uncertainCount>0) "$uncertainCount to review" else "Ready to solve",fontWeight=FontWeight.SemiBold,color=if(issue==null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
    }
    Spacer(Modifier.height(4.dp))
    Text(if(vm.busy) vm.message else issue?.message ?: if(uncertainCount>0) "Glare made the outlined colors less certain. Check them, or solve if the cube preview matches." else if(correcting) "Colors are valid. Recalculate from your current cube." else "All six faces checked. Choose your starting position next.",style=MaterialTheme.typography.bodySmall,modifier=Modifier.heightIn(max=120.dp).verticalScroll(rememberScrollState()))
    if(vm.busy) { Spacer(Modifier.height(8.dp)); LinearProgressIndicator(modifier=Modifier.fillMaxWidth()) }
   }
  }
  Spacer(Modifier.height(10.dp))
  if(correcting) {
   Primary(if(vm.busy) "Preparing your guide..." else "Update solution",!vm.busy) { selected=null;vm.solve() }
   TextButton(onClick=vm::cancelCorrection,enabled=!vm.busy,modifier=Modifier.fillMaxWidth()) { Text("Cancel changes") }
  } else Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
   OutlinedButton(onClick={vm.beginEdit(Face.entries[faceOrdinal])},enabled=!vm.busy,modifier=Modifier.weight(1f).height(54.dp),shape=RubixTokens.controlShape) { Icon(Icons.Rounded.Edit,null,Modifier.size(19.dp));Spacer(Modifier.width(7.dp));Text("Edit",maxLines=1) }
   Button(onClick={selected=null;vm.solve()},enabled=!vm.busy,modifier=Modifier.weight(1f).height(54.dp),shape=RubixTokens.controlShape) { Icon(Icons.Rounded.CheckCircle,null,Modifier.size(19.dp));Spacer(Modifier.width(7.dp));Text(if(vm.busy) "Checking…" else "Continue",maxLines=1) }
  }
  Spacer(Modifier.height(12.dp))
 }
 vm.solveError?.let { error ->
  AlertDialog(
   onDismissRequest=vm::dismissSolveError,
   title={Text("Check the scan before solving")},
   text={Text(error)},
   confirmButton={TextButton(onClick={vm.dismissSolveError();val target=highlighted.firstOrNull()?.div(9) ?: faceOrdinal;if(correcting) faceOrdinal=target else vm.beginEdit(Face.entries[target])}) { Text("Check colors") }},
   dismissButton={TextButton(onClick={vm.dismissSolveError(); vm.scan()}) { Text("Rescan cube") }}
  )
 }

 selected?.let { index -> StickerColorPicker(
  index=index,current=vm.cube.stickers[index],enabled=!vm.busy,
  onSelect={color -> feedback(); vm.edit(index,color);selected=null},onDismiss={selected=null}
 ) }
}
