package com.cubeguide.core
import cs.min2phase.Tools
import kotlin.test.*
import org.junit.Test
import kotlin.random.Random

class CubeTest {
 @Test fun everyMoveMatchesReference() {
  val scrambled="R U2 F' L D B2 R' D2 F L2"
  val cube=CubeState.solved().apply(Move.parse(scrambled))
  for(f in Face.entries) for(t in 1..3) {
   val m=Move(f,t)
   assertEquals(Tools.fromScramble("$scrambled ${m.notation}"),cube.apply(m).facelets(),m.notation)
   assertEquals(cube,cube.apply(m).apply(m.inverse()))
   assertEquals(cube,cube.apply(List(4) { Move(f) }))
   assertEquals(cube.apply(Move(f,2)),cube.apply(List(2) { Move(f) }))
  }
 }
 @Test fun randomSolutionsSolveAndRemainValid() {
  val random=Random(17)
  repeat(60) {
   val scramble=List(25) { Move(Face.entries[random.nextInt(6)],random.nextInt(1,4)) }
   val cube=CubeState.solved().apply(scramble)
   assertEquals(Tools.fromScramble(scramble.joinToString(" ") { it.notation }),cube.facelets())
   assertNull(Validator.validate(cube))
   assertTrue(cube.apply(Solver.solve(cube)).solved)
  }
 }
 @Test fun impossibleStatesAreActionable() {
  fun swap(a: Int,b: Int): CubeState { val s=CubeState.solved().stickers.toMutableList(); val tmp=s[a]; s[a]=s[b]; s[b]=tmp; return CubeState(s) }
  assertTrue(Validator.validate(swap(5,10))!!.message.contains("flipped"))
  val s=CubeState.solved().stickers.toMutableList(); val c=s[8]; s[8]=s[9]; s[9]=s[20]; s[20]=c
  assertTrue(Validator.validate(CubeState(s))!!.message.contains("twisted"))
  assertTrue(Validator.validate(swap(10,19))!!.message.contains("swapped"))
  assertNotNull(Validator.validate(swap(0,10)))
  assertNotNull(Validator.validate(swap(9,20))) // mirrored URF corner
  assertNotNull(Validator.validate(swap(10,52))) // opposite-colored edge
  val counts=CubeState.solved().stickers.toMutableList(); counts[0]=CubeColor.RED
  assertTrue(Validator.validate(CubeState(counts))!!.message.contains("exactly 9"))
  val centers=CubeState.solved().stickers.toMutableList(); val old=centers[13]; centers[13]=centers[4]; centers[0]=old
  assertTrue(Validator.validate(CubeState(centers))!!.message.contains("center"))
 }
 @Test fun optimizationPreservesState() {
  assertEquals("R2",Solver.optimize(Move.parse("R R")).single().notation)
  assertEquals("R'",Solver.optimize(Move.parse("R R R")).single().notation)
  assertTrue(Solver.optimize(Move.parse("R U U' R'")).isEmpty())
  val m=Move.parse("F R R U U' R' D2 D2 B B B B F'")
  assertEquals(CubeState.solved().apply(m),CubeState.solved().apply(Solver.optimize(m)))
 }
 @Test fun solvedHasNoMovesAndParsingIsStrict() {
  assertTrue(Solver.solve(CubeState.solved()).isEmpty())
  assertFailsWith<IllegalArgumentException> { Move.parse("R x F") }
  assertEquals(18,Face.entries.flatMap { f -> (1..3).map { Move(f,it).instruction } }.toSet().size)
 }
}
