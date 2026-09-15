package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.cubeguide.rendering.CubeView

@Composable
internal fun AnalyzingScreen(vm: CubeViewModel) {
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        Spacer(Modifier.height(12.dp))
        Text("Building your solution", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("This usually takes a moment.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        CubeView(vm.cube, Modifier.fillMaxWidth().weight(1f).heightIn(min = 220.dp, max = 340.dp))
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceContainer,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
                AnalysisRow("✓", "Read all 54 stickers")
                AnalysisRow("✓", "Validated cube colors")
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
private fun AnalysisRow(mark: String, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(mark, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.width(12.dp))
        Text(label)
    }
}
