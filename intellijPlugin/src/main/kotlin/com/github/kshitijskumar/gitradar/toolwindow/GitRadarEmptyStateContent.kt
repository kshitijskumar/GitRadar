package com.github.kshitijskumar.gitradar.toolwindow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.jewel.ui.component.OutlinedButton
import org.jetbrains.jewel.ui.component.Text

@Composable
fun GitRadarEmptyStateContent(
    type: EmptyStateType,
    onOpenSettings: () -> Unit,
) {
    val message = when (type) {
        EmptyStateType.NOT_LOGGED_IN -> "No account configured."
        EmptyStateType.NO_REPO_DETECTED ->
            "No GitHub repository detected in this project.\nOnly HTTPS remotes are supported."
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = message, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onOpenSettings) {
            Text("Open Settings")
        }
    }
}
