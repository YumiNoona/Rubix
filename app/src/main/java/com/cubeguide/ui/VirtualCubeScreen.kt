package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.Face
import com.cubeguide.core.Move
import com.cubeguide.core.CubeState
import com.cubeguide.play.VirtualCube
import com.cubeguide.play.virtualScramble
import com.cubeguide.rendering.CubeView

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
    var inverse by rememberSaveable { mutableStateOf(false) }

    Column(Modifier.fillMaxSize()) {
        if (lessonTitle != null) {
            Surface(
                color = if (cube.solved) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth(),
            ) {
                Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = if (cube.solved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(10.dp))
                    Column { Text(lessonTitle, fontWeight = FontWeight.SemiBold); Text(if (cube.solved) "Practice complete" else "Try the idea from the lesson", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            Spacer(Modifier.height(10.dp))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (cube.solved) "Solved · drag to rotate" else "${cube.history.size} moves · drag to rotate",
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
            PuzzleSizeMenu(size, preferences::updatePuzzleSize)
        }
        if (size == 3) CubeView(CubeState(cube.stickers), Modifier.fillMaxWidth().weight(1f).heightIn(min = 250.dp))
        else VirtualCubeView(cube, Modifier.fillMaxWidth().weight(1f).heightIn(min = 250.dp))
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(14.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Face.entries.forEach { face ->
                        OutlinedButton(
                            onClick = { feedback(); cube = cube.apply(Move(face, if (inverse) 3 else 1)) },
                            enabled = true,
                            modifier = Modifier.weight(1f).aspectRatio(1f),
                            shape = CircleShape,
                            contentPadding = PaddingValues(0.dp),
                        ) { Text(face.name, fontWeight = FontWeight.Bold, maxLines = 1) }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Turn direction", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                    FilterChip(selected = !inverse, onClick = { inverse = false; feedback() }, label = { Text("CW") })
                    Spacer(Modifier.width(6.dp))
                    FilterChip(selected = inverse, onClick = { inverse = true; feedback() }, label = { Text("CCW") })
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompactAction("Scramble", false, true, Modifier.weight(1f)) {
                        feedback()
                        cube = virtualScramble(size).fold(VirtualCube.solved(size)) { state, move -> state.apply(move) }
                    }
                    CompactAction("Undo", true, cube.history.isNotEmpty(), Modifier.weight(1f)) {
                        feedback(); val last=cube.history.last();cube=cube.apply(last.inverse(),record=false).copy(history=cube.history.dropLast(1))
                    }
                    CompactAction("Reset", false, !cube.solved, Modifier.weight(1f)) {
                        feedback(); cube = cube.reset()
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun CompactAction(label: String, filled: Boolean, enabled: Boolean, modifier: Modifier, onClick: () -> Unit) {
    if (filled) {
        Button(onClick = onClick, enabled = enabled, modifier = modifier.height(46.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        OutlinedButton(onClick = onClick, enabled = enabled, modifier = modifier.height(46.dp), contentPadding = PaddingValues(horizontal = 4.dp)) {
            Text(label, maxLines = 1, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
internal fun PuzzleSizeMenu(size: Int, onSelect: (Int) -> Unit) {
    var open by remember { mutableStateOf(false) }
    Box {
        OutlinedButton(
            onClick = { open = true },
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        ) { Text("${size}×${size}", maxLines = 1) }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }) {
            (2..7).forEach { value ->
                DropdownMenuItem(text = { Text("${value}×${value} cube") }, onClick = { open = false; onSelect(value) })
            }
        }
    }
}
