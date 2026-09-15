package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class PuzzleProfile(val size:Int,val name:String,val note:String)
private val puzzles=listOf(PuzzleProfile(2,"Pocket 2x2","Virtual cube + timer"),PuzzleProfile(3,"Classic 3x3","Camera solver + virtual + timer"),PuzzleProfile(4,"Revenge 4x4","Virtual cube + timer"),PuzzleProfile(5,"Professor 5x5","Virtual cube + timer"),PuzzleProfile(6,"6x6 cube","Virtual cube + timer"),PuzzleProfile(7,"7x7 cube","Virtual cube + timer"))
@Composable internal fun PuzzleCatalogScreen(onPlay:()->Unit) {
 val preferences=LocalAppPreferences.current
 Column(Modifier.fillMaxSize()) { Text("Choose the cube used in the playground and timer.",color=MaterialTheme.colorScheme.onSurfaceVariant);Spacer(Modifier.height(18.dp))
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(12.dp)) { puzzles.forEach { puzzle ->
   val selected=preferences.puzzleSize==puzzle.size
   Surface(onClick={preferences.updatePuzzleSize(puzzle.size)},shape=RoundedCornerShape(24.dp),color=if(selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,border=BorderStroke(if(selected)2.dp else 1.dp,if(selected)MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) { Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically) { MiniPuzzle(puzzle.size,Modifier.size(58.dp));Spacer(Modifier.width(16.dp));Column(Modifier.weight(1f)) { Text(puzzle.name,style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold);Text(puzzle.note,style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant) } } }
  };Spacer(Modifier.height(12.dp)) }
  Button(onClick=onPlay,modifier=Modifier.fillMaxWidth().heightIn(min=52.dp),shape=RoundedCornerShape(18.dp)) { Text("Open ${preferences.puzzleSize}×${preferences.puzzleSize} virtual cube") };Spacer(Modifier.height(12.dp))
 }
}
@Composable private fun MiniPuzzle(size:Int,modifier:Modifier) { androidx.compose.foundation.Canvas(modifier) { drawRoundRect(Color(0xFF071014),cornerRadius=androidx.compose.ui.geometry.CornerRadius(8f));val gap=2f;val edge=(this.size.minDimension-12f-(size-1)*gap)/size;for(row in 0 until size) for(col in 0 until size) drawRoundRect(if((row+col)%3==0) Color(0xFFFFE43B) else if(col<size/2) Color(0xFF15824F) else Color(0xFFD96B12),topLeft=androidx.compose.ui.geometry.Offset(6f+col*(edge+gap),6f+row*(edge+gap)),size=androidx.compose.ui.geometry.Size(edge,edge),cornerRadius=androidx.compose.ui.geometry.CornerRadius(2f)) } }
