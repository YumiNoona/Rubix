package com.cubeguide.core

sealed interface PocketScanOrientationResult {
 data class Unique(val cube:PocketCube,val rotations:List<Int>):PocketScanOrientationResult
 data object Ambiguous:PocketScanOrientationResult
 data object Impossible:PocketScanOrientationResult
}

/** Repairs independent camera-face rotations while keeping the guided U/R/F/D/L/B face order. */
object PocketScanOrientationResolver {
 private fun rotate(face:List<CubeColor>,turns:Int):List<CubeColor> {
  var value=face
  repeat(turns) { value=listOf(value[2],value[0],value[3],value[1]) }
  return value
 }
 fun resolve(faces:List<List<CubeColor>>):PocketScanOrientationResult {
  if(faces.size!=6 || faces.any { it.size!=4 }) return PocketScanOrientationResult.Impossible
  var found:PocketScanOrientationResult.Unique?=null
  val rotations=IntArray(6)
  fun search(face:Int):Boolean {
   if(face==6) {
    val cube=PocketCube(faces.flatMapIndexed { index,colors -> rotate(colors,rotations[index]) })
    if(TwoByTwoEngine.validate(cube)!=null) return false
    val candidate=PocketScanOrientationResult.Unique(cube,rotations.toList())
    if(found!=null && found!!.cube!=cube) return true
    found=candidate;return false
   }
   for(turn in 0..3) { rotations[face]=turn;if(search(face+1)) return true }
   return false
  }
  return if(search(0)) PocketScanOrientationResult.Ambiguous else found ?: PocketScanOrientationResult.Impossible
 }
 fun validateGuided(faces:List<List<CubeColor>>):PocketCube? {
  if(faces.size!=6 || faces.any { it.size!=4 }) return null
  return runCatching { PocketCube(faces.flatten()) }.getOrNull()?.takeIf { TwoByTwoEngine.validate(it)==null }
 }
}
