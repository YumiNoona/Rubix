package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.Face
import com.cubeguide.core.Move
import com.cubeguide.play.VirtualCube
import com.cubeguide.play.VirtualMove
import com.cubeguide.play.virtualScramble

private enum class PlaygroundMode(val label: String) {
    FREE("Free play"),
    CHALLENGE("Challenge"),
    ASSIST("Solve"),
}

private data class PendingTurn(val move: VirtualMove, val result: VirtualCube)

@Composable
internal fun VirtualCubeScreen(vm: CubeViewModel) {
    val preferences = LocalAppPreferences.current
    val feedback = rememberTouchFeedback()
    val lessonTitle = vm.virtualLessonTitle
    val lessonScramble = vm.virtualLessonScramble
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
    var mode by rememberSaveable { mutableStateOf(if (lessonTitle == null) PlaygroundMode.FREE else PlaygroundMode.ASSIST) }
    var selectedFace by rememberSaveable { mutableStateOf(Face.R) }
    var layerDepth by rememberSaveable(size) { mutableIntStateOf(0) }
    var wideTurn by rememberSaveable(size) { mutableStateOf(false) }
    var pending by remember { mutableStateOf<PendingTurn?>(null) }
    var replay by remember { mutableIntStateOf(0) }
    var redo by remember(size) { mutableStateOf<List<VirtualMove>>(emptyList()) }
    var hintVisible by rememberSaveable { mutableStateOf(false) }

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
        } else {
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                PlaygroundMode.entries.forEachIndexed { index, item ->
                    SegmentedButton(
                        selected = mode == item,
                        onClick = { feedback(); mode = item; hintVisible = false },
                        shape = SegmentedButtonDefaults.itemShape(index, PlaygroundMode.entries.size),
                    ) { Text(item.label, maxLines = 1) }
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                when {
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

        VirtualCubeView(
            cube = cube,
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp),
            move = pending?.move,
            replay = replay,
            onAnimationProgress = { progress ->
                if (progress >= .999f) {
                    pending?.let {
                        cube = it.result
                        pending = null
                        feedback()
                    }
                }
            },
        )

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
                        onClick = { val move = selectedMove(3); begin(move, cube.apply(move)); redo = emptyList() },
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                    ) { Text("↶ 90°") }
                    OutlinedButton(
                        onClick = { val move = selectedMove(2); begin(move, cube.apply(move)); redo = emptyList() },
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                    ) { Text("180°") }
                    Button(
                        onClick = { val move = selectedMove(1); begin(move, cube.apply(move)); redo = emptyList() },
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                    ) { Text("90° ↷") }
                }

                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    TextButton(
                        enabled = pending == null,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            feedback()
                            cube = virtualScramble(size).fold(VirtualCube.solved(size)) { state, move -> state.apply(move) }
                            redo = emptyList()
                            hintVisible = mode != PlaygroundMode.FREE
                        },
                    ) { Text(if (mode == PlaygroundMode.CHALLENGE) "New challenge" else "Scramble") }
                    TextButton(
                        enabled = pending == null && cube.history.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val last = cube.history.last()
                            redo = redo + last
                            begin(last.inverse(), cube.apply(last.inverse(), record = false).withoutLastHistory())
                        },
                    ) { Text("Undo") }
                    TextButton(
                        enabled = pending == null && redo.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val move = redo.last()
                            redo = redo.dropLast(1)
                            begin(move, cube.apply(move))
                        },
                    ) { Text("Redo") }
                    if (mode != PlaygroundMode.FREE) {
                        TextButton(
                            enabled = pending == null && cube.history.isNotEmpty(),
                            modifier = Modifier.weight(1f),
                            onClick = { hintVisible = !hintVisible; feedback() },
                        ) { Text("Hint") }
                    } else {
                        TextButton(
                            enabled = pending == null && !cube.solved,
                            modifier = Modifier.weight(1f),
                            onClick = { feedback(); cube = cube.reset(); redo = emptyList() },
                        ) { Text("Reset") }
                    }
                }
            }
        }
        Spacer(Modifier.height(10.dp))
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
