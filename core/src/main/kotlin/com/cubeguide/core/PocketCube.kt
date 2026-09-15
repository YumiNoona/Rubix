package com.cubeguide.core

data class PocketCube(val stickers:List<CubeColor>) {
 init { require(stickers.size==24) }
 fun apply(move:Move):PocketCube {
  var current=stickers;val axis=Geometry.normals[move.face.ordinal]
  repeat(move.turns) {
   val output=current.toMutableList()
   geometry.forEachIndexed { index,sticker -> if(sticker.position.dot(axis)==1) {
    val position=Geometry.rotate(sticker.position,axis);val normal=Geometry.rotate(sticker.normal,axis)
    val target=geometry.indexOfFirst { it.position==position && it.normal==normal }
    check(target>=0);output[target]=current[index]
   } }
   current=output
  }
  return PocketCube(current)
 }
 fun apply(moves:List<Move>)=moves.fold(this) { state,move -> state.apply(move) }
 val solved get()=Face.entries.all { face -> (0..3).all { stickers[face.ordinal*4+it]==CubeColor.entries[face.ordinal] } }

 fun embeddedThreeByThree():CubeState? {
  if(CubeColor.entries.any { color -> stickers.count { it==color }!=4 }) return null
  val base=CubeState.solved().stickers.toMutableList()
  geometry.forEachIndexed { index,sticker ->
   val target=Geometry.stickers.indexOfFirst { it.position==sticker.position && it.normal==sticker.normal }
   check(target>=0);base[target]=stickers[index]
  }
  val even=CubeState(base)
  if(Validator.validate(even)==null) return even
  val odd=base.toMutableList()
  val ur=Geometry.stickers.indexOfFirst { it.position==Vec(1,1,0) && it.normal==Vec(1,0,0) }
  val uf=Geometry.stickers.indexOfFirst { it.position==Vec(0,1,1) && it.normal==Vec(0,0,1) }
  val swap=odd[ur];odd[ur]=odd[uf];odd[uf]=swap
  return CubeState(odd).takeIf { Validator.validate(it)==null }
 }

 companion object {
  val geometry=Face.entries.flatMap { face -> (0..3).map { index ->
   val normal=Geometry.normals[face.ordinal];val right=Geometry.rights[face.ordinal];val down=Geometry.downs[face.ordinal]
   StickerGeometry(normal+right*(2*(index%2)-1)+down*(2*(index/2)-1),normal,right,down)
  } }
  fun solved()=PocketCube(Face.entries.flatMap { face -> List(4) { CubeColor.entries[face.ordinal] } })
 }
}

object TwoByTwoEngine:PuzzleEngine<PocketCube> {
 override val puzzle=PuzzleId.TWO_BY_TWO
 override fun validate(state:PocketCube):String? {
  CubeColor.entries.forEach { color ->
   val count=state.stickers.count { it==color }
   if(count!=4) return "${color.label} has $count stickers; a 2×2 needs exactly 4."
  }
  return if(state.embeddedThreeByThree()==null) "The corner colors or orientations do not form a solvable 2×2." else null
 }
 override fun solve(state:PocketCube)=Solver.solve(requireNotNull(state.embeddedThreeByThree())).map { it.notation }
 override fun apply(state:PocketCube,move:String)=state.apply(Move.parse(move).single())
 override fun isSolved(state:PocketCube)=state.solved
}
