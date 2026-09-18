package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import kotlin.math.*

@Composable internal fun ScanPuzzlePickerScreen(onScan:(PuzzleId)->Unit) {
 val preferences=LocalAppPreferences.current
 val available=PuzzleRegistry.all.filter { it.solverState==SolverState.AVAILABLE }
 val planned=PuzzleRegistry.all.filterNot { it.solverState==SolverState.AVAILABLE }
 Column(Modifier.fillMaxSize()) {
  PageIntro("What are you solving?", subtitle = "Choose a supported puzzle.")
  Spacer(Modifier.height(18.dp))
  LazyVerticalGrid(columns=GridCells.Adaptive(140.dp),modifier=Modifier.weight(1f),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=18.dp)) {
   item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}) { SectionLabel("Ready to scan") }
   items(available,key={it.id}) { puzzle ->
    val active=puzzle.id==preferences.puzzleId
    Surface(onClick={preferences.updatePuzzle(puzzle.id);onScan(puzzle.id)},shape=RubixTokens.cardShape,
     color=if(active) MaterialTheme.colorScheme.primaryContainer.copy(alpha=.72f) else MaterialTheme.colorScheme.surfaceContainer,
     border=BorderStroke(1.dp,if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
     modifier=Modifier.height(148.dp)) {
     Column(Modifier.fillMaxSize().padding(14.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
      PuzzleGlyph(puzzle,Modifier.size(72.dp));Spacer(Modifier.height(8.dp))
      Text(puzzle.shortName,style=MaterialTheme.typography.titleMedium,textAlign=TextAlign.Center,maxLines=1)
     }
    }
   }
   item(span={androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan)}) { SectionLabel("Coming later",Modifier.padding(top=12.dp)) }
   items(planned,key={it.id}) { puzzle ->
    Surface(shape=RubixTokens.cardShape,color=MaterialTheme.colorScheme.surfaceContainer.copy(alpha=.58f),modifier=Modifier.height(112.dp)) {
     Column(Modifier.fillMaxSize().padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
      Box { PuzzleGlyph(puzzle,Modifier.size(50.dp));Icon(Icons.Rounded.Lock,null,Modifier.align(Alignment.BottomEnd).size(18.dp),tint=MaterialTheme.colorScheme.onSurfaceVariant) }
      Spacer(Modifier.height(6.dp));Text(puzzle.shortName,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant,maxLines=1)
     }
    }
   }
  }
 }
}

@Composable private fun PuzzleGlyph(puzzle:PuzzleSpec,modifier:Modifier) {
 val primary=Color(0xFF2F8CFF);val yellow=Color(0xFFFFE43B);val red=Color(0xFFE83D54);val green=Color(0xFF1FA66A)
 Canvas(modifier) {
  val s=size.minDimension;val c=Offset(size.width/2,size.height/2)
  when(puzzle.scanShape) {
   ScanShape.SQUARE_GRID -> {
    val n=puzzle.squareSize ?: 3;val gap=max(1.5f,s*0.025f);val edge=(s-(n-1)*gap)/n
    repeat(n*n) { i -> drawRoundRect(listOf(yellow,primary,red,green)[(i+i/n)%4],Offset(i%n*(edge+gap),i/n*(edge+gap)),androidx.compose.ui.geometry.Size(edge,edge),androidx.compose.ui.geometry.CornerRadius(edge*0.12f)) }
   }
   ScanShape.TRIANGLE_GRID -> {
    val p=Path().apply { moveTo(c.x,s*0.05f);lineTo(s*0.96f,s*0.9f);lineTo(s*0.04f,s*0.9f);close() };drawPath(p,green);drawLine(Color(0xFF071116),Offset(c.x,s*0.05f),Offset(c.x,s*0.9f),s*0.045f);drawLine(Color(0xFF071116),Offset(s*0.27f,s*0.48f),Offset(s*0.73f,s*0.48f),s*0.045f)
   }
   ScanShape.PENTAGON -> {
    val points=(0..4).map { i -> val a=-PI/2+i*2*PI/5;Offset(c.x+cos(a).toFloat()*s*.46f,c.y+sin(a).toFloat()*s*.46f) };val p=Path().apply { moveTo(points[0].x,points[0].y);points.drop(1).forEach{lineTo(it.x,it.y)};close() };drawPath(p,yellow);points.forEach { drawLine(Color(0xFF071116),c,it,s*.035f) }
   }
   ScanShape.SKEWB_FACE -> { drawRoundRect(primary,cornerRadius=androidx.compose.ui.geometry.CornerRadius(s*.12f));drawLine(Color(0xFF071116),Offset(0f,0f),Offset(s,s),s*.05f);drawLine(Color(0xFF071116),Offset(s,0f),Offset(0f,s),s*.05f);drawCircle(yellow,s*.16f,c) }
   ScanShape.CLOCK_FACE -> { drawCircle(Color(0xFF172A35),s*.48f,c);repeat(9){i->val x=c.x+(i%3-1)*s*.23f;val y=c.y+(i/3-1)*s*.23f;drawCircle(yellow,s*.08f,Offset(x,y));drawLine(red,Offset(x,y),Offset(x,y-s*.06f),s*.025f)} }
   ScanShape.SQUARE_ONE -> { drawRoundRect(yellow,Offset(0f,s*.08f),androidx.compose.ui.geometry.Size(s,s*.34f),androidx.compose.ui.geometry.CornerRadius(s*.12f));drawRoundRect(red,Offset(0f,s*.58f),androidx.compose.ui.geometry.Size(s,s*.34f),androidx.compose.ui.geometry.CornerRadius(s*.12f));drawLine(Color(0xFF071116),Offset(0f,c.y),Offset(s,c.y),s*.07f) }
  }
 }
}
