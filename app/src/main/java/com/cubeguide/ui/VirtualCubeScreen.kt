package com.cubeguide.ui

import android.os.SystemClock
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.RotateLeft
import androidx.compose.material.icons.automirrored.rounded.RotateRight
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.Face
import com.cubeguide.core.Move
import com.cubeguide.core.CubeState
import com.cubeguide.play.VirtualCube
import com.cubeguide.play.VirtualMove
import com.cubeguide.play.virtualScramble
import com.cubeguide.rendering.CubeView
import kotlinx.coroutines.delay

private data class PendingTurn(val move: VirtualMove, val result: VirtualCube)

@Composable
internal fun VirtualCubeScreen(vm: CubeViewModel) {
    val preferences = LocalAppPreferences.current
    val feedback = rememberTouchFeedback()
    val lessonTitle = vm.virtualLessonTitle
    val lessonScramble = vm.virtualLessonScramble
    val mode = vm.virtualMode
    if (mode == null) {
        VirtualCubeHub(preferences.puzzleSize, preferences::updatePuzzleSize, vm::selectVirtualMode)
        return
    }
    val size = if (lessonTitle != null) 3 else preferences.puzzleSize
    val cubeSaver = remember(size) {
        Saver<VirtualCube, String>(
            save = { it.encode() },
            restore = { VirtualCube.decode(it)?.takeIf { cube -> cube.size == size } ?: VirtualCube.solved(size) },
        )
    }
    var cube by rememberSaveable(size, lessonTitle, lessonScramble, stateSaver = cubeSaver) {
        val start = Move.parse(lessonScramble).fold(VirtualCube.solved(size)) { state, move -> state.apply(move) }
        mutableStateOf(start)
    }
    var selectedFace by rememberSaveable { mutableStateOf(Face.R) }
    var layerDepth by rememberSaveable(size) { mutableIntStateOf(0) }
    var wideTurn by rememberSaveable(size) { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingTurn?>(null) }
    var replay by remember { mutableIntStateOf(0) }
    var redo by remember(size) { mutableStateOf<List<VirtualMove>>(emptyList()) }
    var hintVisible by rememberSaveable { mutableStateOf(false) }
    var challengeStart by rememberSaveable(size, mode) { mutableStateOf<Long?>(null) }
    var challengeElapsed by rememberSaveable(size, mode) { mutableLongStateOf(0L) }
    var challengeNow by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var challengeMoves by rememberSaveable(size, mode) { mutableIntStateOf(0) }
    var challengeHints by rememberSaveable(size, mode) { mutableIntStateOf(0) }

    LaunchedEffect(challengeStart) {
        while (challengeStart != null) {
            challengeNow = SystemClock.elapsedRealtime()
            delay(31)
        }
    }

    fun begin(move: VirtualMove, result: VirtualCube) {
        if (pending != null) return
        feedback()
        pending = PendingTurn(move, result)
        replay++
    }

    fun selectedMove(turns: Int): VirtualMove {
        val width = if (wideTurn && layerDepth == 0 && size >= 4) 2 else 1
        return VirtualMove(selectedFace, depth = layerDepth, width = width, turns = turns)
    }

    Column(Modifier.fillMaxSize()) {
        if (lessonTitle != null) {
            Surface(
                color = if (cube.solved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = if (cube.solved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(lessonTitle, fontWeight = FontWeight.SemiBold)
                        Text(if (cube.solved) "Practice complete" else "Try the idea from the lesson", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
                    mode == VirtualMode.CHALLENGE && challengeStart != null ->
                        "${formatTime(challengeNow - challengeStart!!)}  ·  $challengeMoves moves"
                    mode == VirtualMode.CHALLENGE && challengeElapsed > 0L ->
                        "${formatTime(challengeElapsed)}  ·  $challengeMoves moves  ·  $challengeHints hints"
                    cube.solved -> "Solved · drag to inspect"
                    pending != null -> "Turning ${pending!!.move.notation}"
                    else -> "${cube.history.size} moves · drag to inspect"
                },
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
            PuzzleSizeMenu(size, preferences::updatePuzzleSize)
        }

        val finishTurn: (Float) -> Unit = { progress ->
            if (progress >= .999f) {
                pending?.let {
                    cube = it.result
                    pending = null
                    if (mode == VirtualMode.CHALLENGE && it.result.solved && challengeStart != null) {
                        challengeElapsed = SystemClock.elapsedRealtime() - challengeStart!!
                        challengeStart = null
                    }
                    feedback()
                }
            }
        }
        val cubeModifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp)
        if (size == 3) {
            CubeView(
                cube = CubeState(cube.stickers),
                modifier = cubeModifier,
                move = pending?.move?.let { Move(it.face, it.turns) },
                replay = replay,
                showInitials = false,
                onAnimationProgress = finishTurn,
            )
        } else {
            VirtualCubeView(
                cube = cube,
                modifier = cubeModifier,
                move = pending?.move,
                replay = replay,
                onAnimationProgress = finishTurn,
            )
        }

        if (hintVisible && cube.history.isNotEmpty()) {
            val hint = cube.history.last().inverse()
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            ) {
                Column(Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.LightMode, null)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Try ${hint.notation}", fontWeight = FontWeight.SemiBold)
                            Text("Turn the ${hint.face.name} layer ${turnDescription(hint)}.", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(
                            enabled = pending == null,
                            onClick = { begin(hint, cube) },
                        ) { Icon(Icons.Rounded.PlayArrow, null); Text("Preview") }
                        Button(
                            enabled = pending == null,
                            onClick = {
                                begin(hint, cube.apply(hint, record = false).withoutLastHistory())
                                redo = emptyList()
                                if (mode == VirtualMode.CHALLENGE) challengeHints++
                            },
                        ) { Text("Apply hint") }
                    }
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(12.dp)) {
                FaceSelector(selectedFace, pending == null) { selectedFace = it; feedback() }

                if (size >= 4) {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Text("Layer", style = MaterialTheme.typography.labelLarge)
                        (0 until (size + 1) / 2).forEach { depth ->
                            FilterChip(
                                selected = layerDepth == depth,
                                onClick = { layerDepth = depth; if (depth > 0) wideTurn = false; feedback() },
                                enabled = pending == null,
                                label = { Text(if (depth == 0) "Outer" else "${depth + 1}") },
                            )
                        }
                        FilterChip(
                            selected = wideTurn,
                            onClick = { wideTurn = !wideTurn; layerDepth = 0; feedback() },
                            enabled = pending == null,
                            label = { Text("Wide") },
                        )
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { val move = selectedMove(3); begin(move, cube.apply(move)); redo = emptyList();if(mode==VirtualMode.CHALLENGE && challengeStart!=null) challengeMoves++ },
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                    ) { Icon(Icons.AutoMirrored.Rounded.RotateLeft, null, Modifier.size(20.dp));Spacer(Modifier.width(5.dp));Text("90°",maxLines=1) }
                    OutlinedButton(
                        onClick = { val move = selectedMove(2); begin(move, cube.apply(move)); redo = emptyList();if(mode==VirtualMode.CHALLENGE && challengeStart!=null) challengeMoves++ },
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                    ) { Text("180°",maxLines=1) }
                    Button(
                        onClick = { val move = selectedMove(1); begin(move, cube.apply(move)); redo = emptyList();if(mode==VirtualMode.CHALLENGE && challengeStart!=null) challengeMoves++ },
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                    ) { Text("90°",maxLines=1);Spacer(Modifier.width(5.dp));Icon(Icons.AutoMirrored.Rounded.RotateRight,null,Modifier.size(20.dp)) }
                }

                Spacer(Modifier.height(8.dp))
                Button(
                        enabled = pending == null,
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        shape = RoundedCornerShape(15.dp),
                        onClick = {
                            feedback()
                            cube = virtualScramble(size).fold(VirtualCube.solved(size)) { state, move -> state.apply(move) }
                            redo = emptyList()
                            hintVisible = mode != VirtualMode.FREE
                            if(mode==VirtualMode.CHALLENGE) {
                                challengeMoves=0;challengeHints=0;challengeElapsed=0L
                                challengeNow=SystemClock.elapsedRealtime();challengeStart=challengeNow
                            }
                        },
                    ) {
                        Icon(Icons.Rounded.Shuffle, null, Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (mode == VirtualMode.CHALLENGE) "New challenge" else "Scramble cube",
                            maxLines = 1,
                        )
                    }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        enabled = pending == null && cube.history.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        onClick = {
                            val last = cube.history.last()
                            redo = redo + last
                            begin(last.inverse(), cube.apply(last.inverse(), record = false).withoutLastHistory())
                        },
                    ) { Icon(Icons.AutoMirrored.Rounded.Undo,null,Modifier.size(19.dp));Spacer(Modifier.width(5.dp));Text("Undo", maxLines = 1) }
                    OutlinedButton(
                        enabled = pending == null && redo.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        onClick = {
                            val move = redo.last()
                            redo = redo.dropLast(1)
                            begin(move, cube.apply(move))
                        },
                    ) { Icon(Icons.AutoMirrored.Rounded.Redo,null,Modifier.size(19.dp));Spacer(Modifier.width(5.dp));Text("Redo", maxLines = 1) }
                    if (mode != VirtualMode.FREE) {
                        OutlinedButton(
                            enabled = pending == null && cube.history.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            onClick = { hintVisible = !hintVisible; feedback() },
                        ) { Icon(Icons.Rounded.Lightbulb,null,Modifier.size(19.dp));Spacer(Modifier.width(5.dp));Text("Hint", maxLines = 1) }
                    } else {
                        OutlinedButton(
                            enabled = pending == null && !cube.solved,
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            onClick = { feedback(); cube = cube.reset(); redo = emptyList() },
                        ) { Icon(Icons.Rounded.RestartAlt,null,Modifier.size(19.dp));Spacer(Modifier.width(5.dp));Text("Reset", maxLines = 1) }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
    }
}

@Composable
private fun VirtualCubeHub(size: Int, onSize: (Int) -> Unit, onMode: (VirtualMode) -> Unit) {
    val feedback = rememberTouchFeedback()
    val preview = remember {
        Move.parse("R U2 F' L D R2").fold(CubeState.solved()) { cube, move -> cube.apply(move) }
    }
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Choose how to play", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("No physical cube needed", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            PuzzleSizeMenu(size, onSize)
        }
        CubeView(preview, Modifier.fillMaxWidth().height(205.dp), showInitials = false)
        VirtualModeCard(
            Icons.Rounded.TouchApp,
            "Free play",
            "Turn any face, experiment and undo moves.",
        ) { feedback(); onMode(VirtualMode.FREE) }
        Spacer(Modifier.height(10.dp))
        VirtualModeCard(
            Icons.Rounded.Timer,
            "Challenge",
            "Race an automatic scramble with time and move tracking.",
        ) { feedback(); onMode(VirtualMode.CHALLENGE) }
        Spacer(Modifier.height(10.dp))
        VirtualModeCard(
            Icons.Rounded.Lightbulb,
            "Guided solve",
            "Preview one correct move at a time and solve along.",
        ) { feedback(); onMode(VirtualMode.GUIDED) }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun VirtualModeCard(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().heightIn(min = 86.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(15.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(52.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(27.dp))
                }
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
            }
            Icon(Icons.Rounded.ChevronRight, "Open", tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
private fun FaceSelector(selected: Face, enabled: Boolean, onSelect: (Face) -> Unit) {
    BoxWithConstraints(Modifier.fillMaxWidth()) {
        val rows = if (maxWidth < 430.dp) Face.entries.chunked(3) else listOf(Face.entries)
        Column {
            rows.forEach { faces ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    faces.forEach { face ->
                        FilterChip(
                            selected = selected == face,
                            onClick = { onSelect(face) },
                            enabled = enabled,
                            modifier = Modifier.weight(1f),
                            shape = CircleShape,
                            label = { Text(face.name, fontWeight = FontWeight.Bold) },
                        )
                    }
                }
            }
        }
    }
}

private fun turnDescription(move: VirtualMove) = when (move.turns) {
    2 -> "halfway around"
    3 -> "counter-clockwise"
    else -> "clockwise"
}

@Composable
internal fun PuzzleSizeMenu(size: Int, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { open = true },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) { Text("${size}×$size", maxLines = 1) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (2..7).forEach { value ->
                DropdownMenuItem(text = { Text("${value}×$value cube") }, onClick = { open = false; onSelect(value) })
            }
        }
    }
}
