package com.cubeguide.ui

import androidx.compose.foundation.Canvas
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class FeatureIcon { HOME,SCAN,CUBE,TIMER,LEARN,STATS,PUZZLES,SETTINGS }
@androidx.compose.runtime.Composable internal fun FeatureGlyph(icon: FeatureIcon,modifier: Modifier=Modifier) {
 val color=androidx.compose.material3.MaterialTheme.colorScheme.primary
 Canvas(modifier) {
  val w=size.width;val h=size.height;val stroke=Stroke(2.4.dp.toPx())
  when(icon) {
   FeatureIcon.HOME -> { val p=androidx.compose.ui.graphics.Path().apply { moveTo(w*.14f,h*.48f);lineTo(w*.5f,h*.16f);lineTo(w*.86f,h*.48f);lineTo(w*.76f,h*.48f);lineTo(w*.76f,h*.82f);lineTo(w*.24f,h*.82f);lineTo(w*.24f,h*.48f);close() };drawPath(p,color,style=stroke);drawLine(color,Offset(w*.44f,h*.82f),Offset(w*.44f,h*.6f),stroke.width);drawLine(color,Offset(w*.44f,h*.6f),Offset(w*.6f,h*.6f),stroke.width);drawLine(color,Offset(w*.6f,h*.6f),Offset(w*.6f,h*.82f),stroke.width) }
   FeatureIcon.SCAN -> { drawRoundRect(color,Offset(w*.12f,h*.2f),androidx.compose.ui.geometry.Size(w*.76f,h*.62f),style=stroke);drawCircle(color,w*.16f,Offset(w*.5f,h*.51f),style=stroke);drawLine(color,Offset(w*.3f,h*.2f),Offset(w*.38f,h*.1f),stroke.width);drawLine(color,Offset(w*.38f,h*.1f),Offset(w*.62f,h*.1f),stroke.width) }
   FeatureIcon.CUBE,FeatureIcon.PUZZLES -> { drawRect(color,Offset(w*.2f,h*.2f),androidx.compose.ui.geometry.Size(w*.6f,h*.6f),style=stroke);for(i in 1..2) { drawLine(color,Offset(w*(.2f+i*.2f),h*.2f),Offset(w*(.2f+i*.2f),h*.8f),stroke.width);drawLine(color,Offset(w*.2f,h*(.2f+i*.2f)),Offset(w*.8f,h*(.2f+i*.2f)),stroke.width) } }
   FeatureIcon.TIMER -> { drawCircle(color,w*.32f,Offset(w*.5f,h*.55f),style=stroke);drawLine(color,Offset(w*.5f,h*.55f),Offset(w*.5f,h*.34f),stroke.width);drawLine(color,Offset(w*.5f,h*.55f),Offset(w*.67f,h*.62f),stroke.width);drawLine(color,Offset(w*.4f,h*.12f),Offset(w*.6f,h*.12f),stroke.width) }
   FeatureIcon.LEARN -> { val p=androidx.compose.ui.graphics.Path().apply { moveTo(w*.16f,h*.3f);lineTo(w*.5f,h*.14f);lineTo(w*.84f,h*.3f);lineTo(w*.5f,h*.47f);close() };drawPath(p,color,style=stroke);drawLine(color,Offset(w*.28f,h*.38f),Offset(w*.28f,h*.7f),stroke.width);drawLine(color,Offset(w*.72f,h*.38f),Offset(w*.72f,h*.7f),stroke.width);drawLine(color,Offset(w*.28f,h*.7f),Offset(w*.72f,h*.7f),stroke.width) }
   FeatureIcon.STATS -> { drawRoundRect(color,Offset(w*.15f,h*.62f),androidx.compose.ui.geometry.Size(w*.16f,h*.22f),style=stroke);drawRoundRect(color,Offset(w*.42f,h*.4f),androidx.compose.ui.geometry.Size(w*.16f,h*.44f),style=stroke);drawRoundRect(color,Offset(w*.69f,h*.18f),androidx.compose.ui.geometry.Size(w*.16f,h*.66f),style=stroke) }
   FeatureIcon.SETTINGS -> { drawCircle(color,w*.3f,Offset(w*.5f,h*.5f),style=stroke);drawCircle(color,w*.1f,Offset(w*.5f,h*.5f),style=stroke) }
  }
 }
}
