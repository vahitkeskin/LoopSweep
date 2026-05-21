package com.vahitkeskin.loopsweep.domain.model

data class XiaomiDevice(
    val name: String,
    val model: String,
    val ip: String,
    val token: String,
    val did: String,
    val isOnline: Boolean
)

data class XiaomiSession(
    val userId: String,
    val serviceToken: String,
    val ssecurity: String
)
