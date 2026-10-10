package com.focusapp.ui.today

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// Home screen: only "Now" and "Next" are shown, nothing else competes for attention.
@Composable
fun TodayScreen(modifier: Modifier = Modifier, viewModel: TodayViewModel = viewModel()) {
    val sessions by viewModel.activeSessions.collectAsState(initial = emptyList())
    val now = sessions.getOrNull(0)
    val next = sessions.getOrNull(1)

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Now", style = MaterialTheme.typography.titleLarge)
        Text(now?.title ?: "Nothing planned yet", style = MaterialTheme.typography.bodyLarge)

        Text("Next", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 32.dp))
        Text(next?.title ?: "Nothing planned yet", style = MaterialTheme.typography.bodyLarge)
    }
}
