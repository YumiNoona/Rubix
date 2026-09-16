package com.cubeguide.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.Composable
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import com.cubeguide.core.*
import kotlin.math.*

@Composable internal fun PyraminxFaceView(
 facelets:PyraminxFacelets,
 face:Int,
 modifier:Modifier=Modifier,
 selected:Int?=null,
 palette:Map<PyraminxColor,CubeColor> = mapOf(PyraminxColor.GREEN to CubeColor.GREEN,PyraminxColor.RED to CubeColor.RED,PyraminxColor.BLUE to CubeColor.BLUE,PyraminxColor.YELLOW to CubeColor.YELLOW),
 showInitials:Boolean=false,
 onStickerClick:((Int)->Unit)?=null,
) {
 val preferences=LocalAppPreferences.current
 val selectionColor=MaterialTheme.colorScheme.primary
 val order=listOf(listOf(0),listOf(8,1,2),listOf(6,7,5,4,3))
 Canvas(modifier.pointerInput(face,onStickerClick) {
  if(onStickerClick!=null) detectTapGestures { point ->
   val nearest=triangleCenters(size.width.toFloat(),size.height.toFloat(),order).minBy { (_,center)->(center-point).getDistance() }
   onStickerClick(face*9+nearest.first)
  }
 }) {
  val centers=triangleCenters(size.width,size.height,order)
  val edge=size.width/3f;val triangleHeight=size.height/3f
  centers.forEach { (sticker,center) ->
   val row=order.indexOfFirst { sticker in it };val position=order[row].indexOf(sticker);val up=position%2==0
   val half=edge*.49f;val h=triangleHeight*.49f
   val path=Path().apply {
    if(up) { moveTo(center.x,center.y-h);lineTo(center.x-half,center.y+h);lineTo(center.x+half,center.y+h) }
    else { moveTo(center.x-half,center.y-h);lineTo(center.x+half,center.y-h);lineTo(center.x,center.y+h) }
    close()
   }
   val color=palette.getValue(facelets.stickers[face*9+sticker])
   drawPath(path,Color(preferences.color(color)))
   drawPath(path,if(selected==face*9+sticker) selectionColor else Color(0xFF071116),style=Stroke(if(selected==face*9+sticker) 4f else 2f))
   if(showInitials) {
    val paint=android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { this.color=preferences.ink(color);textSize=min(edge,triangleHeight)*.28f;textAlign=android.graphics.Paint.Align.CENTER;typeface=android.graphics.Typeface.DEFAULT_BOLD }
    drawContext.canvas.nativeCanvas.drawText(color.initial,center.x,center.y-(paint.ascent()+paint.descent())/2,paint)
   }
  }
 }
}

private fun triangleCenters(width:Float,height:Float,order:List<List<Int>>):List<Pair<Int,Offset>> = order.flatMapIndexed { row,stickers ->
 val y=height*(row+.62f)/3f
 stickers.mapIndexed { position,sticker ->
  val spacing=width/(stickers.size+1);sticker to Offset(spacing*(position+1),y)
 }
}
