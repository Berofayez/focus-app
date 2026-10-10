package com.focusapp.ui.session

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.focusapp.data.Goal
import com.focusapp.data.SessionOutcome
import com.focusapp.service.BlockerAccessibilityService
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionScreen(modifier: Modifier = Modifier, viewModel: SessionViewModel = viewModel()) {
    val session by viewModel.currentLockedSession.collectAsState(initial = null)

    val context = LocalContext.current
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {}
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    Box(modifier = modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        if (session == null) {
            val accessibilityEnabled = rememberAccessibilityServiceEnabled()
            val goals by viewModel.goals.collectAsState(initial = emptyList())
            Column {
                if (!accessibilityEnabled) {
                    AccessibilityPermissionPrompt()
                }
                StartSessionForm(goals = goals, onStart = viewModel::startSession)
            }
        } else {
            val startedAt = session!!.startedAt!!
            val lockedUntil = viewModel.lockManager.lockedUntil(startedAt, session!!.plannedMinutes)

            var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
            LaunchedEffect(session!!.id) {
                while (true) {
                    now = System.currentTimeMillis()
                    delay(1000)
                }
            }

            val pendingExitRequestedAt = session!!.emergencyExitRequestedAt
            when {
                pendingExitRequestedAt != null -> {
                    val exitWaitUntil = viewModel.emergencyPassPolicy.waitUntil(pendingExitRequestedAt)
                    val remaining = exitWaitUntil - now
                    if (remaining > 0) {
                        EmergencyExitWaiting(
                            reason = session!!.emergencyExitReason.orEmpty(),
                            remainingMillis = remaining,
                            onCancel = { viewModel.cancelEmergencyExit(session!!) },
                        )
                    } else {
                        LaunchedEffect(session!!.id) { viewModel.consumeEmergencyExit(session!!) }
                        Text("Ending session...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
                viewModel.lockManager.isLocked(lockedUntil) -> {
                    val passesUsed by viewModel.emergencyPassesUsedThisWeek.collectAsState(initial = 0)
                    LockedCountdown(
                        title = session!!.title,
                        remainingMillis = lockedUntil - now,
                        canRequestEmergencyExit = viewModel.emergencyPassPolicy.canUsePass(passesUsed),
                        onRequestEmergencyExit = { reason -> viewModel.requestEmergencyExit(session!!, reason) },
                    )
                }
                else -> EndOfSessionPrompt(title = session!!.title, onResult = viewModel::completeSession)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StartSessionForm(
    goals: List<Goal>,
    onStart: (goalId: Long, title: String, firstStep: String, plannedMinutes: Int) -> Unit,
) {
    if (goals.isEmpty()) {
        Text("Add a goal first, on the Goals tab.", style = MaterialTheme.typography.bodyLarge)
        return
    }

    var selectedGoal by remember { mutableStateOf(goals.first()) }
    var expanded by remember { mutableStateOf(false) }
    var title by remember { mutableStateOf("") }
    var firstStep by remember { mutableStateOf("") }
    var minutesText by remember { mutableStateOf("25") }

    val plannedMinutes = minutesText.toIntOrNull()
    val canStart = title.isNotBlank() && plannedMinutes != null && plannedMinutes > 0

    Column(modifier = Modifier.fillMaxWidth()) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = selectedGoal.title,
                onValueChange = {},
                readOnly = true,
                label = { Text("Goal") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                goals.forEach { goal ->
                    DropdownMenuItem(
                        text = { Text(goal.title) },
                        onClick = {
                            selectedGoal = goal
                            expanded = false
                        },
                    )
                }
            }
        }

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Session title") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        OutlinedTextField(
            value = firstStep,
            onValueChange = { firstStep = it },
            label = { Text("First step") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        OutlinedTextField(
            value = minutesText,
            onValueChange = { minutesText = it },
            label = { Text("Minutes") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        Button(
            onClick = { onStart(selectedGoal.id, title, firstStep, plannedMinutes ?: 0) },
            enabled = canStart,
            modifier = Modifier.padding(top = 16.dp),
        ) {
            Text("Start - cannot be cancelled once locked")
        }
    }
}

@Composable
private fun LockedCountdown(
    title: String,
    remainingMillis: Long,
    canRequestEmergencyExit: Boolean,
    onRequestEmergencyExit: (reason: String) -> Unit,
) {
    val totalSeconds = remainingMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    var showEmergencyForm by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            "%d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "Locked - no cancelling",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        if (!showEmergencyForm) {
            if (canRequestEmergencyExit) {
                Button(onClick = { showEmergencyForm = true }, modifier = Modifier.padding(top = 32.dp)) {
                    Text("Emergency exit")
                }
            } else {
                Text(
                    "No emergency passes left this week",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 32.dp),
                )
            }
        } else {
            OutlinedTextField(
                value = reason,
                onValueChange = { reason = it },
                label = { Text("Reason") },
                modifier = Modifier.padding(top = 24.dp),
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 12.dp),
            ) {
                Button(
                    onClick = { onRequestEmergencyExit(reason) },
                    enabled = reason.isNotBlank(),
                ) {
                    Text("Start 15-minute wait")
                }
                Button(onClick = { showEmergencyForm = false }) {
                    Text("Never mind, stay locked")
                }
            }
        }
    }
}

@Composable
private fun EmergencyExitWaiting(reason: String, remainingMillis: Long, onCancel: () -> Unit) {
    val totalSeconds = remainingMillis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Emergency exit requested", style = MaterialTheme.typography.titleLarge)
        Text("Reason: $reason", style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
        Text(
            "%d:%02d".format(minutes, seconds),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            "The session ends when this reaches zero",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )
        Button(onClick = onCancel, modifier = Modifier.padding(top = 24.dp)) {
            Text("Cancel, go back to the session")
        }
    }
}

@Composable
private fun rememberAccessibilityServiceEnabled(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var enabled by remember { mutableStateOf(BlockerAccessibilityService.isEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                enabled = BlockerAccessibilityService.isEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
    return enabled
}

@Composable
private fun AccessibilityPermissionPrompt() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
        Text(
            "To block distracting apps during a locked session, turn on the " +
                "Focus App accessibility service.",
            style = MaterialTheme.typography.bodyLarge,
        )
        Button(
            onClick = { context.startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) },
            modifier = Modifier.padding(top = 12.dp),
        ) {
            Text("Open Accessibility Settings")
        }
    }
}

@Composable
private fun EndOfSessionPrompt(title: String, onResult: (SessionOutcome) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(
            "Time's up. How did it go?",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { onResult(SessionOutcome.DONE) }) { Text("Done") }
            Button(onClick = { onResult(SessionOutcome.PARTLY) }) { Text("Partly") }
            Button(onClick = { onResult(SessionOutcome.STUCK) }) { Text("Stuck") }
        }
    }
}
