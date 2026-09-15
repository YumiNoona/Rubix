package com.cubeguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

internal val Mint=Color(0xFF62E7C4)
private val mainScreens=setOf(Screen.HOME,Screen.VIRTUAL,Screen.TIMER,Screen.LEARN,Screen.PUZZLES)
@Composable fun CubeApp(vm: CubeViewModel=viewModel()) {
 val context=LocalContext.current;val preferences=remember { AppPreferences(context) };val view=androidx.compose.ui.platform.LocalView.current
 DisposableEffect(vm.screen,preferences.keepAwake) { val old=view.keepScreenOn;view.keepScreenOn=preferences.keepAwake && vm.screen in setOf(Screen.SCAN,Screen.SETUP,Screen.GUIDE,Screen.CORRECT,Screen.TIMER);onDispose { view.keepScreenOn=old } }
 CompositionLocalProvider(LocalAppPreferences provides preferences) {
  val scheme=darkColorScheme(primary=Mint,onPrimary=Color(0xFF002119),primaryContainer=Color(0xFF173C34),onPrimaryContainer=Color(0xFFB4F6E3),secondary=Color(0xFFFFB59F),background=Color(0xFF071216),surface=Color(0xFF071216),surfaceContainer=Color(0xFF112027),surfaceContainerHighest=Color(0xFF1A2B32),outlineVariant=Color(0xFF294047),onSurface=Color(0xFFE7F0F2),onSurfaceVariant=Color(0xFFA9BBC0))
  var settings by remember { mutableStateOf(false) };var exit by remember { mutableStateOf(false) };val holder=rememberSaveableStateHolder()
  MaterialTheme(colorScheme=scheme,typography=Typography(headlineLarge=MaterialTheme.typography.headlineLarge.copy(fontWeight=FontWeight.Bold),headlineMedium=MaterialTheme.typography.headlineMedium.copy(fontWeight=FontWeight.Bold))) {
   Surface(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().safeDrawingPadding()) {
    AppTopBar(vm.screen,onBack={if(vm.screen in mainScreens) vm.open(Screen.HOME) else exit=true},onSettings={settings=true})
    AnimatedContent(vm.screen,modifier=Modifier.weight(1f).padding(horizontal=18.dp),transitionSpec={fadeIn(tween(180))+slideInHorizontally { it/10 } togetherWith fadeOut(tween(120))},label="screen") { screen ->
     holder.SaveableStateProvider(screen.name) { when(screen) {
      Screen.HOME -> Home(vm)
      Screen.VIRTUAL -> VirtualCubeScreen()
      Screen.TIMER -> TimerScreen()
      Screen.LEARN -> LearnScreen { vm.open(Screen.VIRTUAL) }
      Screen.PUZZLES -> PuzzleCatalogScreen { vm.open(Screen.VIRTUAL) }
      Screen.SCAN -> Scan(vm);Screen.REVIEW,Screen.CORRECT -> Review(vm);Screen.SETUP -> HoldingSetup(vm);Screen.GUIDE -> Guide(vm);Screen.DONE -> Done(vm)
     } }
    }
    if(vm.screen in mainScreens && vm.screen!=Screen.PUZZLES) MainNavigation(vm.screen) { vm.open(it) }
   } }
   BackHandler(vm.screen!=Screen.HOME) { if(vm.screen in mainScreens) vm.open(Screen.HOME) else if(vm.screen==Screen.CORRECT && !vm.busy) vm.cancelCorrection() else exit=true }
   if(settings) DisplaySettings { settings=false }
   if(exit) AlertDialog(onDismissRequest={exit=false},title={Text("Leave this solve?")},text={Text("Your cube will stay as it is. This guide will close.")},confirmButton={TextButton(onClick={exit=false;vm.home()}) { Text("Leave") }},dismissButton={TextButton(onClick={exit=false}) { Text("Keep solving") }})
  }
 }
}
@Composable private fun AppTopBar(screen:Screen,onBack:()->Unit,onSettings:()->Unit) {
 val feedback=rememberTouchFeedback()
 Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
  if(screen!=Screen.HOME) TextButton(onClick={feedback();onBack()},contentPadding=PaddingValues(horizontal=6.dp)) { Text("Back") }
  Text(when(screen) { Screen.HOME->"rubix";Screen.VIRTUAL->"Play";Screen.TIMER->"Timer";Screen.LEARN->"Learn";Screen.PUZZLES->"Puzzles";Screen.SCAN->"Capture";Screen.REVIEW->"Review";Screen.CORRECT->"Adjust";Screen.SETUP->"Get ready";Screen.GUIDE->"Your solve";Screen.DONE->"Complete" },style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,modifier=Modifier.weight(1f).padding(start=if(screen==Screen.HOME)6.dp else 4.dp))
  TextButton(onClick={feedback();onSettings()},contentPadding=PaddingValues(horizontal=8.dp)) { Text("Settings") }
 }
}
@Composable private fun MainNavigation(selected:Screen,onSelect:(Screen)->Unit) {
 val feedback=rememberTouchFeedback()
 NavigationBar(containerColor=MaterialTheme.colorScheme.surfaceContainer,tonalElevation=0.dp) { listOf(Screen.HOME to "Home",Screen.VIRTUAL to "Play",Screen.TIMER to "Timer",Screen.LEARN to "Learn").forEach { (screen,label) -> NavigationBarItem(selected=selected==screen,onClick={feedback();onSelect(screen)},icon={ FeatureGlyph(when(screen) { Screen.HOME->FeatureIcon.HOME;Screen.VIRTUAL->FeatureIcon.CUBE;Screen.TIMER->FeatureIcon.TIMER;else->FeatureIcon.LEARN },Modifier.size(23.dp)) },label={Text(label)}) } }
}
