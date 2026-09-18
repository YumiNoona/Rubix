package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayCircle
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
        if (preferences.reduceMotion) {
            previewCube = CubeState.solved()
            moveIndex = homeSolution.size
            return@LaunchedEffect
        }
        delay(500)
        homeSolution.forEachIndexed { index, move ->
            moveIndex = index
            val turnDuration = preferences.animationMillis.toLong() * if (move.turns == 2) 2 else 1
            delay(turnDuration + 240L)
            previewCube = previewCube.apply(move)
        }
        moveIndex = homeSolution.size
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PageIntro("Ready to solve?", subtitle = "Scan a puzzle or enter its colors.")
        Spacer(Modifier.height(10.dp))
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f).heightIn(min = 210.dp, max = 340.dp),
            shape = RubixTokens.cardShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = .65f)),
        ) {
            CubeView(
                cube = previewCube,
                modifier = Modifier.fillMaxSize().padding(6.dp)
                    .semantics { contentDescription = "Scrambled cube solving itself" },
                move = homeSolution.getOrNull(moveIndex),
                replay = moveIndex,
                showInitials = false,
            )
        }
        Spacer(Modifier.height(14.dp))
        RubixPrimaryButton("Scan puzzle", onClick = vm::openScanPicker, icon = Icons.Rounded.PhotoCamera)
        Spacer(Modifier.height(10.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = { feedback(); vm.manual() },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RubixTokens.controlShape,
            ) {
                Icon(Icons.Rounded.Edit, null, Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("Manual")
            }
            TextButton(
                onClick = { feedback(); vm.demo() },
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RubixTokens.controlShape,
            ) {
                Icon(Icons.Rounded.PlayCircle, null, Modifier.size(19.dp))
                Spacer(Modifier.width(7.dp))
                Text("Demo")
            }
        }
    }
}
