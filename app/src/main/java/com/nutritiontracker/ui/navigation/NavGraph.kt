package com.nutritiontracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Today
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Camera : Screen("camera", "Scan", Icons.Default.CameraAlt)
    data object DailyTracker : Screen("daily", "Today", Icons.Default.Today)
    data object WeeklyTracker : Screen("weekly", "Week", Icons.Default.DateRange)
}

val bottomNavItems = listOf(
    Screen.Camera,
    Screen.DailyTracker,
    Screen.WeeklyTracker
)
