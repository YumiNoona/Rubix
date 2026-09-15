package com.cubeguide.core

import cs.threephase.Search

data class FourByFourMove(val face:Face,val turns:Int=1,val wide:Boolean=false) {
 init { require(turns in 1..3) }
 val notation get()=face.name+(if(wide) "w" else "")+when(turns){2->"2";3->"'";else->""}
 fun inverse()=copy(turns=4-turns)
 companion object {
  private val pattern=Regex("([URFDLB])(w)?([2']?)")
  fun parse(text:String)=text.trim().split(Regex("\\s+")).filter(String::isNotBlank).map { token ->
   val match=pattern.matchEntire(token) ?: error("Invalid 4×4 move: $token")
   FourByFourMove(Face.valueOf(match.groupValues[1]),when(match.groupValues[3]){"2"->2;"'"->3;else->1},match.groupValues[2].isNotEmpty())
  }
 }
}

internal object FourGeometry {
 val coordinates=listOf(-3,-1,1,3)
 val stickers=Face.entries.flatMap { face -> (0 until 16).map { index ->
  val normal=Geometry.normals[face.ordinal];val right=Geometry.rights[face.ordinal];val down=Geometry.downs[face.ordinal]
  StickerGeometry(normal*3+right*coordinates[index%4]+down*coordinates[index/4],normal,right,down)
 } }
}

data class FourByFourState(val stickers:List<CubeColor>) {
 init { require(stickers.size==96) }
 fun apply(move:FourByFourMove):FourByFourState {
  var current=stickers;val axis=Geometry.normals[move.face.ordinal]
  repeat(move.turns) {
   val output=current.toMutableList()
   FourGeometry.stickers.forEachIndexed { index,sticker ->
    val depth=sticker.position.dot(axis)
    if(depth==3 || move.wide && depth==1) {
     val position=Geometry.rotate(sticker.position,axis);val normal=Geometry.rotate(sticker.normal,axis)
     val target=FourGeometry.stickers.indexOfFirst { it.position==position && it.normal==normal }
     check(target>=0);output[target]=current[index]
    }
   }
   current=output
  }
  return FourByFourState(current)
 }
 fun apply(moves:List<FourByFourMove>)=moves.fold(this) { state,move -> state.apply(move) }
 val solved get()=Face.entries.map { face -> stickers.drop(face.ordinal*16).take(16) }.let { faces -> faces.all { colors -> colors.distinct().size==1 } && faces.map { it.first() }.toSet().size==6 }
 fun facelets()=stickers.joinToString("") { "URFDLB"[it.ordinal].toString() }
 companion object { fun solved()=FourByFourState(Face.entries.flatMap { face -> List(16){CubeColor.entries[face.ordinal]} }) }
}

object FourByFourValidator {
 fun validate(state:FourByFourState):String? {
  CubeColor.entries.forEach { color -> val count=state.stickers.count { it==color };if(count!=16) return "${color.label} has $count stickers; a 4×4 needs exactly 16." }
  val groups=FourGeometry.stickers.indices.groupBy { FourGeometry.stickers[it].position }
  fun signature(indices:List<Int>,stickers:List<CubeColor>)=indices.map { stickers[it].ordinal }.sorted()
  val solved=FourByFourState.solved().stickers
  val expectedCorners=groups.values.filter { it.size==3 }.map { signature(it,solved) }.sortedBy { it.joinToString() }
  val actualCorners=groups.values.filter { it.size==3 }.map { signature(it,state.stickers) }.sortedBy { it.joinToString() }
  if(actualCorners!=expectedCorners) return "A 4×4 corner has impossible or duplicate colors."
  val expectedWings=groups.values.filter { it.size==2 }.map { signature(it,solved) }.groupingBy { it }.eachCount()
  val actualWings=groups.values.filter { it.size==2 }.map { signature(it,state.stickers) }.groupingBy { it }.eachCount()
  if(actualWings!=expectedWings) return "A 4×4 edge wing has impossible or duplicate colors."
  return null
 }
}

object FourByFourEngine:PuzzleEngine<FourByFourState> {
 override val puzzle=PuzzleId.FOUR_BY_FOUR
 override fun validate(state:FourByFourState)=FourByFourValidator.validate(state)
 @Synchronized override fun solve(state:FourByFourState):List<String> {
  require(validate(state)==null);if(state.solved) return emptyList()
  val search=Search().apply { inverse_solution=false;with_rotation=false }
  return FourByFourMove.parse(search.solution(state.facelets())).map { it.notation }
 }
 override fun apply(state:FourByFourState,move:String)=state.apply(FourByFourMove.parse(move).single())
 override fun isSolved(state:FourByFourState)=state.solved
}
