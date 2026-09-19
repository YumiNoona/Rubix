package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import kotlin.math.max

@Composable internal fun ScanPuzzlePickerScreen(onScan:(PuzzleId)->Unit) {
 val preferences=LocalAppPreferences.current
 val selected=PuzzleRegistry.get(preferences.puzzleId)
 Column(Modifier.fillMaxSize()) {
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
  if(selected.solverState==SolverState.AVAILABLE) {
   Button(onClick={onScan(selected.id)},modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)) {
    Icon(Icons.Rounded.PhotoCamera,null);Spacer(Modifier.width(8.dp));Text("Scan ${selected.shortName}")
   }
  } else Button(onClick={},enabled=false,modifier=Modifier.fillMaxWidth().height(52.dp),shape=RoundedCornerShape(16.dp)) { Text("${selected.shortName} scanner unavailable") }
  Spacer(Modifier.height(12.dp))
 }
}

@Composable private fun PuzzleGlyph(puzzle:PuzzleSpec,modifier:Modifier) {
 val primary=Color(0xFF2F8CFF);val yellow=Color(0xFFFFE43B);val red=Color(0xFFE83D54);val green=Color(0xFF1FA66A)
 Canvas(modifier) {
  val s=size.minDimension
    val n=puzzle.squareSize ?: 3;val gap=max(1.5f,s*0.025f);val edge=(s-(n-1)*gap)/n
    repeat(n*n) { i -> drawRoundRect(listOf(yellow,primary,red,green)[(i+i/n)%4],Offset(i%n*(edge+gap),i/n*(edge+gap)),androidx.compose.ui.geometry.Size(edge,edge),androidx.compose.ui.geometry.CornerRadius(edge*0.12f)) }
 }
}
