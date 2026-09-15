package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material.icons.rounded.PlayArrow
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

@Composable internal fun PuzzleCatalogScreen(onPlay:()->Unit,onScan:(PuzzleId)->Unit,scanMode:Boolean=false) {
 val preferences=LocalAppPreferences.current
 val selected=PuzzleRegistry.get(preferences.puzzleId)
 Column(Modifier.fillMaxSize()) {
  Text(if(scanMode) "Scan and solve" else "Choose a puzzle",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
  Text(if(scanMode) "Choose the puzzle in your hands. Verified scanners can be opened below." else "Every solver is enabled only after its moves can be replay-verified.",style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
  Spacer(Modifier.height(14.dp))
  LazyVerticalGrid(
   columns=GridCells.Adaptive(102.dp),modifier=Modifier.weight(1f),
   horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp),
   contentPadding=PaddingValues(bottom=12.dp),
  ) {
   items(PuzzleRegistry.all,key={it.id}) { puzzle ->
    val active=puzzle.id==selected.id
    Surface(onClick={preferences.updatePuzzle(puzzle.id)},shape=RoundedCornerShape(18.dp),
     color=if(active) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
     border=BorderStroke(if(active) 2.dp else 1.dp,if(active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant),
     modifier=Modifier.height(126.dp)) {
     Column(Modifier.fillMaxSize().padding(9.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center) {
      PuzzleGlyph(puzzle,Modifier.size(62.dp));Spacer(Modifier.height(5.dp))
      Text(puzzle.shortName,style=MaterialTheme.typography.labelLarge,fontWeight=FontWeight.SemiBold,textAlign=TextAlign.Center,maxLines=1)
     }
    }
   }
  }
  Surface(color=MaterialTheme.colorScheme.surfaceContainer,shape=RoundedCornerShape(18.dp),modifier=Modifier.fillMaxWidth()) {
   Column(Modifier.padding(14.dp)) {
    Text(selected.name,fontWeight=FontWeight.SemiBold)
    Text(when(selected.solverState) {
     SolverState.AVAILABLE -> "Camera scan, verified solve, 3D guide and timer are ready."
     SolverState.ENGINE_READY -> "The verified solver is ready. Camera review and 2D/3D guidance are next."
     SolverState.ENGINE_PENDING -> "Its dedicated scanner, validator and replay-verified solver are being built."
    },style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
   }
  }
  Spacer(Modifier.height(10.dp))
  if(selected.solverState==SolverState.AVAILABLE) {
   Button(onClick={onScan(selected.id)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)) {
    Icon(Icons.Rounded.PhotoCamera,null);Spacer(Modifier.width(8.dp));Text("Scan and solve ${selected.shortName}")
   }
  } else Button(onClick={},enabled=false,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)) { Text("Verified solver in progress") }
  if(!scanMode && selected.supportsCubePlayground) {
   TextButton(onClick=onPlay,modifier=Modifier.fillMaxWidth()) { Icon(Icons.Rounded.PlayArrow,null);Spacer(Modifier.width(6.dp));Text("Open ${selected.shortName} playground") }
  } else Spacer(Modifier.height(12.dp))
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
