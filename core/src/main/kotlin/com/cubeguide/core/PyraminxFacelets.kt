package com.cubeguide.core

enum class PyraminxColor { GREEN, RED, BLUE, YELLOW }

data class PyraminxFacelets(val stickers:List<PyraminxColor>) {
 init { require(stickers.size==36) }
 private fun index(face:Int,sticker:Int)=face*9+sticker
 private fun cycle(values:MutableList<PyraminxColor>,a:Int,b:Int,c:Int) { val held=values[a];values[a]=values[b];values[b]=values[c];values[c]=held }
 fun apply(move:PyraminxMove):PyraminxFacelets {
  var value=this
  repeat(move.turns) { value=value.quarter(move.axis,move.tipOnly) }
  return value
 }
 fun apply(moves:List<PyraminxMove>)=moves.fold(this) { state,move -> state.apply(move) }
 private fun quarter(axis:PyraminxAxis,tipOnly:Boolean):PyraminxFacelets {
  val out=stickers.toMutableList();val s=axis.ordinal
  fun swap(f1:Int,p1:Int,f2:Int,p2:Int,f3:Int,p3:Int)=cycle(out,index(f1,p1),index(f2,p2),index(f3,p3))
  if(!tipOnly) when(s) {
   0->{swap(0,8,3,8,2,2);swap(0,1,3,1,2,4);swap(0,2,3,2,2,5)}
   1->{swap(2,8,1,2,0,8);swap(2,7,1,1,0,7);swap(2,5,1,8,0,5)}
   2->{swap(3,8,0,5,1,5);swap(3,7,0,4,1,4);swap(3,5,0,2,1,2)}
   3->{swap(1,8,2,2,3,5);swap(1,7,2,1,3,4);swap(1,5,2,8,3,2)}
  }
  when(s) { 0->swap(0,0,3,0,2,3);1->swap(0,6,2,6,1,0);2->swap(0,3,1,3,3,6);3->swap(1,6,2,0,3,3) }
  return PyraminxFacelets(out)
 }
 fun toState():PyraminxState? {
  if(PyraminxColor.entries.any { color -> stickers.count { it==color }!=9 }) return null
  fun colors(vararg positions:Pair<Int,Int>)=positions.map { stickers[index(it.first,it.second)].ordinal }
  val edgeColors=listOf(colors(0 to 5,1 to 2),colors(0 to 8,2 to 5),colors(1 to 8,2 to 8),colors(0 to 2,3 to 8),colors(1 to 5,3 to 5),colors(2 to 2,3 to 2))
  val weights=intArrayOf(0,1,2,4);val permutation=edgeColors.map { weights[it[0]]+weights[it[1]]-1 }
  if(permutation.sorted()!=List(6){it} || edgeColors.any { it[0]==it[1] }) return null
  val orientation=edgeColors.map { if(it[0]>it[1]) 1 else 0 }
  val corners=listOf(colors(0 to 1,2 to 4,3 to 1),colors(0 to 7,1 to 1,2 to 7),colors(0 to 4,3 to 7,1 to 4),colors(1 to 7,3 to 4,2 to 1))
  val expected=listOf(setOf(0,2,3),setOf(0,1,2),setOf(0,1,3),setOf(1,2,3))
  if(corners.indices.any { corners[it].toSet()!=expected[it] }) return null
  val axial=corners.map { triple -> triple.indices.minBy { triple[it] } }
  val tips=listOf(colors(0 to 0,2 to 3,3 to 0),colors(0 to 6,1 to 0,2 to 6),colors(0 to 3,3 to 6,1 to 3),colors(1 to 6,3 to 3,2 to 0))
  if(tips.indices.any { tips[it].toSet()!=expected[it] }) return null
  val tipOrientation=tips.indices.map { i -> tips[i].indexOf(corners[i][0]).takeIf { it>=0 } ?: return null }
  return PyraminxState(permutation,orientation,axial,tipOrientation).takeIf { PyraminxValidator.validate(it)==null }
 }
 companion object { fun solved()=PyraminxFacelets(PyraminxColor.entries.flatMap { color -> List(9){color} }) }
}
