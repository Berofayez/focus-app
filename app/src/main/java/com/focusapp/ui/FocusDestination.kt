package com.focusapp.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

enum class FocusDestination(val route: String, val label: String, val icon: ImageVector) {
    Today(route = "today", label = "Today", icon = Icons.Filled.Home),
    Goals(route = "goals", label = "Goals", icon = Icons.Filled.Star),
    Session(route = "session", label = "Session", icon = Icons.Filled.CheckCircle),
}
