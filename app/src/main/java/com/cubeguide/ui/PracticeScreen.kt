package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
internal fun PracticeScreen(vm: CubeViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Text("Train at your pace", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Play a virtual cube or time a focused 3×3 solve.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(22.dp))
        PracticeCard("Virtual cube", "Free play, challenges and guided solving", FeatureIcon.CUBE) { vm.open(Screen.VIRTUAL) }
        Spacer(Modifier.height(12.dp))
        PracticeCard("Cube timer", "3×3 scrambles and personal records", FeatureIcon.TIMER) { vm.open(Screen.TIMER) }
        Spacer(Modifier.height(20.dp))
    }
}

@Composable
private fun PracticeCard(title: String, subtitle: String, icon: FeatureIcon, onClick: () -> Unit) {
    val feedback = rememberTouchFeedback()
    Card(
        onClick = { feedback(); onClick() },
        modifier = Modifier.fillMaxWidth().height(104.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Row(Modifier.fillMaxSize().padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                Box(Modifier.size(50.dp), contentAlignment = Alignment.Center) { FeatureGlyph(icon, Modifier.size(27.dp)) }
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            }
            Icon(Icons.Rounded.ChevronRight,"Open",tint=MaterialTheme.colorScheme.primary)
        }
    }
}
