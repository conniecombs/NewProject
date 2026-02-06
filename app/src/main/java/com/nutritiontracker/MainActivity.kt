package com.nutritiontracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nutritiontracker.ui.navigation.Screen
import com.nutritiontracker.ui.navigation.bottomNavItems
import com.nutritiontracker.ui.screens.CameraScreen
import com.nutritiontracker.ui.screens.DailyTrackerScreen
import com.nutritiontracker.ui.screens.WeeklyTrackerScreen
import com.nutritiontracker.ui.theme.NutritionTrackerTheme
import com.nutritiontracker.viewmodel.CameraViewModel
import com.nutritiontracker.viewmodel.TrackerViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutritionTrackerTheme {
                NutritionTrackerApp()
            }
        }
    }
}

@Composable
fun NutritionTrackerApp() {
    val navController = rememberNavController()
    val cameraViewModel: CameraViewModel = viewModel()
    val trackerViewModel: TrackerViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            NavHost(
                navController = navController,
                startDestination = Screen.Camera.route
            ) {
                composable(Screen.Camera.route) {
                    CameraScreen(
                        viewModel = cameraViewModel,
                        onSaved = {
                            trackerViewModel.refresh()
                        }
                    )
                }
                composable(Screen.DailyTracker.route) {
                    DailyTrackerScreen(viewModel = trackerViewModel)
                }
                composable(Screen.WeeklyTracker.route) {
                    WeeklyTrackerScreen(viewModel = trackerViewModel)
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
