package com.vahitkeskin.loopsweep.domain.model

data class VacuumTelemetry(
    val batteryLevel: Int?,
    val statusCode: Int?,
    val faultCode: Int?,
    val cleanTimeMinutes: Int?,
    val cleanAreaSqm: Int?,
    val suctionState: Int?, // 0: Silent, 1: Standard, 2: Medium, 3: Turbo
    val waterState: Int?,   // 0: Low, 1: Medium, 2: High
    val sideBrushLife: Int?,
    val mainBrushLife: Int?,
    val filterLife: Int?,
    val mopLife: Int?,
    val cleaningPath: String?,
    val rawJson: String? = null
)
