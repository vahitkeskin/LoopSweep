package com.vahitkeskin.loopsweep

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getEpochSeconds(): Long = System.currentTimeMillis() / 1000

@androidx.compose.runtime.Composable
actual fun SystemBarsVisibility(visible: Boolean) {
    val view = androidx.compose.ui.platform.LocalView.current
    val context = view.context
    androidx.compose.runtime.DisposableEffect(visible) {
        val window = (context as? android.app.Activity)?.window
        if (window != null) {
            val insetsController = androidx.core.view.WindowCompat.getInsetsController(window, view)
            if (!visible) {
                insetsController.hide(androidx.core.view.WindowInsetsCompat.Type.statusBars() or androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                insetsController.systemBarsBehavior = androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                insetsController.show(androidx.core.view.WindowInsetsCompat.Type.statusBars() or androidx.core.view.WindowInsetsCompat.Type.navigationBars())
            }
        }
        onDispose {
            if (!visible) {
                window?.let { w ->
                    val controller = androidx.core.view.WindowCompat.getInsetsController(w, view)
                    controller.show(androidx.core.view.WindowInsetsCompat.Type.statusBars() or androidx.core.view.WindowInsetsCompat.Type.navigationBars())
                }
            }
        }
    }
}