package com.cubeguide.core

sealed interface VerifiedSolution {
 data class Ready(val puzzle:PuzzleId,val moves:List<String>):VerifiedSolution
 data class Rejected(val puzzle:PuzzleId,val reason:String):VerifiedSolution
}

/**
 * A puzzle engine is not considered usable merely because it returns moves.
 * The gate validates input, parses every move, replays the result, and checks
 * the engine's solved predicate before the UI may expose the guide.
 */
interface PuzzleEngine<S> {
 val puzzle:PuzzleId
 fun validate(state:S):String?
 fun solve(state:S):List<String>
 fun apply(state:S,move:String):S
 fun isSolved(state:S):Boolean
}

object PuzzleSolverGate {
 fun <S> solve(engine:PuzzleEngine<S>,state:S):VerifiedSolution {
  engine.validate(state)?.let { return VerifiedSolution.Rejected(engine.puzzle,it) }
  return try {
   val moves=engine.solve(state)
   val final=moves.fold(state,engine::apply)
   if(engine.isSolved(final)) VerifiedSolution.Ready(engine.puzzle,moves)
   else VerifiedSolution.Rejected(engine.puzzle,"The generated solution did not replay to a solved state.")
  } catch(error:Exception) {
   VerifiedSolution.Rejected(engine.puzzle,error.message ?: "The puzzle could not be solved safely.")
  }
 }
}

object ThreeByThreeEngine:PuzzleEngine<CubeState> {
 override val puzzle=PuzzleId.THREE_BY_THREE
 override fun validate(state:CubeState)=Validator.validate(state)?.message
 override fun solve(state:CubeState)=Solver.solve(state).map { it.notation }
 override fun apply(state:CubeState,move:String)=state.apply(Move.parse(move).single())
 override fun isSolved(state:CubeState)=state.solved
}
