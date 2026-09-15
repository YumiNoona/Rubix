package com.cubeguide.core

import kotlin.random.Random
import kotlin.test.*
import org.junit.Test

class PyraminxScanResolverTest {
 private fun rotate(face:List<PyraminxColor>,turns:Int):List<PyraminxColor> {
  var value=face;repeat(turns) { val next=MutableList(9){PyraminxColor.GREEN};repeat(9){i->next[(i+3)%9]=value[i]};value=next };return value
 }
 @Test fun guidedPhotosReconstructExactState() {
  val random=Random(55);repeat(50) {
   val moves=List(20) { PyraminxMove(PyraminxAxis.entries.random(random),random.nextInt(1,3),random.nextBoolean()) }
   val facelets=PyraminxFacelets.solved().apply(moves);val faces=(0..3).map { facelets.stickers.drop(it*9).take(9) }
   val result=assertIs<PyraminxScanResult.Unique>(PyraminxScanResolver.resolveGuided(faces))
   assertEquals(facelets,result.facelets);assertEquals(PyraminxState.solved().apply(moves),result.state)
  }
 }
 @Test fun unorderedRotatedPhotosAreNeverAcceptedAsImpossibleWhenLegal() {
  val facelets=PyraminxFacelets.solved().apply(PyraminxMove.parse("U R' l B u' L"))
  val rotations=listOf(2,0,1,2)
  val photos=(0..3).map { rotate(facelets.stickers.drop(it*9).take(9),rotations[it]) }.shuffled(Random(9))
  assertNotEquals(PyraminxScanResult.Impossible,PyraminxScanResolver.resolve(photos))
 }
 @Test fun wrongColorCountIsImpossible() {
  val faces=(0..3).map { PyraminxFacelets.solved().stickers.drop(it*9).take(9).toMutableList() }
  faces[0][0]=PyraminxColor.RED
  assertEquals(PyraminxScanResult.Impossible,PyraminxScanResolver.resolve(faces))
 }
}
