package com.vahitkeskin.loopsweep

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

expect fun getEpochSeconds(): Long

@androidx.compose.runtime.Composable
expect fun SystemBarsVisibility(visible: Boolean)