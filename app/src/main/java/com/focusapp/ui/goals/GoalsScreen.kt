package com.focusapp.ui.goals

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusapp.data.Goal

@Composable
fun GoalsScreen(modifier: Modifier = Modifier, viewModel: GoalsViewModel = viewModel()) {
    val goals by viewModel.goals.collectAsState(initial = emptyList())
    var newGoalTitle by remember { mutableStateOf("") }

    Column(modifier = modifier.fillMaxSize().padding(24.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = newGoalTitle,
                onValueChange = { newGoalTitle = it },
                label = { Text("New goal") },
                modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    viewModel.addGoal(newGoalTitle)
                    newGoalTitle = ""
                },
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text("Add")
            }
        }

        if (goals.isEmpty()) {
            Text(
                "No goals yet",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            LazyColumn(modifier = Modifier.padding(top = 16.dp)) {
                items(goals, key = { it.id }) { goal: Goal ->
                    Text(
                        goal.title,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 12.dp),
                    )
                }
            }
        }
    }
}
