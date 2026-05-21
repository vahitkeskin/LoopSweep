package com.vahitkeskin.loopsweep

import platform.UIKit.UIDevice

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun getEpochSeconds(): Long = platform.Foundation.NSDate().timeIntervalSince1970.toLong()