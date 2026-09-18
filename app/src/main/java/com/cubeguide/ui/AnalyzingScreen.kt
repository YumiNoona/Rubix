package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.rendering.CubeView

@Composable
internal fun AnalyzingScreen(vm: CubeViewModel) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        PageIntro("Building your solution", subtitle = "Checking every move.")
        CubeView(vm.cube, Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp, max = 340.dp))
        Surface(
            shape = RubixTokens.cardShape,
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                AnalysisRow("Read all 54 stickers")
                AnalysisRow("Validated cube colors")
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(Modifier.size(19.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.width(12.dp))
                    Text("Finding a verified solution", fontWeight = FontWeight.Medium)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AnalysisRow(label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.CheckCircle,null,Modifier.size(20.dp),tint=MaterialTheme.colorScheme.tertiary)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}
