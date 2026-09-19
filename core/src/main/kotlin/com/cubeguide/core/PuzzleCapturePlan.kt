package com.cubeguide.core

data class CaptureStep(
 val key:String,
 val title:String,
 val instruction:String,
 val observedValues:Int,
)

data class PuzzleCapturePlan(
 val puzzle:PuzzleId,
 val preparation:String,
 val steps:List<CaptureStep>,
) {
 init { require(steps.isNotEmpty());require(steps.map { it.key }.distinct().size==steps.size) }
 val observedValues get()=steps.sumOf { it.observedValues }
}

/** Capture contracts used by the picker, scanner, restoration and review layers. */
object PuzzleCapturePlans {
 private fun faces(puzzle:PuzzleId,size:Int)=PuzzleCapturePlan(
  puzzle,"Keep the same physical orientation while moving through all six faces.",
  listOf("Top","Right","Front","Bottom","Left","Back").mapIndexed { i,name ->
   CaptureStep("face-$i",name,"Show the complete $name face with every sticker visible.",size*size)
  }
 )
 val all=listOf(
  faces(PuzzleId.TWO_BY_TWO,2),faces(PuzzleId.THREE_BY_THREE,3),faces(PuzzleId.FOUR_BY_FOUR,4),
  faces(PuzzleId.FIVE_BY_FIVE,5),faces(PuzzleId.SIX_BY_SIX,6),faces(PuzzleId.SEVEN_BY_SEVEN,7),
 )
 fun get(id:PuzzleId)=all.first { it.puzzle==id }
}
