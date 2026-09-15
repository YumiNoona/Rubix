package com.cubeguide.core

import kotlin.test.*
import org.junit.Test

class PuzzleSpecTest {
 @Test fun registryContainsExactlyTheNineDistinctWcaPuzzleTypes() {
  assertEquals(9,PuzzleRegistry.all.size)
  assertEquals(9,PuzzleRegistry.all.map { it.id }.toSet().size)
  assertEquals(PuzzleId.THREE_BY_THREE,PuzzleRegistry.default.id)
 }
 @Test fun everyPuzzleDefinesACompleteScanAndMoveContract() {
  PuzzleRegistry.all.forEach { puzzle ->
   assertTrue(puzzle.scanViews>0,puzzle.name)
   assertTrue(puzzle.stickersPerView>0,puzzle.name)
   assertTrue(puzzle.totalObservedStickers>0,puzzle.name)
  }
  assertEquals(setOf(2,3,4,5),PuzzleRegistry.all.mapNotNull { it.squareSize }.toSet())
 }
 @Test fun unknownPersistedPuzzleFallsBackToThreeByThree() {
  assertEquals(PuzzleId.THREE_BY_THREE,PuzzleId.fromStorage("bad"))
  PuzzleId.entries.forEach { assertEquals(it,PuzzleId.fromStorage(it.storageId)) }
 }
}
