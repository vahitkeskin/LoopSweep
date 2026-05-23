package com.vahitkeskin.loopsweep

import platform.UIKit.UIDevice
import platform.Foundation.NSNotificationCenter

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual fun getEpochSeconds(): Long = platform.posix.time(null)

@androidx.compose.runtime.Composable
actual fun SystemBarsVisibility(visible: Boolean) {
    androidx.compose.runtime.DisposableEffect(visible) {
        val notificationName = "SystemBarsVisibilityNotification"
        NSNotificationCenter.defaultCenter.postNotificationName(
            aName = notificationName,
            `object` = null,
            userInfo = mapOf("visible" to visible)
        )
        onDispose {
            if (!visible) {
                NSNotificationCenter.defaultCenter.postNotificationName(
                    aName = notificationName,
                    `object` = null,
                    userInfo = mapOf("visible" to true)
                )
            }
        }
    }
}