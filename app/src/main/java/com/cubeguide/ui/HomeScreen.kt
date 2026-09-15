package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.core.CubeState
import com.cubeguide.core.Move
import com.cubeguide.rendering.CubeView
import kotlinx.coroutines.delay

private val homeScramble = Move.parse("R U2 F' L D R2 U' B")
private val homeSolution = homeScramble.asReversed().map(Move::inverse)

@Composable
internal fun Home(vm: CubeViewModel) {
    val feedback = rememberTouchFeedback()
    val preferences = LocalAppPreferences.current
    val scrambled = remember { CubeState.solved().apply(homeScramble) }
    var previewCube by remember { mutableStateOf(scrambled) }
    var moveIndex by remember { mutableIntStateOf(-1) }
    LaunchedEffect(Unit) {
        delay(500)
        homeSolution.forEachIndexed { index, move ->
            moveIndex = index
            val turnDuration = preferences.animationMillis.toLong() * if (move.turns == 2) 2 else 1
            delay(turnDuration + 240L)
            previewCube = previewCube.apply(move)
        }
        moveIndex = homeSolution.size
    }
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(8.dp))
        Text("Scan. Solve. Learn.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "Your pocket cube companion",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        CubeView(
            cube = previewCube,
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 190.dp, max = 310.dp)
                .semantics { contentDescription = "Scrambled Rubix cube solving itself" },
            move = homeSolution.getOrNull(moveIndex),
            replay = moveIndex,
            showInitials = false,
        )
        Button(
            onClick = { feedback(); vm.openScanPicker() },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(17.dp),
        ) {
            FeatureGlyph(FeatureIcon.SCAN, Modifier.size(23.dp))
            Spacer(Modifier.width(10.dp))
            Text("Scan my cube", maxLines = 1)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(
            onClick = { feedback(); vm.manual() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(17.dp),
        ) { Text("Enter colors manually", maxLines = 1) }
        TextButton(onClick = { feedback(); vm.demo() }) { Text("Try a guided demo", maxLines = 1) }
        Spacer(Modifier.height(8.dp))
    }
}
