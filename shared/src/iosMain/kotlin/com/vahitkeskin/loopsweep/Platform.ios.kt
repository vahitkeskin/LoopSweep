package com.vahitkeskin.loopsweep

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getEpochSeconds(): Long = platform.posix.time(null)

@androidx.compose.runtime.Composable
actual fun SystemBarsVisibility(visible: Boolean) {
    // No-op on iOS
}