package com.vahitkeskin.loopsweep.data.repository

import com.vahitkeskin.loopsweep.data.network.VacuumClient
import com.vahitkeskin.loopsweep.domain.model.VacuumProperties
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class VacuumRepositoryImpl : VacuumRepository {
    override suspend fun cleanRoom(host: String, token: String, roomId: Int, repeats: Int): Result<String> {
        return VacuumClient.sendCommand(host, token, roomId, repeats)
    }

    override suspend fun getProperties(host: String, token: String): Result<VacuumProperties> {
        return VacuumClient.getProperties(host, token)
    }
}
