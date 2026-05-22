package com.vahitkeskin.loopsweep

import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.loopsweep.ui.screen.SplashScreen
import com.vahitkeskin.loopsweep.navigation.Screen

@Composable
@Preview
fun App() {
    MaterialTheme {
        val navController = rememberNavController()

        NavHost(
            navController = navController, 
            startDestination = Screen.Splash.route
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    onSplashFinished = {
                        navController.navigate(Screen.Vacuum.route) {
                            // Clear splash screen from back stack to prevent users from navigating back to it
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Vacuum.route) {
                VacuumApp()
            }
        }
    }
}