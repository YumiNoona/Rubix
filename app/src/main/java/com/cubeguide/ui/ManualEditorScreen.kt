package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.cubeguide.rendering.CubeView

@Composable
internal fun ManualEditor(vm: CubeViewModel) {
    val preferences = LocalAppPreferences.current
    val feedback = rememberTouchFeedback()
    var faceIndex by rememberSaveable { mutableIntStateOf(0) }
    var brushIndex by rememberSaveable { mutableIntStateOf(CubeColor.WHITE.ordinal) }
    val face = Face.entries[faceIndex]
    val brush = CubeColor.entries[brushIndex]

    Column(Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Face ${faceIndex + 1} of 6", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Choose a color, then paint the face.", color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            TextButton(
                onClick = { feedback(); vm.undoEdit() },
                enabled = vm.canUndoEdit,
            ) { Text("Undo") }
        }

        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CubeView(
                vm.cube,
                Modifier.fillMaxWidth().height(190.dp).semantics { contentDescription = "Your editable 3D cube" },
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                TextButton(
                    onClick = { feedback(); faceIndex = (faceIndex + 5) % 6 },
                    modifier = Modifier.weight(1f),
                ) { Text("‹ Previous") }
                Surface(
                    shape = CircleShape,
                    color = Color(preferences.color(vm.cube.stickers[faceIndex * 9 + 4])),
                ) {
                    Text(
                        "${face.name} · ${vm.cube.stickers[faceIndex * 9 + 4].label}",
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        color = Color(preferences.ink(vm.cube.stickers[faceIndex * 9 + 4])),
                        fontWeight = FontWeight.Bold,
                    )
                }
                TextButton(
                    onClick = { feedback(); faceIndex = (faceIndex + 1) % 6 },
                    modifier = Modifier.weight(1f),
                ) { Text("Next ›") }
            }

            LargeFace(vm.cube, face, vm.lowConfidence) { index ->
                if (index % 9 != 4) {
                    feedback()
                    vm.edit(index, brush)
                }
            }
            Spacer(Modifier.height(14.dp))
            Text("Choose a color", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                CubeColor.entries.forEach { color ->
                    val count = vm.cube.stickers.count { it == color }
                    Surface(
                        onClick = { brushIndex = color.ordinal; feedback() },
                        shape = CircleShape,
                        color = Color(preferences.color(color)),
                        border = if (brush == color) BorderStroke(3.dp, MaterialTheme.colorScheme.onSurface) else null,
                        modifier = Modifier.size(48.dp).semantics {
                            selected = brush == color
                            contentDescription = "${color.label} brush, $count stickers"
                        },
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("$count", color = Color(preferences.ink(color)), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                "Paint stickers with ${brush.label.lowercase()}. Centers stay fixed.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))
        }

        Button(
            onClick = { feedback(); vm.solve() },
            enabled = !vm.busy,
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
            shape = RoundedCornerShape(18.dp),
        ) { Text(if (vm.busy) "Checking cube…" else "Solve this cube") }
        Spacer(Modifier.height(12.dp))
    }

    vm.solveError?.let { error ->
        AlertDialog(
            onDismissRequest = vm::dismissSolveError,
            title = { Text("This cube needs a check") },
            text = { Text(error) },
            confirmButton = { TextButton(onClick = vm::dismissSolveError) { Text("Keep editing") } },
        )
    }
}
