package com.vahitkeskin.loopsweep.utils

import timber.log.Timber

actual object Logger {
    init {
        if (Timber.forest().isEmpty()) {
            Timber.plant(Timber.DebugTree())
        }
    }

    actual fun e(tag: String, message: String, throwable: Throwable?) {
        Timber.tag(tag).e(throwable, message)
    }

    actual fun d(tag: String, message: String) {
        Timber.tag(tag).d(message)
    }

    actual fun i(tag: String, message: String) {
        Timber.tag(tag).i(message)
    }
}
