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


@Composable internal fun Done(vm: CubeViewModel) {
 CompletionFeedback(vm.moves)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
  Spacer(Modifier.height(28.dp))
  val checkColor=MaterialTheme.colorScheme.primary
  Canvas(Modifier.size(64.dp).semantics { contentDescription="Solved" }) {
   drawLine(checkColor,Offset(size.width*0.18f,size.height*0.52f),Offset(size.width*0.42f,size.height*0.75f),6.dp.toPx())
   drawLine(checkColor,Offset(size.width*0.42f,size.height*0.75f),Offset(size.width*0.85f,size.height*0.25f),6.dp.toPx())
  }
  Text(if(vm.moves.isEmpty()) "Already solved." else "Order restored.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.SemiBold)
  CubeView(vm.cube,modifier=Modifier.fillMaxWidth().height(280.dp))
  Text("${vm.moves.size} moves  /  one solved cube",style=MaterialTheme.typography.titleMedium)
  Spacer(Modifier.height(26.dp)); Primary("Solve another cube") { vm.scan() }
  TextButton(onClick={vm.viewSolution()},enabled=vm.moves.isNotEmpty()) { Text("View solution again") }
  Text(vm.moves.joinToString("  ") { it.notation },style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center)
 }
}
