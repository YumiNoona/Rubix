package com.cubeguide.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.CubeState
import com.cubeguide.core.Move
import com.cubeguide.rendering.CubeView

private enum class Skill(val label:String) { ROOKIE("Rookie"), EXPERIENCED("Experienced"), VETERAN("Veteran") }
private data class Lesson(val id:Int,val skill:Skill,val title:String,val goal:String,val notice:String,val plan:String,val scramble:String)
private val lessons=listOf(
 Lesson(0,Skill.ROOKIE,"How the cube moves","Learn centers, edges and corners.","Centers set each face color.","Rotate the model and make one turn at a time.","R U"),
 Lesson(1,Skill.ROOKIE,"Read cube notation","Recognize U, R, F, prime and double turns.","A move is viewed straight at that face.","Say the move, find the face, then turn.","R U R'"),
 Lesson(2,Skill.ROOKIE,"Build the white cross","Place four white edges around the white center.","Each side color must match its center too.","Match the side first, then lift the edge.","F R U R' U' F'"),
 Lesson(3,Skill.ROOKIE,"Finish the first layer","Insert the four white corners.","A corner belongs between its three centers.","Place it below its slot and repeat the trigger.","R U R' U'"),
 Lesson(4,Skill.ROOKIE,"Middle layer","Insert edges that have no yellow.","The top sticker tells you left or right.","Match the front center, move away, then insert.","U R U' R' U' F' U F"),
 Lesson(5,Skill.ROOKIE,"Last layer","Orient and position the yellow pieces.","Shape first, piece location second.","Make the yellow face, place corners, then edges.","F R U R' U' F'"),
 Lesson(100,Skill.EXPERIENCED,"Plan during inspection","Find the first cross pieces before timing starts.","Track an edge relative to both centers.","Plan two cross moves, then extend the plan.","D R2 F' U"),
 Lesson(101,Skill.EXPERIENCED,"Finger tricks","Turn without regripping after every move.","Stable thumbs make U and R turns flow.","Practice slowly and keep each move clean.","R U R' U' R' F R F'"),
 Lesson(102,Skill.EXPERIENCED,"Pair F2L pieces","Join a corner and edge before inserting them.","Separate joined pieces when their colors clash.","Find, pair, insert, then look for the next pair.","R U2 R' U' R U R'"),
 Lesson(103,Skill.EXPERIENCED,"Two-look last layer","Use shape recognition to reduce hesitation.","Ignore side colors during orientation.","Recognize OLL, execute, then recognize PLL.","R U R' U R U2 R'"),
 Lesson(200,Skill.VETERAN,"Full cross planning","Plan the whole cross and first pair.","Efficient crosses usually stay on the bottom.","Trace all four edges before the first turn.","F2 D R' L2 U2"),
 Lesson(201,Skill.VETERAN,"Look-ahead F2L","Find the next pair while inserting this one.","Pauses cost more than slightly slower turns.","Turn evenly and keep your eyes off the active pair.","R U R' U' R U2 R'"),
 Lesson(202,Skill.VETERAN,"Fast last-layer recognition","Recognize OLL and PLL from fewer angles.","Use blocks and headlights instead of sticker counting.","Name the case before executing its algorithm.","R2 U R U R' U' R' U' R' U R'"),
 Lesson(203,Skill.VETERAN,"Solve rhythm","Build speed without losing control.","Clean turns prevent lockups and recoveries.","Alternate accuracy solves with controlled speed solves.","R U2 R' U' R U' R'")
)

private fun example(lesson:Lesson)=Move.parse(lesson.scramble).fold(CubeState.solved()) { cube,move -> cube.apply(move) }
private fun solution(lesson:Lesson)=Move.parse(lesson.scramble).asReversed().joinToString("  ") { it.inverse().notation }

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun LearnScreen(vm:CubeViewModel) {
 val preferences=LocalAppPreferences.current
 var skill by rememberSaveable { mutableStateOf<Skill?>(null) }
 var selected by remember { mutableStateOf<Lesson?>(null) }
 var showSolved by remember { mutableStateOf(false) }
 BackHandler(enabled=skill!=null) { skill=null }
 if(skill==null) LearnPathPicker { skill=it }
 else LearnLessonList(skill!!,onBack={skill=null},onLesson={lesson -> showSolved=false;selected=lesson})
 selected?.let { lesson ->
  ModalBottomSheet(onDismissRequest={selected=null}) {
   Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal=20.dp).navigationBarsPadding()) {
    Text(lesson.title,style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.Center) {
     FilterChip(selected=!showSolved,onClick={showSolved=false},label={Text("Problem")})
     Spacer(Modifier.width(8.dp));FilterChip(selected=showSolved,onClick={showSolved=true},label={Text("Solved")})
    }
    Surface(shape=RubixTokens.cardShape,color=MaterialTheme.colorScheme.surfaceContainer,modifier=Modifier.fillMaxWidth()) { CubeView(if(showSolved) CubeState.solved() else example(lesson),Modifier.fillMaxWidth().height(176.dp)) }
    Insight("LOOK FOR",lesson.notice)
    Spacer(Modifier.height(8.dp));Insight("PLAN",lesson.plan)
    Spacer(Modifier.height(8.dp));Insight("MOVES",solution(lesson))
    Spacer(Modifier.height(16.dp))
    Button(onClick={preferences.completeLesson(lesson.id);selected=null;vm.startLessonPractice(lesson.title,lesson.scramble)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)) {
     Icon(Icons.Rounded.PlayArrow,"Practise lesson");Spacer(Modifier.width(8.dp));Text("Practise",maxLines=1)
    }
    Spacer(Modifier.height(14.dp))
   }
  }
 }
}

@Composable private fun LearnPathPicker(onSelect:(Skill)->Unit) {
 Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
  PageIntro("Learn to solve",subtitle="Choose a path, study the cube, then practise each idea.")
  Spacer(Modifier.height(RubixTokens.sectionGap));SectionLabel("Choose your path");Spacer(Modifier.height(10.dp))
  Skill.entries.forEach { skill -> SkillCard(skill) { onSelect(skill) };Spacer(Modifier.height(10.dp)) }
  Spacer(Modifier.height(88.dp))
 }
}

@Composable private fun LearnLessonList(skill:Skill,onBack:()->Unit,onLesson:(Lesson)->Unit) {
 val feedback=rememberTouchFeedback();val visible=lessons.filter { it.skill==skill }
 Column(Modifier.fillMaxSize()) {
  Row(Modifier.fillMaxWidth().height(58.dp),verticalAlignment=Alignment.CenterVertically) {
   IconButton(onClick={feedback();onBack()},modifier=Modifier.offset(x=(-12).dp).size(48.dp)) { Icon(Icons.AutoMirrored.Rounded.ArrowBack,"Back to learning paths",Modifier.size(25.dp)) }
   Column(Modifier.weight(1f)) { Text(skill.label,style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold);Text(skill.subtitle(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1) }
  }
  Spacer(Modifier.height(8.dp))
  LazyColumn(verticalArrangement=Arrangement.spacedBy(10.dp)) {
   items(visible,key={it.id}) { lesson ->
    Card(onClick={onLesson(lesson)},modifier=Modifier.fillMaxWidth(),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.surfaceContainer),shape=RubixTokens.cardShape) {
     Row(Modifier.fillMaxWidth().heightIn(min=104.dp).padding(12.dp),verticalAlignment=Alignment.CenterVertically) {
      Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceContainerHighest) { CubeView(example(lesson),Modifier.size(82.dp)) }
      Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)) { Text(lesson.title,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,maxLines=1);Spacer(Modifier.height(4.dp));Text(lesson.goal,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=2) }
      Spacer(Modifier.width(8.dp));Icon(Icons.AutoMirrored.Rounded.ArrowForward,"Open lesson",tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(20.dp))
     }
    }
   }
   item { Spacer(Modifier.height(88.dp)) }
  }
 }
}

private fun Skill.subtitle()=when(this){Skill.ROOKIE->"Foundations";Skill.EXPERIENCED->"Efficiency";Skill.VETERAN->"Speed"}

@Composable private fun SkillCard(skill:Skill,onClick:()->Unit) {
 val icon=when(skill){Skill.ROOKIE->Icons.Rounded.School;Skill.EXPERIENCED->Icons.Rounded.Speed;Skill.VETERAN->Icons.Rounded.Bolt}
 val feedback=rememberTouchFeedback()
 Surface(onClick={feedback();onClick()},modifier=Modifier.fillMaxWidth().height(94.dp),shape=RubixTokens.cardShape,color=MaterialTheme.colorScheme.surfaceContainer,border=androidx.compose.foundation.BorderStroke(1.dp,MaterialTheme.colorScheme.outlineVariant)) {
  Row(Modifier.fillMaxSize().padding(16.dp),verticalAlignment=Alignment.CenterVertically) {
   Surface(shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.primaryContainer,modifier=Modifier.size(54.dp)) { Box(contentAlignment=Alignment.Center) { Icon(icon,null,tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(28.dp)) } }
   Spacer(Modifier.width(14.dp));Column(Modifier.weight(1f)) { Text(skill.label,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold,maxLines=1);Text(skill.subtitle(),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1) }
   Icon(Icons.AutoMirrored.Rounded.ArrowForward,"Open ${skill.label}",tint=MaterialTheme.colorScheme.primary,modifier=Modifier.size(22.dp))
  }
 }
}

@Composable private fun Insight(label:String,text:String) {
 Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=RoundedCornerShape(14.dp),modifier=Modifier.fillMaxWidth()) {
  Row(Modifier.padding(14.dp)) { Text(label,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold,style=MaterialTheme.typography.labelMedium,modifier=Modifier.width(72.dp));Text(text,style=MaterialTheme.typography.bodyMedium,modifier=Modifier.weight(1f)) }
 }
}
