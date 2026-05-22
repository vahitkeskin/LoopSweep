package com.vahitkeskin.loopsweep.data.repository

import com.vahitkeskin.loopsweep.data.network.VacuumClient
import com.vahitkeskin.loopsweep.domain.model.VacuumProperties
import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class VacuumRepositoryImpl : VacuumRepository {
    override suspend fun cleanRoom(host: String, token: String, roomId: Long, repeats: Int): Result<String> {
        return VacuumClient.sendCommand(host, token, roomId, repeats)
    }

    override suspend fun getProperties(host: String, token: String): Result<VacuumProperties> {
        return VacuumClient.getProperties(host, token)
    }

    override suspend fun getTelemetry(host: String, token: String): Result<VacuumTelemetry> {
        return VacuumClient.getTelemetry(host, token)
    }

    override suspend fun fetchRooms(host: String, token: String): Result<List<Pair<Long, String>>> {
        return VacuumClient.fetchRooms(host, token)
    }

    override suspend fun stopCleaning(host: String, token: String): Result<String> {
        return VacuumClient.sendStopCommand(host, token)
    }

    override suspend fun dockVacuum(host: String, token: String): Result<String> {
        return VacuumClient.sendDockCommand(host, token)
    }
}
