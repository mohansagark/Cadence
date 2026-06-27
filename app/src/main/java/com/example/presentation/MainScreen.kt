package com.example.presentation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.InsertChart
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.presentation.calendar.CalendarScreen
import com.example.presentation.home.HomeScreen
import com.example.presentation.home.HomeViewModel
import com.example.presentation.profile.*
import com.example.presentation.stats.StatsScreen
import com.example.ui.theme.DayFlowPrimary
import com.example.ui.theme.Slate400

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "Today", Icons.Filled.Home)
    object Calendar : Screen("calendar", "Calendar", Icons.Filled.CalendarToday)
    object Stats : Screen("stats", "Stats", Icons.Filled.InsertChart)
}

@Composable
fun MainScreen(homeViewModel: HomeViewModel, settingsManager: com.example.data.local.prefs.SettingsManager, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val items = listOf(
        Screen.Home,
        Screen.Calendar,
        Screen.Stats
    )

    Scaffold(
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route
            
            if (currentRoute in items.map { it.route }) {
                NavigationBar(
                    containerColor = com.example.ui.theme.DayFlowSurfaceLight,
                ) {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            selected = currentRoute == screen.route,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DayFlowPrimary,
                                selectedTextColor = DayFlowPrimary,
                                unselectedIconColor = Slate400,
                                unselectedTextColor = Slate400,
                                indicatorColor = com.example.ui.theme.DayFlowPrimaryLight
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateTo = { route -> navController.navigate(route) },
                    onLogout = onLogout
                ) 
            }
            composable(Screen.Calendar.route) { CalendarScreen(homeViewModel = homeViewModel) }
            composable(Screen.Stats.route) { StatsScreen(homeViewModel = homeViewModel) }
            composable("profile") { 
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateTo = { route -> navController.navigate(route) }
                ) 
            }
            composable("profile_settings") { ProfileSettingsScreen(onBack = { navController.popBackStack() }) }
            composable("appearance_settings") { AppearanceScreen(onBack = { navController.popBackStack() }) }
            composable("security_settings") { SecurityScreen(onBack = { navController.popBackStack() }, settingsManager = settingsManager) }
            composable("notification_settings") { NotificationSettingsScreen(onBack = { navController.popBackStack() }) }
            composable("sync_settings") { SyncSettingsScreen(onBack = { navController.popBackStack() }, settingsManager = settingsManager) }
            composable("about") { AboutScreen(onBack = { navController.popBackStack() }) }
        }
    }
}
