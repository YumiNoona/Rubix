package com.cubeguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cubeguide.core.PuzzleRegistry

internal val AccentBlue=Color(0xFF238BFF)
private val rootScreens=setOf(Screen.HOME,Screen.PRACTICE,Screen.LEARN)
private fun Screen.depth()=when(this) {
 Screen.HOME,Screen.PRACTICE,Screen.LEARN -> 0
 Screen.VIRTUAL,Screen.TIMER,Screen.SCAN_PICKER -> 1
 Screen.SCAN_PREPARE,Screen.PUZZLE_SOLVE,Screen.SCAN -> 2
 Screen.REVIEW,Screen.EDIT,Screen.ANALYZING -> 3
 Screen.CORRECT,Screen.SETUP,Screen.GUIDE -> 4
 Screen.DONE -> 5
}
@Composable fun CubeApp(vm: CubeViewModel=viewModel()) {
 val context=LocalContext.current;val preferences=remember { AppPreferences(context) };val view=androidx.compose.ui.platform.LocalView.current
 DisposableEffect(vm.screen,preferences.keepAwake) { val old=view.keepScreenOn;view.keepScreenOn=preferences.keepAwake && vm.screen in setOf(Screen.SCAN,Screen.ANALYZING,Screen.SETUP,Screen.GUIDE,Screen.CORRECT,Screen.TIMER);onDispose { view.keepScreenOn=old } }
 CompositionLocalProvider(LocalAppPreferences provides preferences) {
  val scheme=darkColorScheme(primary=AccentBlue,onPrimary=Color.White,primaryContainer=Color(0xFF123458),onPrimaryContainer=Color(0xFFE4F0FF),secondary=Color(0xFFFFB21C),onSecondary=Color(0xFF241600),secondaryContainer=Color(0xFF4B3407),onSecondaryContainer=Color(0xFFFFE2A4),tertiary=Color(0xFF36D399),background=Color(0xFF050C16),surface=Color(0xFF050C16),surfaceContainer=Color(0xFF0D1926),surfaceContainerHighest=Color(0xFF142536),outline=Color(0xFF547083),outlineVariant=Color(0xFF293E4E),onSurface=Color(0xFFF2F6FA),onSurfaceVariant=Color(0xFFAAB9C6))
  var settings by remember { mutableStateOf(false) };var exit by remember { mutableStateOf(false) };val holder=rememberSaveableStateHolder()
  MaterialTheme(colorScheme=scheme,typography=Typography(headlineLarge=MaterialTheme.typography.headlineLarge.copy(fontWeight=FontWeight.Bold),headlineMedium=MaterialTheme.typography.headlineMedium.copy(fontWeight=FontWeight.Bold))) {
   Surface(Modifier.fillMaxSize()) { Column(Modifier.fillMaxSize().safeDrawingPadding()) {
    if(vm.screen !in setOf(Screen.GUIDE,Screen.PUZZLE_SOLVE)) AppTopBar(vm,onBack={when(vm.screen) { Screen.VIRTUAL -> if(vm.virtualMode!=null) vm.closeVirtualMode() else vm.open(Screen.PRACTICE);Screen.TIMER -> vm.open(Screen.PRACTICE);Screen.SCAN_PICKER -> vm.home();Screen.SCAN_PREPARE -> vm.openScanPicker();Screen.SCAN -> vm.leaveScan();Screen.EDIT -> vm.cancelEdit();Screen.CORRECT -> vm.cancelCorrection();Screen.REVIEW -> if(vm.manualEntry) vm.home() else vm.scan(vm.pose.face);Screen.ANALYZING,Screen.SETUP -> vm.returnToReview();Screen.DONE -> vm.home();else -> exit=true }},onSettings={settings=true})
    AnimatedContent(vm.screen,modifier=Modifier.weight(1f).padding(horizontal=18.dp),transitionSpec={
     if(initialState in rootScreens && targetState in rootScreens) {
      fadeIn(tween(180)) togetherWith fadeOut(tween(140))
     } else {
      val forward=targetState.depth()>=initialState.depth()
      fadeIn(tween(190))+slideInHorizontally(tween(210)) { if(forward) it/8 else -it/8 } togetherWith
       fadeOut(tween(140))+slideOutHorizontally(tween(170)) { if(forward) -it/12 else it/12 }
     }
    },label="screen") { screen ->
     holder.SaveableStateProvider(screen.name) { when(screen) {
      Screen.HOME -> Home(vm)
      Screen.PRACTICE -> PracticeScreen(vm)
      Screen.VIRTUAL -> VirtualCubeScreen(vm)
      Screen.TIMER -> TimerScreen()
      Screen.LEARN -> LearnScreen(vm)
      Screen.SCAN_PICKER -> ScanPuzzlePickerScreen(onScan=vm::scanPuzzle)
      Screen.SCAN_PREPARE -> ThreeByThreeScanPreparation(vm::startThreeByThreeScan)
      Screen.PUZZLE_SOLVE -> MultiPuzzleFlow(vm.puzzleSession(),vm::closePuzzleSolve)
      Screen.SCAN -> Scan(vm);Screen.REVIEW,Screen.CORRECT -> Review(vm);Screen.EDIT -> ColorEditorScreen(vm);Screen.ANALYZING -> AnalyzingScreen(vm);Screen.SETUP -> HoldingSetup(vm);Screen.GUIDE -> Guide(vm) { exit=true };Screen.DONE -> Done(vm)
     } }
    }
    if(vm.screen in rootScreens) MainNavigation(vm.screen) { vm.open(it) }
   } }
   BackHandler(vm.screen!=Screen.HOME) { when { vm.screen in rootScreens -> vm.open(Screen.HOME);vm.screen==Screen.VIRTUAL && vm.virtualMode!=null -> vm.closeVirtualMode();vm.screen in setOf(Screen.VIRTUAL,Screen.TIMER) -> vm.open(Screen.PRACTICE);vm.screen==Screen.SCAN_PICKER -> vm.home();vm.screen==Screen.SCAN_PREPARE -> vm.openScanPicker();vm.screen==Screen.PUZZLE_SOLVE -> vm.handlePuzzleBack();vm.screen==Screen.SCAN -> vm.leaveScan();vm.screen==Screen.EDIT -> vm.cancelEdit();vm.screen==Screen.CORRECT && !vm.busy -> vm.cancelCorrection();vm.screen==Screen.REVIEW -> if(vm.manualEntry) vm.home() else vm.scan(vm.pose.face);vm.screen in setOf(Screen.ANALYZING,Screen.SETUP) -> vm.returnToReview();vm.screen==Screen.DONE -> vm.home();else -> exit=true } }
   if(settings) DisplaySettings { settings=false }
   if(exit) AlertDialog(onDismissRequest={exit=false},title={Text("Leave this solve?")},text={Text("Your cube will stay as it is. This guide will close.")},confirmButton={TextButton(onClick={exit=false;vm.home()}) { Text("Leave") }},dismissButton={TextButton(onClick={exit=false}) { Text("Keep solving") }})
  }
 }
}
@Composable private fun AppTopBar(vm:CubeViewModel,onBack:()->Unit,onSettings:()->Unit) {
 val feedback=rememberTouchFeedback()
 val screen=vm.screen
 Row(Modifier.fillMaxWidth().height(58.dp).padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically) {
  if(screen !in rootScreens) IconButton(onClick={feedback();onBack()},modifier=Modifier.size(48.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back",Modifier.size(26.dp)) }
  Text(when(screen) { Screen.HOME->"rubix";Screen.PRACTICE->"Practice";Screen.LEARN->"Learn";Screen.VIRTUAL->vm.virtualMode?.title ?: "Virtual cube";Screen.TIMER->"Cube timer";Screen.SCAN_PICKER->"Choose puzzle";Screen.SCAN_PREPARE->"Before you scan";Screen.PUZZLE_SOLVE->PuzzleRegistry.get(vm.activePuzzle).name;Screen.SCAN->"Scan cube";Screen.REVIEW->if(vm.manualEntry) "Manual input" else "Review colors";Screen.EDIT->"Edit face";Screen.CORRECT->"Correct colors";Screen.ANALYZING->"Analyzing";Screen.SETUP->"Starting position";Screen.GUIDE->"Solve";Screen.DONE->"Complete" },style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold,maxLines=1,modifier=Modifier.weight(1f).padding(start=if(screen in rootScreens)6.dp else 4.dp))
  if(screen in rootScreens) IconButton(onClick={feedback();onSettings()},modifier=Modifier.size(48.dp)) { Icon(Icons.Rounded.Settings,"Settings",Modifier.size(25.dp),tint=MaterialTheme.colorScheme.primary) }
 }
}
@Composable private fun MainNavigation(selected:Screen,onSelect:(Screen)->Unit) {
 val feedback=rememberTouchFeedback()
 NavigationBar(containerColor=MaterialTheme.colorScheme.surfaceContainer,tonalElevation=0.dp) { listOf(Screen.HOME to "Home",Screen.PRACTICE to "Practice",Screen.LEARN to "Learn").forEach { (screen,label) -> NavigationBarItem(selected=selected==screen,onClick={feedback();onSelect(screen)},icon={ FeatureGlyph(when(screen) { Screen.HOME->FeatureIcon.HOME;Screen.PRACTICE->FeatureIcon.CUBE;else->FeatureIcon.LEARN },Modifier.size(23.dp)) },label={Text(label,maxLines=1)}) } }
}
