package com.cubeguide.play

import com.cubeguide.core.*

data class VirtualCube(val size: Int,val stickers: List<CubeColor>,val history: List<Move> = emptyList()) {
 init { require(size in 2..7);require(stickers.size==6*size*size) }
 fun apply(move: Move,record: Boolean=true): VirtualCube {
  var current=stickers
  val axis=Geometry.normals[move.face.ordinal];val edge=size-1
  repeat(move.turns) {
   val output=current.toMutableList();val geometry=geometry(size)
   geometry.forEachIndexed { index,sticker -> if(sticker.position.dot(axis)==edge) {
    val position=Geometry.rotate(sticker.position,axis);val normal=Geometry.rotate(sticker.normal,axis)
    val target=geometry.indexOfFirst { it.position==position && it.normal==normal }
    check(target>=0);output[target]=current[index]
   } }
   current=output
  }
  return VirtualCube(size,current,if(record) history+move else history)
 }
 fun reset()=solved(size)
 fun encode(): String = buildString {
  append(size);append('|')
  append(stickers.joinToString("") { it.ordinal.toString() });append('|')
  append(history.joinToString(" ") { it.notation })
 }
 val solved get()=Face.entries.all { face -> (0 until size*size).all { stickers[face.ordinal*size*size+it]==stickers[face.ordinal*size*size] } }
 companion object {
  fun solved(size: Int)=VirtualCube(size,Face.entries.flatMap { face -> List(size*size) { CubeColor.entries[face.ordinal] } })
  fun decode(value: String): VirtualCube? = runCatching {
   val fields=value.split('|',limit=3)
   val size=fields[0].toInt()
   val stickers=fields[1].map { CubeColor.entries[it.digitToInt()] }
   val history=if(fields.getOrElse(2) { "" }.isBlank()) emptyList() else Move.parse(fields[2])
   VirtualCube(size,stickers,history)
  }.getOrNull()
  fun geometry(size: Int): List<StickerGeometry> {
   val edge=size-1
   return Face.entries.flatMap { face -> (0 until size*size).map { index ->
    val normal=Geometry.normals[face.ordinal];val right=Geometry.rights[face.ordinal];val down=Geometry.downs[face.ordinal]
    StickerGeometry(normal*edge+right*(2*(index%size)-edge)+down*(2*(index/size)-edge),normal,right,down)
   } }
  }
 }
}
fun virtualScramble(size: Int,count: Int=if(size==2) 9 else 18): List<Move> {
 val out=mutableListOf<Move>()
 while(out.size<count) {
  val move=Move(Face.entries.random(),(1..3).random())
  if(out.lastOrNull()?.face!=move.face) out+=move
 }
 return out
}
