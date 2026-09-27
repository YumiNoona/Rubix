package com.cubeguide.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun PracticeScreen(vm: CubeViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.height(8.dp))
        RubixActionCard(
            title = "Virtual cube",
            icon = Icons.Rounded.ViewInAr,
            accent = MaterialTheme.colorScheme.primary,
            onClick = { vm.open(Screen.VIRTUAL) },
        )
        Spacer(Modifier.height(RubixTokens.itemGap))
        RubixActionCard(
            title = "Cube timer",
            icon = Icons.Rounded.Timer,
            accent = MaterialTheme.colorScheme.tertiary,
            onClick = { vm.open(Screen.TIMER) },
        )
        Spacer(Modifier.height(12.dp))
    }
}
