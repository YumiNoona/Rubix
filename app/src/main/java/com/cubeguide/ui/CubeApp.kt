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


internal val Mint=Color(0xFFA9F5C5)
@Composable fun CubeApp(vm: CubeViewModel=viewModel()) {
 val context=LocalContext.current
 val preferences=remember { AppPreferences(context) }
 val view=androidx.compose.ui.platform.LocalView.current
 DisposableEffect(vm.screen,preferences.keepAwake) {
  val previous=view.keepScreenOn
  view.keepScreenOn=preferences.keepAwake && vm.screen in listOf(Screen.SCAN,Screen.SETUP,Screen.GUIDE,Screen.CORRECT)
  onDispose { view.keepScreenOn=previous }
 }
 CompositionLocalProvider(LocalAppPreferences provides preferences) {
 val dark=isSystemInDarkTheme()
 val scheme=if(dark) darkColorScheme(primary=Mint,background=Color(0xFF0C1511),surface=Color(0xFF17241D),onPrimary=Color(0xFF102A1C)) else lightColorScheme(primary=Color(0xFF246642),background=Color(0xFFF5F7F1),surface=Color(0xFFE8EFE5))
 var settings by remember { mutableStateOf(false) }
 var exit by remember { mutableStateOf(false) }
 MaterialTheme(colorScheme=scheme) {
  Surface(Modifier.fillMaxSize()) {
   Column(Modifier.fillMaxSize().safeDrawingPadding().padding(horizontal=24.dp)) {
    Row(Modifier.fillMaxWidth().padding(vertical=16.dp),verticalAlignment=Alignment.CenterVertically) {
     Text(if(vm.screen==Screen.HOME) "rubix" else when(vm.screen) { Screen.SCAN -> "Capture"; Screen.REVIEW -> "Review"; Screen.CORRECT -> "Adjust"; Screen.SETUP -> "Get ready"; Screen.GUIDE -> "Your solve"; Screen.DONE -> "Complete"; else -> "rubix" },style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f))
     if(vm.screen!=Screen.HOME) { TextButton(onClick={settings=true}) { Text("Settings") }; TextButton(onClick={ exit=true }) { Text("Close") } }
    }
    Box(Modifier.weight(1f)) { when(vm.screen) {
     Screen.HOME -> Home(vm)
     Screen.SCAN -> Scan(vm)
     Screen.REVIEW -> Review(vm)
     Screen.CORRECT -> Review(vm)
     Screen.SETUP -> HoldingSetup(vm)
     Screen.GUIDE -> Guide(vm)
     Screen.DONE -> Done(vm)
    } }
   }
  }
  BackHandler(vm.screen!=Screen.HOME) { if(vm.screen==Screen.CORRECT && !vm.busy) vm.cancelCorrection() else exit=true }
  if(settings) DisplaySettings { settings=false }
  if(exit) AlertDialog(onDismissRequest={exit=false},title={Text("Leave this solve?")},text={Text("Your physical cube will stay as it is. You can scan it again whenever you're ready.")},confirmButton={TextButton(onClick={exit=false;vm.home()}) { Text("Exit") }},dismissButton={TextButton(onClick={exit=false}) { Text("Keep going") }})
 }
}}
