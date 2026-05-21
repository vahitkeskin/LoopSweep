package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.model.XiaomiDevice
import com.vahitkeskin.loopsweep.domain.model.XiaomiSession
import com.vahitkeskin.loopsweep.domain.repository.XiaomiCloudRepository

class GetXiaomiDevicesUseCase(
    private val repository: XiaomiCloudRepository
) {
    suspend fun executeWithSession(session: XiaomiSession, region: String): Result<List<XiaomiDevice>> {
        return repository.fetchDevices(session, region)
    }

    suspend fun executeWithTokens(userId: String, serviceToken: String, ssecurity: String, region: String): Result<List<XiaomiDevice>> {
        return repository.fetchDevicesWithTokens(userId, serviceToken, ssecurity, region)
    }
}
