package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.AutoFixHigh
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ViewInAr
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun ManualEditor(vm: CubeViewModel) {
    val preferences=LocalAppPreferences.current
    val feedback=rememberTouchFeedback()
    var faceIndex by rememberSaveable { mutableIntStateOf(Face.F.ordinal) }
    var brushIndex by rememberSaveable { mutableIntStateOf(CubeColor.WHITE.ordinal) }
    var preview by rememberSaveable { mutableStateOf(false) }
    val face=Face.entries[faceIndex]
    val brush=CubeColor.entries[brushIndex]

    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Paint the stickers",style=MaterialTheme.typography.titleLarge)
                Text("Centers stay fixed",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FilledTonalIconButton(onClick={feedback();preview=true}) {
                Icon(Icons.Rounded.ViewInAr,"Preview cube")
            }
            Spacer(Modifier.width(6.dp))
            FilledTonalIconButton(onClick={feedback();vm.undoEdit()},enabled=vm.canUndoEdit) { Icon(Icons.AutoMirrored.Rounded.Undo,"Undo color change") }
        }

        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                IconButton(onClick={feedback();faceIndex=(faceIndex+5)%6}) { Icon(Icons.Rounded.ChevronLeft,"Previous face") }
                Surface(Modifier.weight(1f),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
                    Row(Modifier.padding(horizontal=14.dp,vertical=10.dp),horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                        val center=vm.cube.stickers[faceIndex*9+4]
                        Box(Modifier.size(18.dp),contentAlignment=Alignment.Center) { Surface(Modifier.fillMaxSize(),shape=CircleShape,color=Color(preferences.color(center))) {} }
                        Spacer(Modifier.width(9.dp));Text("${face.label.replaceFirstChar { it.uppercase() }} face",fontWeight=FontWeight.SemiBold)
                    }
                }
                IconButton(onClick={feedback();faceIndex=(faceIndex+1)%6}) { Icon(Icons.Rounded.ChevronRight,"Next face") }
            }
            Spacer(Modifier.height(14.dp))
            LargeFace(vm.cube,face,vm.lowConfidence) { index ->
                if(index%9!=4) { feedback();vm.edit(index,brush) }
            }
            Spacer(Modifier.height(18.dp))
            SectionLabel("Sticker color",Modifier.fillMaxWidth())
            Spacer(Modifier.height(10.dp))
            CubeColor.entries.chunked(3).forEach { colors ->
                Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                    colors.forEach { color ->
                        val count=vm.cube.stickers.count { it==color }
                        Surface(
                            onClick={brushIndex=color.ordinal;feedback()},
                            modifier=Modifier.weight(1f).height(54.dp).semantics { selected=brush==color;contentDescription="${color.label} brush, $count stickers" },
                            shape=RubixTokens.controlShape,
                            color=if(brush==color) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                            border=BorderStroke(if(brush==color) 2.dp else 1.dp,if(brush==color) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
                        ) {
                            Row(Modifier.fillMaxSize().padding(horizontal=10.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.Center) {
                                Surface(Modifier.size(22.dp),shape=CircleShape,color=Color(preferences.color(color))) {}
                                Spacer(Modifier.width(7.dp));Text(color.initial,fontWeight=FontWeight.Bold)
                                Spacer(Modifier.width(4.dp));Text(count.toString(),style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }
            Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
                Text("Show color initials",modifier=Modifier.weight(1f))
                Switch(preferences.initials,{preferences.updateInitials(it);feedback()})
            }
            Spacer(Modifier.height(12.dp))
        }

        RubixPrimaryButton(if(vm.busy) "Checking cube…" else "Solve cube",vm::solve,icon=Icons.Rounded.AutoFixHigh,enabled=!vm.busy)
        Spacer(Modifier.height(8.dp))
    }

    vm.solveError?.let { error ->
        AlertDialog(
            onDismissRequest=vm::dismissSolveError,
            title={Text("This cube needs a check")},
            text={Text(error)},
            confirmButton={TextButton(onClick=vm::dismissSolveError) { Text("Keep editing") }},
        )
    }
    if(preview) ModalBottomSheet(onDismissRequest={preview=false},shape=RubixTokens.modalShape) {
        Column(Modifier.fillMaxWidth().padding(horizontal=20.dp).navigationBarsPadding(),horizontalAlignment=Alignment.CenterHorizontally) {
            Text("Cube preview",style=MaterialTheme.typography.headlineSmall)
            CubeView(vm.cube,Modifier.fillMaxWidth().height(340.dp))
            Spacer(Modifier.height(18.dp))
        }
    }
}
