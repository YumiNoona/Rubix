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


@Composable internal fun Home(vm: CubeViewModel) {
 var settings by remember { mutableStateOf(false) }
 val preferences=LocalAppPreferences.current
 val scramble=remember { Move.parse("R U F2 L D B2") }
 val solution=remember { scramble.reversed().map { it.inverse() } }
 var cube by remember { mutableStateOf(CubeState.solved().apply(scramble)) }
 var turn by remember { mutableStateOf<Move?>(null) }
 var caption by remember { mutableStateOf("From mixed up...") }
 LaunchedEffect(preferences.animationMillis) {
  while(true) {
   turn=null; cube=CubeState.solved().apply(scramble); caption="From mixed up..."
   kotlinx.coroutines.delay(1000)
   caption="One turn at a time."
   for(move in solution) {
    turn=move
    kotlinx.coroutines.delay(preferences.animationMillis.toLong()+180)
    turn=null; cube=cube.apply(move)
    kotlinx.coroutines.delay(100)
   }
   caption="Back to solved."
   kotlinx.coroutines.delay(2200)
  }
 }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
  Spacer(Modifier.height(18.dp))
  Text("Find your next turn.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold,textAlign=TextAlign.Center)
  Spacer(Modifier.height(8.dp))
  Text("A clear path from scramble to solved.",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
  CubeView(cube,move=turn,modifier=Modifier.fillMaxWidth().height(270.dp).semantics { contentDescription="A scrambled cube animates into a solved cube. Drag to inspect." })
  Text(caption,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
  Spacer(Modifier.height(24.dp))
  Primary("Scan your cube") { vm.scan() }
  Spacer(Modifier.height(10.dp))
  OutlinedButton(onClick={vm.manual()},modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(18.dp)) { Text("Enter colors") }
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
   TextButton(onClick={vm.demo()}) { Text("Try a demo") }
   TextButton(onClick={settings=true}) { Text("Settings") }
  }
  Spacer(Modifier.height(12.dp))
 }
 if(settings) DisplaySettings { settings=false }
}
