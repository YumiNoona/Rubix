package com.cubeguide.core

import kotlin.random.Random
import kotlin.test.*
import org.junit.Test

class PocketScanOrientationTest {
 private fun rotate(face:List<CubeColor>,turns:Int):List<CubeColor> {
  var value=face;repeat(turns) { value=listOf(value[2],value[0],value[3],value[1]) };return value
 }
 @Test fun legalGuidedScansNeverResolveAsImpossible() {
  val random=Random(41);var cube=PocketCube.solved()
  repeat(30) {
   cube=cube.apply(Move(Face.entries.random(random),random.nextInt(1,4)))
   val faces=(0..5).map { face -> cube.stickers.drop(face*4).take(4) }
   assertNotEquals(PocketScanOrientationResult.Impossible,PocketScanOrientationResolver.resolve(faces))
   assertEquals(cube,PocketScanOrientationResolver.validateGuided(faces))
  }
 }
 @Test fun independentlyRotatedPhotosRemainRecoverable() {
  val cube=PocketCube.solved().apply(Move.parse("R U2 F' L D R2 U'"))
  val turns=listOf(1,3,2,0,1,2)
  val photos=(0..5).map { face -> rotate(cube.stickers.drop(face*4).take(4),turns[face]) }
  assertNotEquals(PocketScanOrientationResult.Impossible,PocketScanOrientationResolver.resolve(photos))
 }
 @Test fun badColorCountIsImpossible() {
  val faces=(0..5).map { face -> PocketCube.solved().stickers.drop(face*4).take(4).toMutableList() }
  faces[0][0]=CubeColor.RED
  assertEquals(PocketScanOrientationResult.Impossible,PocketScanOrientationResolver.resolve(faces))
 }
}
