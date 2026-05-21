package com.vahitkeskin.loopsweep.utils

actual object Logger {
    actual fun e(tag: String, message: String, throwable: Throwable?) {
        println("[$tag] ERROR: $message")
        throwable?.printStackTrace()
    }

    actual fun d(tag: String, message: String) {
        println("[$tag] DEBUG: $message")
    }

    actual fun i(tag: String, message: String) {
        println("[$tag] INFO: $message")
    }
}
