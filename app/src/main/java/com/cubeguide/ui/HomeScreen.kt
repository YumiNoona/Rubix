package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import com.cubeguide.rendering.CubeView
import kotlinx.coroutines.delay

@Composable internal fun Home(vm:CubeViewModel) {
 val preferences=LocalAppPreferences.current;val feedback=rememberTouchFeedback();val scramble=remember { Move.parse("R U F2 L D B2") };val solution=remember { scramble.reversed().map { it.inverse() } }
 var cube by remember { mutableStateOf(CubeState.solved().apply(scramble)) };var turn by remember { mutableStateOf<Move?>(null) };var caption by remember { mutableStateOf("Scrambled") }
 LaunchedEffect(preferences.animationMillis) { while(true) { turn=null;cube=CubeState.solved().apply(scramble);caption="Scrambled";delay(900);for(move in solution) { caption="Finding order";turn=move;delay(preferences.animationMillis.toLong()*(if(move.turns==2)2 else 1)+(if(move.turns==2)180 else 0)+120);turn=null;cube=cube.apply(move);delay(80) };caption="Solved";delay(1800) } }
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
  Text("Solve. Play. Improve.",style=MaterialTheme.typography.headlineLarge);Text("Everything you need for your cube, in one calm workspace.",color=MaterialTheme.colorScheme.onSurfaceVariant)
  Box(Modifier.fillMaxWidth().height(210.dp)) { CubeView(cube,move=turn,modifier=Modifier.fillMaxSize().semantics { contentDescription="Cube changing from scrambled to solved" });Surface(Modifier.align(Alignment.BottomCenter),shape=RoundedCornerShape(50),color=MaterialTheme.colorScheme.primaryContainer) { Text(caption,Modifier.padding(horizontal=14.dp,vertical=6.dp),style=MaterialTheme.typography.labelMedium) } }
  Surface(shape=RoundedCornerShape(26.dp),color=MaterialTheme.colorScheme.primaryContainer) { Column(Modifier.padding(18.dp)) { Row(verticalAlignment=Alignment.CenterVertically) { FeatureGlyph(FeatureIcon.SCAN,Modifier.size(36.dp));Spacer(Modifier.width(12.dp));Column { Text("Solve my 3x3",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text("Scan six faces and follow the guide",color=MaterialTheme.colorScheme.onPrimaryContainer) } };Spacer(Modifier.height(16.dp));Button(onClick={feedback();vm.scan()},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp)) { Text("Start camera scan") };Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) { TextButton(onClick={feedback();vm.manual()}) { Text("Enter colors") };TextButton(onClick={feedback();vm.demo()}) { Text("Try demo") } } } }
  Spacer(Modifier.height(14.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) { DashboardCard("Virtual cube","2x2 to 7x7",FeatureIcon.CUBE,Modifier.weight(1f)) { vm.open(Screen.VIRTUAL) };DashboardCard("Cube timer","Best + avg 5",FeatureIcon.TIMER,Modifier.weight(1f)) { vm.open(Screen.TIMER) } }
  Spacer(Modifier.height(10.dp));Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) { DashboardCard("Learn solve","7 short lessons",FeatureIcon.LEARN,Modifier.weight(1f)) { vm.open(Screen.LEARN) };DashboardCard("Puzzle library","Choose your cube",FeatureIcon.PUZZLES,Modifier.weight(1f)) { vm.open(Screen.PUZZLES) } }
  Spacer(Modifier.height(16.dp))
 }
}
@Composable private fun DashboardCard(title:String,subtitle:String,icon:FeatureIcon,modifier:Modifier,onClick:()->Unit) { val feedback=rememberTouchFeedback();Surface(onClick={feedback();onClick()},modifier=modifier.height(126.dp),shape=RoundedCornerShape(22.dp),color=MaterialTheme.colorScheme.surfaceContainer) { Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.SpaceBetween) { FeatureGlyph(icon,Modifier.size(30.dp));Column { Text(title,fontWeight=FontWeight.SemiBold);Text(subtitle,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } } } }
