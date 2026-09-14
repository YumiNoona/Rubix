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


@Composable internal fun Scan(vm: CubeViewModel) {
 if(vm.scanIndex>0) CompletionFeedback(vm.scanIndex)
 val context=LocalContext.current
 var permission by remember { mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED) }
 val request=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { permission=it }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
  val color=CubeColor.entries[vm.pose.face.ordinal]; val top=CubeColor.entries[vm.pose.top.ordinal]
  Heading("SCAN ${vm.scanIndex+1} / 6","Show ${color.label.lowercase()}.",vm.pose.guidance)
  Spacer(Modifier.height(18.dp))
  if(permission) {
   Box(Modifier.fillMaxWidth().height(340.dp).background(Color.Black,RoundedCornerShape(24.dp))) {
    CameraPreview(Modifier.fillMaxSize(),vm::detection)
    Canvas(Modifier.fillMaxSize()) {
     if(vm.corners.size==4) {
      // Match the analysis image to the FIT_CENTER preview.
      val ratio=vm.cameraAspect; val w=minOf(size.width,size.height*ratio); val h=w/ratio; val left=(size.width-w)/2; val topOffset=(size.height-h)/2
      val points=vm.corners.map { Offset(left+it.first*w,topOffset+it.second*h) }
      val path=Path().apply { moveTo(points[0].x,points[0].y); points.drop(1).forEach { lineTo(it.x,it.y) }; close() }
      drawPath(path,Mint,style=Stroke(4.dp.toPx()))
     }
    }
    Text("${top.label.uppercase()} SIDE ABOVE",color=Color.White,style=MaterialTheme.typography.labelMedium,modifier=Modifier.align(Alignment.TopCenter).padding(14.dp).background(Color.Black.copy(alpha=0.6f)).padding(8.dp))
   }
   Spacer(Modifier.height(16.dp)); LinearProgressIndicator(progress={vm.progress},modifier=Modifier.fillMaxWidth())
   Spacer(Modifier.height(12.dp)); Text(vm.message.ifBlank { "Hold the face toward the camera." },style=MaterialTheme.typography.bodyLarge)
  } else {
   Spacer(Modifier.height(40.dp)); Text("Camera access lets Cube Guide recognize the stickers on your phone. Images are never uploaded.",style=MaterialTheme.typography.bodyLarge)
   Spacer(Modifier.height(24.dp)); Primary("Allow camera") { request.launch(Manifest.permission.CAMERA) }
  }
  Spacer(Modifier.height(12.dp)); TextButton(onClick={vm.manual()},modifier=Modifier.fillMaxWidth()) { Text("Use manual entry instead") }
 }
}
