package com.cubeguide.core

import cs.min2phase.Search
import cs.min2phase.Tools

enum class CubeColor(val label: String, val argb: Long) {
 WHITE("White", 0xFFE9ECF2), RED("Red", 0xFFB72235), GREEN("Green", 0xFF15824F),
 YELLOW("Yellow", 0xFFD2AF1B), ORANGE("Orange", 0xFFD96B12), BLUE("Blue", 0xFF2463BB);
 val initial: String get() = label.take(1)
 val inkArgb: Long get() = if(this in listOf(RED,GREEN,BLUE)) 0xFFFFFFFF else 0xFF101820
}
enum class Face(val label: String) { U("top"), R("right"), F("front"), D("bottom"), L("left"), B("back") }
data class Vec(val x: Int, val y: Int, val z: Int) {
 operator fun plus(v: Vec) = Vec(x+v.x,y+v.y,z+v.z)
 operator fun times(k: Int) = Vec(x*k,y*k,z*k)
 fun dot(v: Vec) = x*v.x+y*v.y+z*v.z
 fun cross(v: Vec) = Vec(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x)
}
data class StickerGeometry(val position: Vec, val normal: Vec, val right: Vec, val down: Vec)
object Geometry {
 val normals = listOf(Vec(0,1,0),Vec(1,0,0),Vec(0,0,1),Vec(0,-1,0),Vec(-1,0,0),Vec(0,0,-1))
 val rights = listOf(Vec(1,0,0),Vec(0,0,-1),Vec(1,0,0),Vec(1,0,0),Vec(0,0,1),Vec(-1,0,0))
 val downs = listOf(Vec(0,0,1),Vec(0,-1,0),Vec(0,-1,0),Vec(0,0,-1),Vec(0,-1,0),Vec(0,-1,0))
 val stickers = Face.entries.flatMap { f -> (0..8).map { i ->
  val n=normals[f.ordinal]; val r=rights[f.ordinal]; val d=downs[f.ordinal]
  StickerGeometry(n+r*(i%3-1)+d*(i/3-1),n,r,d)
 } }
 fun rotate(v: Vec, axis: Vec): Vec = axis*v.dot(axis)+axis.cross(v)*-1
}
data class Move(val face: Face, val turns: Int = 1) {
 init { require(turns in 1..3) }
 val notation: String get() = face.name + when(turns) { 2 -> "2"; 3 -> "'"; else -> "" }
 val instruction: String get() = "Turn the "+face.label+" face "+when(turns) { 2 -> "twice"; 3 -> "counter-clockwise"; else -> "clockwise" }
 fun inverse() = Move(face,4-turns)
 companion object {
  fun parse(sequence: String): List<Move> = sequence.trim().split(Regex("\\s+")).filter { it.isNotBlank() }.map {
   require(it.matches(Regex("[URFDLB][2']?"))) { "Unrecognized move: $it" }
   Move(Face.valueOf(it.take(1)),when(it.getOrNull(1)) { '2' -> 2; '\'' -> 3; else -> 1 })
  }
 }
}
data class CubeState(val stickers: List<CubeColor>) {
 init { require(stickers.size==54) }
 fun apply(move: Move): CubeState {
  var current=stickers
  val axis=Geometry.normals[move.face.ordinal]
  repeat(move.turns) {
   val output=current.toMutableList()
   Geometry.stickers.forEachIndexed { i,g -> if(g.position.dot(axis)==1) {
    val pos=Geometry.rotate(g.position,axis); val normal=Geometry.rotate(g.normal,axis)
    val target=Geometry.stickers.indexOfFirst { it.position==pos && it.normal==normal }
    check(target>=0); output[target]=current[i]
   } }
   current=output
  }
  return CubeState(current)
 }
 fun apply(moves: List<Move>) = moves.fold(this) { c,m -> c.apply(m) }
 fun facelets(): String {
  val centers=Face.entries.map { stickers[it.ordinal*9+4] }
  require(centers.toSet().size==6) { "Each face must have a different center color." }
  return stickers.joinToString("") { Face.entries[centers.indexOf(it)].name }
 }
 val solved: Boolean get() = Face.entries.all { f -> (0..8).all { stickers[f.ordinal*9+it]==stickers[f.ordinal*9+4] } }
 companion object { fun solved() = CubeState(Face.entries.flatMap { f -> List(9) { CubeColor.entries[f.ordinal] } }) }
}
data class ValidationIssue(val message: String, val suspectStickers: List<Int> = emptyList())
object Validator {
 fun validate(cube: CubeState): ValidationIssue? {
  CubeColor.entries.forEach { color ->
   val count=cube.stickers.count { it==color }
   if(count!=9) return ValidationIssue("${color.label} has $count stickers; it needs exactly 9. Check or rescan that color.",cube.stickers.indices.filter { cube.stickers[it]==color })
  }
  if(Face.entries.map { cube.stickers[it.ordinal*9+4] }.toSet().size!=6) return ValidationIssue("Two faces have the same center. Rescan the faces with duplicate centers.")
  val facelets=cube.facelets()
  val edges=listOf(listOf(5,10),listOf(7,19),listOf(3,37),listOf(1,46),listOf(32,16),listOf(28,25),listOf(30,43),listOf(34,52),listOf(23,12),listOf(21,41),listOf(50,39),listOf(48,14))
  val corners=listOf(listOf(8,9,20),listOf(6,18,38),listOf(0,36,47),listOf(2,45,11),listOf(29,26,15),listOf(27,44,24),listOf(33,53,42),listOf(35,17,51))
  val expectedEdges=edges.map { piece -> piece.map { Face.entries[it/9].name[0] }.sorted() }
  val expectedCorners=corners.map { piece -> piece.map { Face.entries[it/9].name[0] } }
  val seenEdges=mutableSetOf<Int>(); val seenCorners=mutableSetOf<Int>()
  for(piece in edges) {
   val colors=piece.map { facelets[it] }.sorted(); val identity=expectedEdges.indexOf(colors)
   if(identity<0 || !seenEdges.add(identity)) return ValidationIssue("An edge has impossible or duplicate colors. Check these edge stickers and face orientation.",piece)
  }
  for(piece in corners) {
   val colors=piece.map { facelets[it] }
   val identity=expectedCorners.indexOfFirst { expected -> (0..2).any { shift -> (0..2).all { colors[it]==expected[(it+shift)%3] } } }
   if(identity<0 || !seenCorners.add(identity)) return ValidationIssue("A corner has impossible or duplicate colors. Check these corner stickers and face orientation.",piece)
  }
  val code=Tools.verify(facelets)
  return when(code) {
   0 -> null
   -2 -> ValidationIssue("An edge is missing or duplicated. Check adjacent edge stickers and face orientation.")
   -3 -> ValidationIssue("One edge is flipped. Check the edge colors; a single flipped edge cannot be solved with turns.")
   -4 -> ValidationIssue("A corner is missing or duplicated. Check corner stickers and scan orientation.")
   -5 -> ValidationIssue("A corner is twisted. Check the corner colors; this orientation is physically impossible with turns.")
   -6 -> ValidationIssue("Two pieces appear swapped. Check the face orientations and stickers, or whether the cube was reassembled.")
   else -> ValidationIssue("The scan needs correction. Check all six faces and their orientation.")
  }
 }
}
object Solver {
 @Synchronized fun solve(cube: CubeState): List<Move> {
  Validator.validate(cube)?.let { error(it.message) }
  if(cube.solved) return emptyList()
  Search.init()
  val search=Search()
  var result=search.solution(cube.facelets(),21,100000,0,0)
  var attempts=0
  while(result=="Error 8" && attempts++<10) result=search.next(100000,0,0)
  check(!result.startsWith("Error")) { "Could not finish solving. Please try again. ($result)" }
  val moves=optimize(Move.parse(result))
  check(cube.apply(moves).solved) { "Solution verification failed. Rescan the cube." }
  return moves
 }
 fun optimize(moves: List<Move>): List<Move> {
  val out=mutableListOf<Move>()
  moves.forEach { move ->
   if(out.lastOrNull()?.face==move.face) {
    val sum=(out.removeAt(out.lastIndex).turns+move.turns)%4
    if(sum!=0) out+=Move(move.face,sum)
   } else out+=move
  }
  return out
 }
}
// Viewed straight on, the indicated neighboring center must be above each scan.
data class ScanPose(val face: Face, val top: Face, val guidance: String)
val scanSequence = listOf(
 ScanPose(Face.F,Face.U,"Start with green facing the camera and white on top."),
 ScanPose(Face.R,Face.U,"Rotate the whole cube so red faces the camera. Keep white on top."),
 ScanPose(Face.B,Face.U,"Rotate the whole cube so blue faces the camera. Keep white on top."),
 ScanPose(Face.L,Face.U,"Rotate the whole cube so orange faces the camera. Keep white on top."),
 ScanPose(Face.U,Face.B,"Show white to the camera with the blue side above it."),
 ScanPose(Face.D,Face.F,"Show yellow to the camera with the green side above it.")
)
