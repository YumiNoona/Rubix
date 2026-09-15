package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.automirrored.rounded.Redo
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.CubeColor
import com.cubeguide.core.Face

@Composable
internal fun ColorEditorScreen(vm: CubeViewModel) {
    val preferences = LocalAppPreferences.current
    val feedback = rememberTouchFeedback()
    val face = Face.entries[vm.editorFace]
    var brushIndex by rememberSaveable { mutableIntStateOf(vm.cube.stickers[vm.editorFace * 9 + 4].ordinal) }
    val brush = CubeColor.entries[brushIndex]

    Column(Modifier.fillMaxSize()) {
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { feedback(); vm.selectEditorFace(Face.entries[(vm.editorFace + 5) % 6]) }) {
                    Icon(Icons.Rounded.ChevronLeft, "Previous face")
                }
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(preferences.color(vm.cube.stickers[vm.editorFace * 9 + 4]))) {
                        Spacer(Modifier.size(13.dp))
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("${face.label.replaceFirstChar { it.uppercase() }} · ${vm.cube.stickers[vm.editorFace * 9 + 4].label}", fontWeight = FontWeight.SemiBold)
                }
                IconButton(onClick = { feedback(); vm.selectEditorFace(Face.entries[(vm.editorFace + 1) % 6]) }) {
                    Icon(Icons.Rounded.ChevronRight, "Next face")
                }
            }
            Spacer(Modifier.height(14.dp))
            LargeFace(vm.cube, face, vm.lowConfidence) { index ->
                if (index % 9 != 4) {
                    feedback()
                    vm.edit(index, brush)
                }
            }
            Spacer(Modifier.height(22.dp))
            Text("Sticker color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CubeColor.entries.forEach { color ->
                    Surface(
                        onClick = { feedback(); brushIndex = color.ordinal },
                        shape = CircleShape,
                        color = Color(preferences.color(color)),
                        border = BorderStroke(if (brush == color) 3.dp else 1.dp, if (brush == color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.size(48.dp).semantics {
                            selected = brush == color
                            contentDescription = color.label
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (preferences.initials) Text(color.initial, color = Color(preferences.ink(color)), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Show color initials", modifier = Modifier.weight(1f))
                Switch(checked = preferences.initials, onCheckedChange = { feedback(); preferences.updateInitials(it) })
            }
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                FilledTonalIconButton(onClick = { feedback(); vm.undoEdit() }, enabled = vm.canUndoEdit) {
                    Icon(Icons.AutoMirrored.Rounded.Undo, "Undo color change")
                }
                FilledTonalIconButton(onClick = { feedback(); vm.redoEdit() }, enabled = vm.canRedoEdit) {
                    Icon(Icons.AutoMirrored.Rounded.Redo, "Redo color change")
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { feedback(); vm.cancelEdit() }, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Cancel", maxLines = 1)
            }
            Button(onClick = { feedback(); vm.finishEdit() }, modifier = Modifier.weight(1f).height(52.dp)) {
                Text("Done", maxLines = 1)
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}
