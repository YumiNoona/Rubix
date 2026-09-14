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


@Composable internal fun Heading(eyebrow: String,title: String,body: String="") {
 Text(eyebrow,style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.primary)
 Spacer(Modifier.height(12.dp)); Text(title,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.SemiBold)
 if(body.isNotEmpty()) { Spacer(Modifier.height(12.dp)); Text(body,style=MaterialTheme.typography.bodyLarge,color=MaterialTheme.colorScheme.onSurfaceVariant) }
}
@Composable internal fun Primary(text: String,enabled: Boolean=true,onClick: ()->Unit) {
 val feedback=rememberTouchFeedback()
 Button({ feedback(); onClick() },Modifier.fillMaxWidth().heightIn(min=56.dp),enabled=enabled,shape=RoundedCornerShape(18.dp)) { Text(text,style=MaterialTheme.typography.titleMedium) }
}
@Composable internal fun CubeNet(cube: CubeState,uncertain: Set<Int>,select: (Int)->Unit) {
 val initials=LocalAppPreferences.current.initials
 val rows=listOf(listOf(null,Face.U,null,null),listOf(Face.L,Face.F,Face.R,Face.B),listOf(null,Face.D,null,null))
 Column(Modifier.fillMaxWidth(),verticalArrangement=Arrangement.spacedBy(5.dp)) { rows.forEach { faces ->
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(5.dp)) { faces.forEach { face ->
   Box(Modifier.weight(1f).aspectRatio(1f)) { if(face!=null) Column(verticalArrangement=Arrangement.spacedBy(2.dp)) { (0..2).forEach { r ->
    Row(Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(2.dp)) { (0..2).forEach { c ->
     val index=face.ordinal*9+r*3+c
     Box(Modifier.weight(1f).fillMaxHeight().background(Color(cube.stickers[index].argb),RoundedCornerShape(3.dp)).border(if(index in uncertain) 2.dp else 0.5.dp,if(index in uncertain) Color.Magenta else Color.Black.copy(alpha=0.2f),RoundedCornerShape(3.dp)).clickable { select(index) }.semantics { contentDescription="${face.label} row ${r+1} column ${c+1}, ${cube.stickers[index].label}" },contentAlignment=Alignment.Center) { if(initials || r==1 && c==1) Text(if(initials) cube.stickers[index].initial else face.name,color=Color(cube.stickers[index].inkArgb),style=MaterialTheme.typography.labelSmall,fontWeight=FontWeight.Bold) }
    } }
   } } }
  } }
 } }
}
@Composable internal fun LargeFace(cube: CubeState,face: Face,uncertain: Set<Int>,select: (Int)->Unit) {
 val initials=LocalAppPreferences.current.initials
 Column(Modifier.fillMaxWidth().padding(horizontal=40.dp),verticalArrangement=Arrangement.spacedBy(6.dp)) { (0..2).forEach { r ->
  Row(horizontalArrangement=Arrangement.spacedBy(6.dp)) { (0..2).forEach { c ->
   val index=face.ordinal*9+r*3+c
   Box(Modifier.weight(1f).aspectRatio(1f).heightIn(min=48.dp).background(Color(cube.stickers[index].argb),RoundedCornerShape(10.dp)).border(if(index in uncertain) 3.dp else 1.dp,if(index in uncertain) Color.Magenta else Color.Black.copy(alpha=0.15f),RoundedCornerShape(10.dp)).clickable { select(index) }.semantics { contentDescription="${face.label} row ${r+1} column ${c+1}, ${cube.stickers[index].label}" },contentAlignment=Alignment.Center) { if(initials) Text(cube.stickers[index].initial,color=Color(cube.stickers[index].inkArgb),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleLarge) }
  } }
 } }
}
@Composable internal fun HoldingLabels(front: CubeColor,top: CubeColor) {
 Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(12.dp)) {
  listOf("FACING YOU" to front,"ON TOP" to top).forEach { (label,color) ->
   Surface(Modifier.weight(1f),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
    Column(Modifier.padding(16.dp),horizontalAlignment=Alignment.CenterHorizontally) {
     Text(label,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
     Spacer(Modifier.height(10.dp)); Box(Modifier.size(24.dp).background(Color(color.argb),RoundedCornerShape(6.dp)))
     Spacer(Modifier.height(8.dp)); Text(color.label,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
    }
   }
  }
 }
}

@Composable internal fun TurnArrow(inverse: Boolean,modifier: Modifier=Modifier) {
 val color=MaterialTheme.colorScheme.primary
 Canvas(modifier.semantics { contentDescription=if(inverse) "Counter-clockwise turn" else "Clockwise turn" }) {
  val radius=minOf(size.width,size.height)*0.32f
  val center=Offset(size.width/2,size.height/2)
  val start=if(inverse) 35f else 145f
  val sweep=if(inverse) -270f else 270f
  drawArc(color,start,sweep,false,topLeft=Offset(center.x-radius,center.y-radius),size=androidx.compose.ui.geometry.Size(radius*2,radius*2),style=Stroke(3.dp.toPx()))
  val angle=(start+sweep)*kotlin.math.PI.toFloat()/180
  val tip=Offset(center.x+radius*kotlin.math.cos(angle),center.y+radius*kotlin.math.sin(angle))
  val direction=angle+if(inverse) -kotlin.math.PI.toFloat()/2 else kotlin.math.PI.toFloat()/2
  val length=8.dp.toPx()
  val path=Path().apply {
   moveTo(tip.x,tip.y)
   lineTo(tip.x-length*kotlin.math.cos(direction-0.55f),tip.y-length*kotlin.math.sin(direction-0.55f))
   lineTo(tip.x-length*kotlin.math.cos(direction+0.55f),tip.y-length*kotlin.math.sin(direction+0.55f))
   close()
  }
  drawPath(path,color)
 }
}

@Composable internal fun RecoveryOption(label: String,onClick: () -> Unit) {
 OutlinedButton(onClick=onClick,modifier=Modifier.fillMaxWidth().heightIn(min=48.dp),shape=RoundedCornerShape(14.dp)) { Text(label,textAlign=TextAlign.Center) }
}
@Composable internal fun DisplaySettings(onDismiss: () -> Unit) {
 val preferences=LocalAppPreferences.current
 val feedback=rememberTouchFeedback()
 AlertDialog(onDismissRequest=onDismiss,title={Text("Display & feedback")},text={Column(Modifier.heightIn(max=320.dp).verticalScroll(rememberScrollState())) {
  Row(verticalAlignment=Alignment.CenterVertically) { Text("Color initials",modifier=Modifier.weight(1f)); Switch(checked=preferences.initials,onCheckedChange={feedback();preferences.updateInitials(it)}) }
  Text("W white, R red, G green, Y yellow, O orange, B blue",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Spacer(Modifier.height(12.dp))
  Row(verticalAlignment=Alignment.CenterVertically) { Text("Haptic feedback",modifier=Modifier.weight(1f)); Switch(checked=preferences.haptics,onCheckedChange={preferences.updateHaptics(it);feedback()}) }
  Text("Gentle feedback for taps, captured faces and completion. Respects your phone settings.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
 }},confirmButton={TextButton(onClick=onDismiss) { Text("Done") }})
}
