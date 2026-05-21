package com.vahitkeskin.loopsweep.data.repository

import com.vahitkeskin.loopsweep.data.network.XiaomiCloudClient
import com.vahitkeskin.loopsweep.domain.model.XiaomiDevice
import com.vahitkeskin.loopsweep.domain.model.XiaomiSession
import com.vahitkeskin.loopsweep.domain.repository.XiaomiCloudRepository

class XiaomiCloudRepositoryImpl(
    private val client: XiaomiCloudClient
) : XiaomiCloudRepository {

    override suspend fun login(username: String, passwordHashHex: String): Result<XiaomiSession> {
        return client.login(username, passwordHashHex)
    }

    override suspend fun fetchDevices(session: XiaomiSession, region: String): Result<List<XiaomiDevice>> {
        return client.fetchDevices(
            userId = session.userId,
            serviceToken = session.serviceToken,
            ssecurity = session.ssecurity,
            region = region
        )
    }

    override suspend fun fetchDevicesWithTokens(
        userId: String,
        serviceToken: String,
        ssecurity: String,
        region: String
    ): Result<List<XiaomiDevice>> {
        return client.fetchDevices(
            userId = userId,
            serviceToken = serviceToken,
            ssecurity = ssecurity,
            region = region
        )
    }
}
