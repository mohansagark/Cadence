package com.example.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.presentation.auth.AppLockScreen
import com.example.presentation.auth.AuthScreen
import com.example.presentation.auth.OnboardingScreen
import com.example.presentation.auth.SplashScreen
import com.example.presentation.home.HomeViewModel

import com.example.data.local.prefs.SettingsManager

@Composable
fun RootNavGraph(homeViewModel: HomeViewModel, settingsManager: SettingsManager) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onNavigateToHome = {
                    if (settingsManager.isAppLockEnabled) {
                        navController.navigate("applock") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                },
                onNavigateToOnboarding = {
                    navController.navigate("onboarding") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }
        
        composable("applock") {
            AppLockScreen(
                onUnlock = {
                    navController.navigate("main") {
                        popUpTo("applock") { inclusive = true }
                    }
                }
            )
        }
        
        composable("onboarding") {
            OnboardingScreen(
                onNavigateToAuth = {
                    navController.navigate("auth") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }
        
        composable("auth") {
            AuthScreen(
                onAuthSuccess = {
                    if (settingsManager.isAppLockEnabled) {
                        navController.navigate("applock") {
                            popUpTo("auth") { inclusive = true }
                        }
                    } else {
                        navController.navigate("main") {
                            popUpTo("auth") { inclusive = true }
                        }
                    }
                }
            )
        }
        
        composable("main") {
            MainScreen(
                homeViewModel = homeViewModel,
                settingsManager = settingsManager,
                onLogout = {
                    navController.navigate("auth") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}
