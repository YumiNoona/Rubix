package com.cubeguide.rendering

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.nativeCanvas
import com.cubeguide.ui.LocalAppPreferences
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.cubeguide.core.*
import kotlin.math.*

private data class P(val x: Float,val y: Float,val z: Float) {
 operator fun plus(v: P)=P(x+v.x,y+v.y,z+v.z)
 operator fun times(k: Float)=P(x*k,y*k,z*k)
 fun dot(v: P)=x*v.x+y*v.y+z*v.z
 fun cross(v: P)=P(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x)
}
private fun Vec.p()=P(x.toFloat(),y.toFloat(),z.toFloat())
private data class Quad(val vertices: List<P>,val color: Color,val letter: String?=null,val ink: Int=0)
@Composable fun CubeView(cube: CubeState, modifier: Modifier=Modifier, move: Move?=null, replay: Int=0,viewReset: Int=0) {
 val preferences=LocalAppPreferences.current
 val initials=preferences.initials
 val letterPaint=remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textAlign=android.graphics.Paint.Align.CENTER; typeface=android.graphics.Typeface.DEFAULT_BOLD } }
 val animation=remember { Animatable(0f) }
 var yaw by remember { mutableFloatStateOf(-0.55f) }; var pitch by remember { mutableFloatStateOf(0.45f) }
 LaunchedEffect(viewReset) { yaw=-0.55f; pitch=0.45f }
 LaunchedEffect(cube,move,replay) { animation.snapTo(0f); if(move!=null) { animation.animateTo(1f,tween(preferences.animationMillis)) } }
 Canvas(modifier.clipToBounds().pointerInput(Unit) { detectDragGestures { change,drag -> change.consume(); yaw+=drag.x*0.008f; pitch=(pitch+drag.y*0.008f).coerceIn(-1.3f,1.3f) } }) {
  val axis=move?.let { Geometry.normals[it.face.ordinal].p() }
  val angle=if(move==null) 0f else -animation.value*(if(move.turns==3) -1 else move.turns)*PI.toFloat()/2
  fun layer(v: P,pos: P): P {
   if(axis==null || pos.dot(axis)<0.9f) return v
   return v*cos(angle)+axis.cross(v)*sin(angle)+axis*(axis.dot(v)*(1-cos(angle)))
  }
  fun camera(p: P): P {
   val x=p.x*cos(yaw)+p.z*sin(yaw); val z=-p.x*sin(yaw)+p.z*cos(yaw)
   return P(x,p.y*cos(pitch)-z*sin(pitch),p.y*sin(pitch)+z*cos(pitch))
  }
  val quads=mutableListOf<Quad>()
  for(x in -1..1) for(y in -1..1) for(z in -1..1) {
   if(x==0 && y==0 && z==0) continue
   val pos=P(x.toFloat(),y.toFloat(),z.toFloat())
   Face.entries.forEach { f ->
    val n=Geometry.normals[f.ordinal].p(); val r=Geometry.rights[f.ordinal].p(); val d=Geometry.downs[f.ordinal].p()
    val center=pos+n*0.48f
    quads+=Quad(listOf(center+r*-0.48f+d*-0.48f,center+r*0.48f+d*-0.48f,center+r*0.48f+d*0.48f,center+r*-0.48f+d*0.48f).map { camera(layer(it,pos)) },Color(0xFF151C1A))
   }
  }
  Geometry.stickers.forEachIndexed { i,g ->
   val pos=g.position.p(); val center=pos+g.normal.p()*0.49f; val r=g.right.p()*0.40f; val d=g.down.p()*0.40f
   quads+=Quad(listOf(center+r*-1f+d*-1f,center+r+d*-1f,center+r+d,center+r*-1f+d).map { camera(layer(it,pos)) },Color(preferences.color(cube.stickers[i])),if(initials) cube.stickers[i].initial else null,preferences.ink(cube.stickers[i]).toInt())
  }
  val scale=min(size.width,size.height)*0.17f
  fun project(p: P): Offset { val perspective=7f/(7f-p.z); return Offset(size.width/2+p.x*scale*perspective,size.height/2-p.y*scale*perspective) }
  quads.sortedBy { q -> q.vertices.map { it.z }.average() }.forEach { q ->
   val points=q.vertices.map(::project); val path=Path().apply { moveTo(points[0].x,points[0].y); points.drop(1).forEach { lineTo(it.x,it.y) }; close() }
   drawPath(path,q.color); drawPath(path,Color(0xFF0C1410),style=Stroke(1.5f))
   q.letter?.let { letter ->
    val width=(points[1]-points[0]).getDistance(); val height=(points[3]-points[0]).getDistance()
    val fontSize=min(width,height)*0.48f
    if(fontSize>=10f) {
     letterPaint.color=q.ink; letterPaint.textSize=fontSize
     val cx=points.map { it.x }.average().toFloat(); val cy=points.map { it.y }.average().toFloat()
     drawContext.canvas.nativeCanvas.drawText(letter,cx,cy-(letterPaint.ascent()+letterPaint.descent())/2,letterPaint)
    }
   }
  }
 }
}
