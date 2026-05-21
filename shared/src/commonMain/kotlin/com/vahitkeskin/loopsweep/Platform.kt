package com.vahitkeskin.loopsweep

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform