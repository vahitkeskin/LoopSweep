package com.vahitkeskin.loopsweep

import androidx.compose.animation.Crossfade
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.vahitkeskin.loopsweep.ui.screen.SplashScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        var showSplash by remember { mutableStateOf(true) }

        Crossfade(targetState = showSplash, label = "AppSplashTransition") { isSplash ->
            if (isSplash) {
                SplashScreen(
                    onSplashFinished = { showSplash = false }
                )
            } else {
                VacuumApp()
            }
        }
    }
}