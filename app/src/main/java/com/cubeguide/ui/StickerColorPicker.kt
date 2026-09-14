package com.cubeguide.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.core.CubeColor
import com.cubeguide.core.Face

@OptIn(ExperimentalMaterial3Api::class)
@Composable internal fun StickerColorPicker(index: Int,current: CubeColor,enabled: Boolean,onSelect: (CubeColor)->Unit,onDismiss: ()->Unit) {
 val preferences=LocalAppPreferences.current
 ModalBottomSheet(onDismissRequest=onDismiss) {
  Column(Modifier.fillMaxWidth().padding(horizontal=24.dp).navigationBarsPadding()) {
   Text("Choose a color",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
   Spacer(Modifier.height(8.dp))
   Text("${Face.entries[index/9].name} face / row ${index%9/3+1}, column ${index%3+1}",style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
   Spacer(Modifier.height(20.dp))
   CubeColor.entries.chunked(3).forEach { row ->
    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
     row.forEach { color ->
      Surface(onClick={onSelect(color)},enabled=enabled,modifier=Modifier.weight(1f).heightIn(min=104.dp).semantics { selected=color==current; contentDescription="${color.label}${if(color==current) ", current color" else ""}" },shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainer,border=BorderStroke(if(color==current) 2.dp else 1.dp,if(color==current) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant)) {
       Column(Modifier.padding(12.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Box(Modifier.size(40.dp).background(Color(preferences.color(color)),RoundedCornerShape(10.dp)),contentAlignment=Alignment.Center) { Text(color.initial,color=Color(preferences.ink(color)),fontWeight=FontWeight.Bold) }
        Spacer(Modifier.height(8.dp));Text(color.label,style=MaterialTheme.typography.labelLarge)
       }
      }
     }
    }
    Spacer(Modifier.height(10.dp))
   }
   TextButton(onClick=onDismiss,modifier=Modifier.fillMaxWidth()) { Text("Cancel") }
   Spacer(Modifier.height(12.dp))
  }
 }
}
