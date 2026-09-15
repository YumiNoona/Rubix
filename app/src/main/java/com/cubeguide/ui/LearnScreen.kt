package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class Lesson(val title: String, val subtitle: String, val steps: List<String>)

private val lessons = listOf(
    Lesson("Meet your cube", "Centers, edges and corners", listOf("Centers identify each face and never trade places.", "Edges have two colors. Corners have three.", "Keep one holding position while reading moves.")),
    Lesson("Turn notation", "Read U, R, F and prime moves", listOf("A letter names the face to turn.", "A plain letter turns clockwise when looking at that face.", "A prime mark turns counter-clockwise. A 2 means two quarter-turns.")),
    Lesson("White cross", "Build four matching edges", listOf("Put white on top.", "Bring each white edge to the top.", "Match its side color with the side center before placing it.")),
    Lesson("First layer", "Place the white corners", listOf("Find a white corner in the bottom layer.", "Rotate the bottom until it sits below its destination.", "Repeat R U R' U' until the corner is placed.")),
    Lesson("Middle layer", "Insert the four side edges", listOf("Find a bottom edge without yellow.", "Match its front color to the center.", "Use the left or right insertion and restore the top.")),
    Lesson("Yellow face", "Orient the last layer", listOf("Make the yellow cross.", "Orient yellow corners without moving solved pieces permanently.", "Check the whole cube after each algorithm.")),
    Lesson("Finish the cube", "Place the last pieces", listOf("Position the yellow corners.", "Cycle the final edges.", "Turn the top to align all six centers.")),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LearnScreen(onPractice: () -> Unit) {
    val preferences = LocalAppPreferences.current
    var selected by remember { mutableStateOf<Int?>(null) }

    Column(Modifier.fillMaxSize()) {
        Text("Seven short lessons. Move at your pace.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        LinearProgressIndicator(
            progress = { preferences.completedLessons.size / lessons.size.toFloat() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            itemsIndexed(lessons) { index, lesson ->
                val done = index in preferences.completedLessons
                Card(
                    onClick = { selected = index },
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = if (done) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHighest,
                        ) {
                            Box(Modifier.size(42.dp), contentAlignment = Alignment.Center) {
                                if (done) Text("✓", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                                else Text("${index + 1}", fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(lesson.title, fontWeight = FontWeight.SemiBold)
                            Text(lesson.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(88.dp)) }
        }
    }

    selected?.let { index ->
        val lesson = lessons[index]
        ModalBottomSheet(onDismissRequest = { selected = null }) {
            Column(Modifier.padding(horizontal = 24.dp).navigationBarsPadding()) {
                Text(lesson.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(14.dp))
                lesson.steps.forEachIndexed { step, instruction ->
                    Row(Modifier.padding(vertical = 9.dp)) {
                        Text("${step + 1}", modifier = Modifier.width(34.dp), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Text(instruction, modifier = Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { preferences.completeLesson(index); selected = null },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                ) { Text(if (index in preferences.completedLessons) "Completed" else "Mark complete") }
                TextButton(onClick = { selected = null; onPractice() }, modifier = Modifier.fillMaxWidth()) {
                    Text("Practice on virtual cube")
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}
