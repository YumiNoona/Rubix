package com.cubeguide.core

/** The regular NxN cubes supported throughout Rubix. */
enum class PuzzleId(val storageId:String) {
 TWO_BY_TWO("222"),
 THREE_BY_THREE("333"),
 FOUR_BY_FOUR("444"),
 FIVE_BY_FIVE("555"),
 SIX_BY_SIX("666"),
 SEVEN_BY_SEVEN("777");

 companion object {
  fun fromStorage(value:String?)=entries.firstOrNull { it.storageId==value } ?: THREE_BY_THREE
 }
}

enum class ScanShape { SQUARE_GRID }
enum class MoveFamily { CUBE }
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
  PuzzleSpec(PuzzleId.TWO_BY_TWO,"Pocket Cube","2×2",ScanShape.SQUARE_GRID,6,4,MoveFamily.CUBE,SolverState.AVAILABLE,2),
  PuzzleSpec(PuzzleId.THREE_BY_THREE,"Rubik's Cube","3×3",ScanShape.SQUARE_GRID,6,9,MoveFamily.CUBE,SolverState.AVAILABLE,3),
  PuzzleSpec(PuzzleId.FOUR_BY_FOUR,"Rubik's Revenge","4×4",ScanShape.SQUARE_GRID,6,16,MoveFamily.CUBE,SolverState.AVAILABLE,4),
  PuzzleSpec(PuzzleId.FIVE_BY_FIVE,"Professor's Cube","5×5",ScanShape.SQUARE_GRID,6,25,MoveFamily.CUBE,SolverState.ENGINE_PENDING,5),
  PuzzleSpec(PuzzleId.SIX_BY_SIX,"6×6 Cube","6×6",ScanShape.SQUARE_GRID,6,36,MoveFamily.CUBE,SolverState.ENGINE_PENDING,6),
  PuzzleSpec(PuzzleId.SEVEN_BY_SEVEN,"7×7 Cube","7×7",ScanShape.SQUARE_GRID,6,49,MoveFamily.CUBE,SolverState.ENGINE_PENDING,7),
 )
 val default get()=all.first { it.id==PuzzleId.THREE_BY_THREE }
 fun get(id:PuzzleId)=all.first { it.id==id }
}
