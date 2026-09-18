package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.cubeguide.core.CubeState
import com.cubeguide.core.Move
import com.cubeguide.rendering.CubeView
import kotlinx.coroutines.delay

private val homeScrambles = listOf(
    "R U2 F' L D R2 U' B",
    "F R U R' U' F' D2 L",
    "L2 U B' R D2 F U' R'",
    "B U2 L' D R2 F' U L",
    "R2 F U' B L2 D R' U2",
    "U R U' L' U R' U' L",
    "F2 D R2 U' L B' D2 R",
    "L U2 L' F R' F' R D",
    "B' R2 D F' U2 L D' R",
    "D2 F R' U L2 B U' F'",
).map(Move::parse)

@Composable
internal fun Home(vm: CubeViewModel) {
    val feedback = rememberTouchFeedback()
    val preferences = LocalAppPreferences.current
    var previewCube by remember { mutableStateOf(CubeState.solved()) }
    var activeMove by remember { mutableStateOf<Move?>(null) }
    var replay by remember { mutableIntStateOf(0) }
    LaunchedEffect(preferences.reduceMotion,preferences.animationMillis) {
        previewCube=CubeState.solved();activeMove=null
        if (preferences.reduceMotion) {
            return@LaunchedEffect
        }
        var previous=-1
        delay(650)
        while(true) {
            val index=homeScrambles.indices.filter { it!=previous }.random();previous=index
            val scramble=homeScrambles[index]
            val sequence=scramble+scramble.asReversed().map(Move::inverse)
            sequence.forEach { move ->
                activeMove=move;replay++
                val turnDuration=preferences.animationMillis.toLong()*(if(move.turns==2) 2 else 1)+(if(move.turns==2) 180 else 0)
                delay(turnDuration+20L)
                previewCube=previewCube.apply(move)
                activeMove=null
                delay(25)
            }
            delay(5_000)
        }
    }
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PageIntro("Ready to solve?", subtitle = "Scan a puzzle or enter its colors.")
        Spacer(Modifier.height(10.dp))
        CubeView(cube=previewCube,modifier=Modifier.fillMaxWidth().weight(1f).heightIn(min=210.dp,max=350.dp).semantics { contentDescription="Cube scrambling and solving itself" },move=activeMove,replay=replay,showInitials=false)
        Spacer(Modifier.height(14.dp))
        RubixPrimaryButton("Scan puzzle", onClick = vm::openScanPicker, icon = Icons.Rounded.PhotoCamera)
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick={feedback();vm.manual()},modifier=Modifier.fillMaxWidth().height(54.dp),shape=RubixTokens.controlShape) {
            Icon(Icons.Rounded.Edit,null,Modifier.size(20.dp));Spacer(Modifier.width(9.dp));Text("Enter colors manually")
        }
    }
}
