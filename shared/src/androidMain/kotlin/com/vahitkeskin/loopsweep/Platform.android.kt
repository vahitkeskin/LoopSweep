package com.vahitkeskin.loopsweep

import android.os.Build

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun getEpochSeconds(): Long = System.currentTimeMillis() / 1000