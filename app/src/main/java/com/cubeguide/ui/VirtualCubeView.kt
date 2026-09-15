package com.cubeguide.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.cubeguide.core.*
import com.cubeguide.play.VirtualCube
import kotlin.math.*

private data class VP(val x: Float,val y: Float,val z: Float) {
 operator fun plus(v: VP)=VP(x+v.x,y+v.y,z+v.z);operator fun times(k: Float)=VP(x*k,y*k,z*k)
 fun cross(v: VP)=VP(y*v.z-z*v.y,z*v.x-x*v.z,x*v.y-y*v.x)
 fun dot(v: VP)=x*v.x+y*v.y+z*v.z
}
private fun Vec.vp()=VP(x.toFloat(),y.toFloat(),z.toFloat())
private data class VQuad(val points: List<VP>,val normal: VP,val color: Color,val outline: Boolean=true)
@Composable internal fun VirtualCubeView(cube: VirtualCube,modifier: Modifier=Modifier) {
 val preferences=LocalAppPreferences.current
 var yaw by remember { mutableFloatStateOf(-0.62f) };var pitch by remember { mutableFloatStateOf(0.52f) }
 val pulse=remember { Animatable(1f) }
 LaunchedEffect(cube.stickers) { pulse.snapTo(0.965f);pulse.animateTo(1f,spring(stiffness=Spring.StiffnessMediumLow,dampingRatio=0.72f)) }
 Canvas(modifier.semantics { contentDescription="Interactive ${cube.size} by ${cube.size} virtual cube. Drag to rotate." }.pointerInput(Unit) { detectDragGestures { change,drag -> change.consume();yaw+=drag.x*.008f;pitch=(pitch+drag.y*.008f).coerceIn(-1.2f,1.2f) } }) {
  drawCircle(brush=Brush.radialGradient(listOf(Color(0x2639E4BB),Color.Transparent),center=Offset(size.width*.5f,size.height*.48f),radius=size.minDimension*.48f),radius=size.minDimension*.48f,center=Offset(size.width*.5f,size.height*.48f))
  drawOval(Color(0x66000000),topLeft=Offset(size.width*.23f,size.height*.79f),size=androidx.compose.ui.geometry.Size(size.width*.54f,size.height*.08f))
  fun camera(p: VP): VP { val x=p.x*cos(yaw)+p.z*sin(yaw);val z=-p.x*sin(yaw)+p.z*cos(yaw);return VP(x,p.y*cos(pitch)-z*sin(pitch),p.y*sin(pitch)+z*cos(pitch)) }
  val edge=cube.size-1f;val quads=mutableListOf<VQuad>()
  Face.entries.forEach { face ->
   val normal=Geometry.normals[face.ordinal].vp();val right=Geometry.rights[face.ordinal].vp();val down=Geometry.downs[face.ordinal].vp()
   val center=normal*(edge+.08f);val span=edge+.96f
   quads+=VQuad(listOf(center+right*-span+down*-span,center+right*span+down*-span,center+right*span+down*span,center+right*-span+down*span).map(::camera),camera(normal),Color(0xFF05090B),false)
  }
  VirtualCube.geometry(cube.size).forEachIndexed { index,g ->
   val pos=g.position.vp();val normal=g.normal.vp();val right=g.right.vp();val down=g.down.vp();val center=pos+normal*.16f
   val half=.82f
   quads+=VQuad(listOf(center+right*-half+down*-half,center+right*half+down*-half,center+right*half+down*half,center+right*-half+down*half).map(::camera),camera(normal),Color(preferences.color(cube.stickers[index])))
  }
  val scale=min(size.width,size.height)*.31f/max(2f,cube.size.toFloat())*pulse.value
  fun project(p:VP):Offset { val perspective=9f/(9f-p.z/max(1f,edge));return Offset(size.width/2+p.x*scale*perspective,size.height/2-p.y*scale*perspective) }
  quads.filter { q -> val center=q.points.reduce { a,b -> a+b }*.25f;q.normal.dot(VP(0f,0f,12f)+center*-1f)>0 }.sortedBy { it.points.map { p->p.z }.average() }.forEach { quad ->
   val points=quad.points.map(::project);val path=Path().apply { moveTo(points[0].x,points[0].y);points.drop(1).forEach { lineTo(it.x,it.y) };close() }
   drawPath(path,quad.color)
   if(quad.outline) {
    drawPath(path,brush=Brush.linearGradient(listOf(Color.White.copy(alpha=.09f),Color.Transparent),points[0],points[2]))
    drawPath(path,Color(0xFF05090B),style=Stroke(max(2f,5f/cube.size)))
   }
  }
 }
}
