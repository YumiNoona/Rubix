package com.cubeguide.core

/** The nine physical puzzle types in the WCA's standard speed-solving events. */
enum class PuzzleId(val storageId:String) {
 TWO_BY_TWO("222"),
 THREE_BY_THREE("333"),
 FOUR_BY_FOUR("444"),
 FIVE_BY_FIVE("555"),
 CLOCK("clock"),
 MEGAMINX("minx"),
 PYRAMINX("pyram"),
 SKEWB("skewb"),
 SQUARE_ONE("sq1");

 companion object {
  fun fromStorage(value:String?)=entries.firstOrNull { it.storageId==value } ?: THREE_BY_THREE
 }
}

enum class ScanShape { SQUARE_GRID, TRIANGLE_GRID, PENTAGON, SKEWB_FACE, CLOCK_FACE, SQUARE_ONE }
enum class MoveFamily { CUBE, PYRAMINX, SKEWB, MEGAMINX, SQUARE_ONE, CLOCK }
enum class SolverState { AVAILABLE, ENGINE_READY, ENGINE_PENDING }

data class PuzzleSpec(
 val id:PuzzleId,
 val name:String,
 val shortName:String,
 val scanShape:ScanShape,
 val scanViews:Int,
 val stickersPerView:Int,
 val moveFamily:MoveFamily,
 val solverState:SolverState,
 val squareSize:Int?=null,
) {
 val totalObservedStickers get()=scanViews*stickersPerView
 val supportsCubePlayground get()=squareSize!=null
}

object PuzzleRegistry {
 val all=listOf(
  PuzzleSpec(PuzzleId.TWO_BY_TWO,"Pocket Cube","2×2",ScanShape.SQUARE_GRID,6,4,MoveFamily.CUBE,SolverState.ENGINE_READY,2),
  PuzzleSpec(PuzzleId.THREE_BY_THREE,"Rubik's Cube","3×3",ScanShape.SQUARE_GRID,6,9,MoveFamily.CUBE,SolverState.AVAILABLE,3),
  PuzzleSpec(PuzzleId.FOUR_BY_FOUR,"Rubik's Revenge","4×4",ScanShape.SQUARE_GRID,6,16,MoveFamily.CUBE,SolverState.ENGINE_PENDING,4),
  PuzzleSpec(PuzzleId.FIVE_BY_FIVE,"Professor's Cube","5×5",ScanShape.SQUARE_GRID,6,25,MoveFamily.CUBE,SolverState.ENGINE_PENDING,5),
  PuzzleSpec(PuzzleId.CLOCK,"Clock","Clock",ScanShape.CLOCK_FACE,2,9,MoveFamily.CLOCK,SolverState.ENGINE_PENDING),
  PuzzleSpec(PuzzleId.MEGAMINX,"Megaminx","Mega",ScanShape.PENTAGON,12,11,MoveFamily.MEGAMINX,SolverState.ENGINE_PENDING),
  PuzzleSpec(PuzzleId.PYRAMINX,"Pyraminx","Pyra",ScanShape.TRIANGLE_GRID,4,9,MoveFamily.PYRAMINX,SolverState.ENGINE_PENDING),
  PuzzleSpec(PuzzleId.SKEWB,"Skewb","Skewb",ScanShape.SKEWB_FACE,6,5,MoveFamily.SKEWB,SolverState.ENGINE_PENDING),
  PuzzleSpec(PuzzleId.SQUARE_ONE,"Square-1","SQ-1",ScanShape.SQUARE_ONE,3,8,MoveFamily.SQUARE_ONE,SolverState.ENGINE_PENDING),
 )
 val default get()=all.first { it.id==PuzzleId.THREE_BY_THREE }
 fun get(id:PuzzleId)=all.first { it.id==id }
}
