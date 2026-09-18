package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import com.cubeguide.rendering.CubeView

@Composable internal fun Done(vm: CubeViewModel) {
 CompletionFeedback(vm.moves)
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
  Spacer(Modifier.height(20.dp))
  Icon(Icons.Rounded.CheckCircle,"Solved",Modifier.size(68.dp),tint=MaterialTheme.colorScheme.tertiary)
  Text(if(vm.moves.isEmpty()) "Already solved" else "Cube solved",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.SemiBold)
  Text(if(vm.isReplay) "Solution replay complete." else "Every face is back in order.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  CubeView(vm.cube,modifier=Modifier.fillMaxWidth().height(280.dp))
  Surface(shape=RubixTokens.cardShape,color=MaterialTheme.colorScheme.surfaceContainer,border=BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant.copy(alpha=.65f))) {
   Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=16.dp),horizontalArrangement=Arrangement.SpaceBetween) {
    Column { Text("MOVES",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(vm.moves.size.toString(),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold) }
    Column(horizontalAlignment=Alignment.End) { Text("STATUS",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Verified",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold) }
   }
  }
  Spacer(Modifier.height(22.dp)); RubixPrimaryButton("Solve another",vm::openScanPicker,icon=Icons.Rounded.Refresh)
  TextButton(onClick={vm.viewSolution()},enabled=vm.moves.isNotEmpty()) { Text("View solution again") }
 }
}
