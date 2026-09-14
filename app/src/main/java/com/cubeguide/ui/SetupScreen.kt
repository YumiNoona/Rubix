package com.cubeguide.ui

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.cubeguide.core.*
import com.cubeguide.rendering.CubeView

@Composable internal fun HoldingSetup(vm: CubeViewModel) {
 var choose by remember { mutableStateOf(false) }
 val preview=vm.holdingPreview
 val front=preview.stickers[22]; val top=preview.stickers[4]
 Column(Modifier.fillMaxSize()) {
  Column(Modifier.weight(1f).verticalScroll(rememberScrollState()),horizontalAlignment=Alignment.CenterHorizontally) {
   Heading("BEFORE YOU START","Hold your cube like this.")
   CubeView(preview,modifier=Modifier.fillMaxWidth().height(260.dp))
   HoldingLabels(front,top)
   Spacer(Modifier.height(16.dp))
   Text("Rotate the whole cube to match the center colors. Keep this position while following the guide.",style=MaterialTheme.typography.bodyMedium,textAlign=TextAlign.Center,color=MaterialTheme.colorScheme.onSurfaceVariant)
   TextButton(onClick={choose=true}) { Text("Choose another front color") }
   if(vm.message.isNotBlank()) Text(vm.message,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(vertical=8.dp))
  }
  Primary("Cube is in position") { vm.startGuide() }
  Spacer(Modifier.height(12.dp))
 }
 if(choose) AlertDialog(onDismissRequest={choose=false},title={Text("Which center should face you?")},text={
  Column(Modifier.heightIn(max=340.dp).verticalScroll(rememberScrollState())) {
   Face.entries.forEach { face -> val color=vm.initial.stickers[face.ordinal*9+4]
    TextButton(onClick={vm.chooseStartingFace(face);choose=false},modifier=Modifier.fillMaxWidth().heightIn(min=48.dp)) {
     Box(Modifier.size(22.dp).background(Color(LocalAppPreferences.current.color(color)),RoundedCornerShape(6.dp)))
     Spacer(Modifier.width(12.dp)); Text(color.label,modifier=Modifier.weight(1f),textAlign=TextAlign.Start)
    }
   }
  }
 },confirmButton={TextButton(onClick={choose=false}) { Text("Cancel") }})
}
