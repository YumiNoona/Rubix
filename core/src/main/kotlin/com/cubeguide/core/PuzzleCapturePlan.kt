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
  faces(PuzzleId.TWO_BY_TWO,2),faces(PuzzleId.THREE_BY_THREE,3),faces(PuzzleId.FOUR_BY_FOUR,4),faces(PuzzleId.FIVE_BY_FIVE,5),
  PuzzleCapturePlan(PuzzleId.CLOCK,"Keep pins unchanged between photos.",listOf(CaptureStep("front","Front dials and pins","Photograph all nine front dials and four pins.",13),CaptureStep("back","Back dials and pins","Flip left-to-right and photograph all nine back dials and pins.",13))),
  PuzzleCapturePlan(PuzzleId.MEGAMINX,"Follow the numbered face order without twisting the puzzle between adjacent captures.",(1..12).map { CaptureStep("face-$it","Face $it","Align the pentagon and show all eleven facelets.",11) }),
  PuzzleCapturePlan(PuzzleId.PYRAMINX,"Keep one vertex pointing upward for each guided face.",listOf("Green","Red","Blue","Yellow").mapIndexed { index,color -> CaptureStep("face-${index+1}","$color face","Point one tip upward and show all nine triangular facelets.",9) }),
  PuzzleCapturePlan(PuzzleId.SKEWB,"Follow the corner markers so face orientation is preserved.",(1..6).map { CaptureStep("face-$it","Face $it","Show the center and four corner regions.",5) }),
  PuzzleCapturePlan(PuzzleId.SQUARE_ONE,"Do not turn either layer between captures.",listOf(CaptureStep("top","Top layer","Capture piece colors and widths around the top ring.",12),CaptureStep("bottom","Bottom layer","Capture piece colors and widths around the bottom ring.",12),CaptureStep("equator","Equator","Capture both middle pieces and the current slice alignment.",2))),
 )
 fun get(id:PuzzleId)=all.first { it.puzzle==id }
}
