package com.vahitkeskin.loopsweep.domain.repository

import com.vahitkeskin.loopsweep.domain.model.XiaomiDevice
import com.vahitkeskin.loopsweep.domain.model.XiaomiSession

interface XiaomiCloudRepository {
    suspend fun login(username: String, passwordHashHex: String): Result<XiaomiSession>
    suspend fun fetchDevices(session: XiaomiSession, region: String): Result<List<XiaomiDevice>>
    suspend fun fetchDevicesWithTokens(userId: String, serviceToken: String, ssecurity: String, region: String): Result<List<XiaomiDevice>>
}
