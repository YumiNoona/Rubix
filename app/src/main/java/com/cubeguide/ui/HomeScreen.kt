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
 var legal by remember { mutableStateOf(false) }
 val context=LocalContext.current
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
  Spacer(Modifier.height(22.dp)); Heading("A LITTLE ORDER, ONE TURN AT A TIME","Unmix your cube.","Scan six sides. Follow the turns. Find your way back to solved.")
  CubeView(CubeState.solved(),modifier=Modifier.fillMaxWidth().height(280.dp).semantics { contentDescription="Interactive solved Rubik's cube. Drag to rotate." })
  Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly) { listOf("01  Scan","02  Solve","03  Follow").forEach { Text(it,style=MaterialTheme.typography.labelLarge) } }
  Spacer(Modifier.height(28.dp)); Primary("Scan my cube") { vm.scan() }
  TextButton(onClick={vm.manual()},modifier=Modifier.fillMaxWidth()) { Text("Enter colors manually") }
  TextButton(onClick={vm.demo()},modifier=Modifier.fillMaxWidth()) { Text("Try a practice solve") }
  Text("For standard 3 x 3 cubes. Offline and private.",textAlign=TextAlign.Center,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.fillMaxWidth().padding(vertical=16.dp))
  TextButton(onClick={settings=true},modifier=Modifier.fillMaxWidth()) { Text("Display & feedback") }
  TextButton(onClick={legal=true},modifier=Modifier.fillMaxWidth()) { Text("Privacy & open-source licenses") }
 }
 if(settings) DisplaySettings { settings=false }
 if(legal) {
  val notices=remember { context.assets.open("THIRD_PARTY_NOTICES.txt").bufferedReader().use { it.readText() }+"\n"+context.assets.open("OPENCV_LICENSE.txt").bufferedReader().use { it.readText() } }
  AlertDialog(onDismissRequest={legal=false},title={Text("Private by design")},text={Column(Modifier.heightIn(max=400.dp).verticalScroll(rememberScrollState())) { Text("Your camera frames are analyzed in memory on this phone, then discarded. No network permission, uploads, accounts, analytics or tracking. Cube Guide uses the following open-source software.\n\n"+notices,style=MaterialTheme.typography.bodySmall) }},confirmButton={TextButton(onClick={legal=false}) { Text("Close") }})
 }
}
