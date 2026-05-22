package com.vahitkeskin.loopsweep.navigation

/**
 * Sealed class representing all routing destinations in the application.
 * Using a sealed class structure ensures type safety and clean navigation references.
 */
sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Dashboard : Screen("dashboard")
    data object Cloud : Screen("cloud")
}
