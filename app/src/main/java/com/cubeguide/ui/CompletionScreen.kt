package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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
  Spacer(Modifier.height(28.dp))
  val checkColor=MaterialTheme.colorScheme.primary
  Canvas(Modifier.size(64.dp).semantics { contentDescription="Solved" }) {
   drawLine(checkColor,Offset(size.width*0.18f,size.height*0.52f),Offset(size.width*0.42f,size.height*0.75f),6.dp.toPx())
   drawLine(checkColor,Offset(size.width*0.42f,size.height*0.75f),Offset(size.width*0.85f,size.height*0.25f),6.dp.toPx())
  }
  Text(if(vm.moves.isEmpty()) "Already solved" else "Cube solved",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.SemiBold)
  Text(if(vm.isReplay) "Solution replay complete." else "Every face is back in order.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  CubeView(vm.cube,modifier=Modifier.fillMaxWidth().height(280.dp))
  Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer) {
   Row(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=16.dp),horizontalArrangement=Arrangement.SpaceBetween) {
    Column { Text("MOVES",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text(vm.moves.size.toString(),style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold) }
    Column(horizontalAlignment=Alignment.End) { Text("STATUS",style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant);Text("Verified",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.SemiBold) }
   }
  }
  Spacer(Modifier.height(26.dp)); Primary("Solve another cube") { vm.scan() }
  TextButton(onClick={vm.viewSolution()},enabled=vm.moves.isNotEmpty()) { Text("View solution again") }
 }
}
