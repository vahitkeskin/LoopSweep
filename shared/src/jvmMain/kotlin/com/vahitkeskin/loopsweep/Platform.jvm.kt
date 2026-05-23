package com.vahitkeskin.loopsweep

class JVMPlatform: Platform {
    override val name: String = "Java ${System.getProperty("java.version")}"
}

actual fun getPlatform(): Platform = JVMPlatform()

actual fun getEpochSeconds(): Long = System.currentTimeMillis() / 1000

@androidx.compose.runtime.Composable
actual fun SystemBarsVisibility(visible: Boolean) {
    // No-op on JVM
}