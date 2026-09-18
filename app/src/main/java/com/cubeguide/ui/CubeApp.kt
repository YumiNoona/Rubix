package com.cubeguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cubeguide.core.PuzzleRegistry

internal val AccentBlue = RubixBlueStrong
private val rootScreens = setOf(Screen.HOME, Screen.PRACTICE, Screen.LEARN)

@Composable
fun CubeApp(vm: CubeViewModel = viewModel()) {
    val context = LocalContext.current
    val preferences = remember { AppPreferences(context) }
    val view = androidx.compose.ui.platform.LocalView.current
    DisposableEffect(vm.screen, preferences.keepAwake) {
        val old = view.keepScreenOn
        view.keepScreenOn = preferences.keepAwake && vm.screen in setOf(
            Screen.SCAN, Screen.ANALYZING, Screen.SETUP, Screen.GUIDE, Screen.CORRECT, Screen.TIMER,
        )
        onDispose { view.keepScreenOn = old }
    }

    CompositionLocalProvider(LocalAppPreferences provides preferences) {
        val useDark=when(preferences.themeMode) { AppThemeMode.SYSTEM->isSystemInDarkTheme();AppThemeMode.DARK->true;AppThemeMode.LIGHT->false }
        SideEffect {
            val activity=view.context as? android.app.Activity
            activity?.let { androidx.core.view.WindowCompat.getInsetsController(it.window,view).apply { isAppearanceLightStatusBars=!useDark;isAppearanceLightNavigationBars=!useDark } }
        }
        MaterialTheme(colorScheme = if(useDark) rubixDarkScheme() else rubixLightScheme(), typography = rubixTypography()) {
            var settings by remember { mutableStateOf(false) }
            var exit by remember { mutableStateOf(false) }
            val holder = rememberSaveableStateHolder()
            val root = vm.screen in rootScreens
            val hideAppBar = vm.screen in setOf(Screen.GUIDE, Screen.PUZZLE_SOLVE)

            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Box(Modifier.fillMaxSize().safeDrawingPadding()) {
                    AnimatedContent(
                        targetState = vm.screen,
                        modifier = Modifier.fillMaxSize(),
                        transitionSpec = {
                            if (preferences.reduceMotion) fadeIn(tween(0)) togetherWith fadeOut(tween(0))
                            else calmScreenTransform()
                        },
                        label = "screen",
                    ) { screen ->
                        val topPadding = if (screen in setOf(Screen.GUIDE, Screen.PUZZLE_SOLVE)) 0.dp else 64.dp
                        val bottomPadding = if (screen in rootScreens) 102.dp else 8.dp
                        Box(
                            Modifier.fillMaxSize().padding(
                                start = RubixTokens.screenPadding,
                                end = RubixTokens.screenPadding,
                                top = topPadding,
                                bottom = bottomPadding,
                            ),
                        ) {
                            holder.SaveableStateProvider(screen.name) {
                                when (screen) {
                                    Screen.HOME -> Home(vm)
                                    Screen.PRACTICE -> PracticeScreen(vm)
                                    Screen.VIRTUAL -> VirtualCubeScreen(vm)
                                    Screen.TIMER -> TimerScreen()
                                    Screen.LEARN -> LearnScreen(vm)
                                    Screen.SCAN_PICKER -> ScanPuzzlePickerScreen(onScan = vm::scanPuzzle)
                                    Screen.SCAN_PREPARE -> ThreeByThreeScanPreparation(vm::startThreeByThreeScan)
                                    Screen.PUZZLE_SOLVE -> MultiPuzzleFlow(vm.puzzleSession(), vm::closePuzzleSolve)
                                    Screen.SCAN -> Scan(vm)
                                    Screen.REVIEW, Screen.CORRECT -> Review(vm)
                                    Screen.EDIT -> ColorEditorScreen(vm)
                                    Screen.ANALYZING -> AnalyzingScreen(vm)
                                    Screen.SETUP -> HoldingSetup(vm)
                                    Screen.GUIDE -> Guide(vm) { exit = true }
                                    Screen.DONE -> Done(vm)
                                }
                            }
                        }
                    }

                    if (!hideAppBar) {
                        RubixTopBar(
                            vm = vm,
                            root = root,
                            onBack = { handleBack(vm) { exit = true } },
                            onSettings = { settings = true },
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                    if (root) {
                        RubixDock(
                            selected = vm.screen,
                            onSelect = vm::open,
                            modifier = Modifier.align(Alignment.BottomCenter),
                        )
                    }
                }
            }

            BackHandler(vm.screen != Screen.HOME) { handleBack(vm) { exit = true } }
            if (settings) DisplaySettings { settings = false }
            if (exit) {
                AlertDialog(
                    onDismissRequest = { exit = false },
                    title = { Text("Leave this solve?") },
                    text = { Text("Your cube is saved. You can safely return to Solve.") },
                    confirmButton = { TextButton(onClick = { exit = false; vm.home() }) { Text("Leave") } },
                    dismissButton = { TextButton(onClick = { exit = false }) { Text("Stay") } },
                )
            }
        }
    }
}

private fun calmScreenTransform(): ContentTransform =
    (fadeIn(tween(180)) + scaleIn(tween(220), initialScale = .992f)) togetherWith fadeOut(tween(110))

private fun handleBack(vm: CubeViewModel, requestExit: () -> Unit) {
    when {
        vm.screen in rootScreens -> vm.open(Screen.HOME)
        vm.screen == Screen.VIRTUAL && vm.virtualMode != null -> vm.closeVirtualMode()
        vm.screen in setOf(Screen.VIRTUAL, Screen.TIMER) -> vm.open(Screen.PRACTICE)
        vm.screen == Screen.SCAN_PICKER -> vm.home()
        vm.screen == Screen.SCAN_PREPARE -> vm.openScanPicker()
        vm.screen == Screen.PUZZLE_SOLVE -> vm.handlePuzzleBack()
        vm.screen == Screen.SCAN -> vm.leaveScan()
        vm.screen == Screen.EDIT -> vm.cancelEdit()
        vm.screen == Screen.CORRECT && !vm.busy -> vm.cancelCorrection()
        vm.screen == Screen.REVIEW -> if (vm.manualEntry) vm.home() else vm.scan(vm.pose.face)
        vm.screen in setOf(Screen.ANALYZING, Screen.SETUP) -> vm.returnToReview()
        vm.screen == Screen.DONE -> vm.home()
        else -> requestExit()
    }
}

@Composable
private fun RubixTopBar(
    vm: CubeViewModel,
    root: Boolean,
    onBack: () -> Unit,
    onSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val feedback = rememberTouchFeedback()
    Row(
        modifier.fillMaxWidth().height(64.dp).background(MaterialTheme.colorScheme.background.copy(alpha = .96f))
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (!root) {
            IconButton(onClick = { feedback(); onBack() }) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, "Back")
            }
        } else {
            Icon(Icons.Rounded.ViewInAr,null,Modifier.padding(start=8.dp).size(25.dp),tint=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
        }
        Text(
            screenTitle(vm),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
        )
        if (root) {
            IconButton(onClick = { feedback(); onSettings() }) {
                Icon(Icons.Rounded.Tune, "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun screenTitle(vm: CubeViewModel) = when (vm.screen) {
    Screen.HOME -> "rubix."
    Screen.PRACTICE -> "Practice"
    Screen.LEARN -> "Learn"
    Screen.VIRTUAL -> vm.virtualMode?.title ?: "Virtual cube"
    Screen.TIMER -> "Timer"
    Screen.SCAN_PICKER -> "Choose puzzle"
    Screen.SCAN_PREPARE -> "Get ready"
    Screen.PUZZLE_SOLVE -> PuzzleRegistry.get(vm.activePuzzle).name
    Screen.SCAN -> "Scan"
    Screen.REVIEW -> if (vm.manualEntry) "Enter colors" else "Review"
    Screen.EDIT -> "Edit colors"
    Screen.CORRECT -> "Correct colors"
    Screen.ANALYZING -> "Checking cube"
    Screen.SETUP -> "Hold your cube"
    Screen.GUIDE -> "Solve"
    Screen.DONE -> "Complete"
}

@Composable
private fun RubixDock(selected: Screen, onSelect: (Screen) -> Unit, modifier: Modifier = Modifier) {
    val feedback = rememberTouchFeedback()
    Box(modifier.padding(horizontal = 22.dp, vertical = 4.dp).fillMaxWidth().height(92.dp)) {
        Surface(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(72.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .75f)),
        ) {
            Row(Modifier.fillMaxSize().padding(horizontal = 10.dp),verticalAlignment = Alignment.CenterVertically) {
                DockItem("Practice",Icons.Rounded.SportsEsports,selected==Screen.PRACTICE,Modifier.weight(1f)) { feedback();onSelect(Screen.PRACTICE) }
                Spacer(Modifier.weight(1f))
                DockItem("Learn",Icons.Rounded.School,selected==Screen.LEARN,Modifier.weight(1f)) { feedback();onSelect(Screen.LEARN) }
            }
        }

        Surface(
            onClick = { feedback(); onSelect(Screen.HOME) },
            modifier = Modifier.align(Alignment.TopCenter).size(68.dp).shadow(14.dp,CircleShape).semantics {
                this.selected = selected == Screen.HOME
                role = Role.Tab
                contentDescription = "Solve"
            },
            shape = CircleShape,
            color = if (selected == Screen.HOME) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer,
            border = androidx.compose.foundation.BorderStroke(4.dp,MaterialTheme.colorScheme.background),
        ) {
            Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
                Icon(Icons.Rounded.ViewInAr,"Solve",Modifier.size(32.dp),tint=if(selected==Screen.HOME) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
    }
}

@Composable
private fun DockItem(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(60.dp).semantics { this.selected = selected; role = Role.Tab },
        color = Color.Transparent,
        shape = RoundedCornerShape(18.dp),
    ) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Icon(
                icon,
                label,
                Modifier.size(24.dp),
                tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
