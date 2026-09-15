package com.cubeguide.rendering

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.cubeguide.core.*
import com.cubeguide.ui.LocalAppPreferences
import kotlin.math.*

private data class FPoint(val x:Float,val y:Float,val z:Float) {
 operator fun plus(v:FPoint)=FPoint(x+v.x,y+v.y,z+v.z);operator fun times(k:Float)=FPoint(x*k,y*k,z*k)
 fun dot(v:FPoint)=x*v.x+y*v.y+z*v.z;fun cross(v:FPoint)=FPoint(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x)
}
private fun Vec.fp()=FPoint(x/3f,y/3f,z/3f)
private data class FQuad(val points:List<FPoint>,val color:Color)

@Composable fun FourByFourView(state:FourByFourState,modifier:Modifier=Modifier,move:FourByFourMove?=null,replay:Int=0,onAnimationProgress:(Float)->Unit={}) {
 val preferences=LocalAppPreferences.current;val animation=remember(state,move){Animatable(0f)};val callback by rememberUpdatedState(onAnimationProgress)
 var yaw by remember { mutableFloatStateOf(-.55f) };var pitch by remember { mutableFloatStateOf(.45f) }
 LaunchedEffect(animation) { snapshotFlow { animation.value }.collect { callback(it) } }
 LaunchedEffect(animation,replay) { if(move!=null) animation.animateTo(1f,tween(preferences.animationMillis)) }
 Canvas(modifier.clipToBounds().pointerInput(Unit){detectDragGestures { change,drag -> change.consume();yaw+=drag.x*.008f;pitch=(pitch+drag.y*.008f).coerceIn(-1.3f,1.3f) }}) {
  val axis=move?.face?.let { Geometry.normals[it.ordinal].fp() };val angle=if(move==null) 0f else -animation.value*(if(move.turns==3)-1 else move.turns)*PI.toFloat()/2
  fun rotating(position:Vec)=axis!=null && position.fp().dot(axis)>=if(move!!.wide) .32f else .98f
  fun layer(point:FPoint,position:Vec):FPoint { if(!rotating(position)) return point;return point*cos(angle)+axis!!.cross(point)*sin(angle)+axis*(axis.dot(point)*(1-cos(angle))) }
  fun camera(point:FPoint):FPoint { val x=point.x*cos(yaw)+point.z*sin(yaw);val z=-point.x*sin(yaw)+point.z*cos(yaw);return FPoint(x,point.y*cos(pitch)-z*sin(pitch),point.y*sin(pitch)+z*cos(pitch)) }
  val quads=mutableListOf<FQuad>();val coords=listOf(-3,-1,1,3)
  Face.entries.forEach { face -> repeat(16) { index ->
   val n=Geometry.normals[face.ordinal];val r=Geometry.rights[face.ordinal];val d=Geometry.downs[face.ordinal]
   val position=n*3+r*coords[index%4]+d*coords[index/4];val center=position.fp()+n.fp()*.035f;val right=r.fp()*.145f;val down=d.fp()*.145f
   quads+=FQuad(listOf(center+right*-1f+down*-1f,center+right+down*-1f,center+right+down,center+right*-1f+down).map { camera(layer(it,position)) },Color(preferences.color(state.stickers[face.ordinal*16+index])))
  } }
  val scale=min(size.width,size.height)*.29f
  fun project(p:FPoint):Offset { val perspective=6f/(6f-p.z);return Offset(size.width/2+p.x*scale*perspective,size.height/2-p.y*scale*perspective) }
  quads.filter { q -> val inward=(q.points[1]+q.points[0]*-1f).cross(q.points[3]+q.points[0]*-1f);val center=q.points.reduce { a,b->a+b }*.25f;inward.dot(FPoint(0f,0f,6f)+center*-1f)<0f }.sortedBy { it.points.map(FPoint::z).average() }.forEach { quad ->
   val p=quad.points.map(::project);val path=Path().apply { moveTo(p[0].x,p[0].y);p.drop(1).forEach { lineTo(it.x,it.y) };close() }
   drawPath(path,quad.color);drawPath(path,Color(0xFF071116),style=Stroke(2.2f))
  }
 }
}
