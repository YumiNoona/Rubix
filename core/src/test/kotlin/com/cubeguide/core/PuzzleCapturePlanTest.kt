package com.cubeguide.core

import kotlin.test.*
import org.junit.Test

class PuzzleCapturePlanTest {
 @Test fun everyPuzzleHasOneCompleteCaptureContract() {
  assertEquals(PuzzleId.entries.toSet(),PuzzleCapturePlans.all.map { it.puzzle }.toSet())
  PuzzleCapturePlans.all.forEach { plan ->
   assertTrue(plan.steps.all { it.title.isNotBlank() && it.instruction.isNotBlank() && it.observedValues>0 })
  }
 }
 @Test fun squareFacePlansMatchRegistryObservationCounts() {
  listOf(PuzzleId.TWO_BY_TWO,PuzzleId.THREE_BY_THREE,PuzzleId.FOUR_BY_FOUR,PuzzleId.FIVE_BY_FIVE).forEach { id ->
   assertEquals(PuzzleRegistry.get(id).totalObservedStickers,PuzzleCapturePlans.get(id).observedValues)
  }
 }
}
