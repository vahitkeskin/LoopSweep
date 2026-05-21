package com.vahitkeskin.loopsweep.domain.repository

import com.vahitkeskin.loopsweep.domain.model.VacuumProperties
import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry

interface VacuumRepository {
    suspend fun cleanRoom(host: String, token: String, roomId: Long, repeats: Int): Result<String>
    suspend fun getProperties(host: String, token: String): Result<VacuumProperties>
    suspend fun getTelemetry(host: String, token: String): Result<VacuumTelemetry>
    suspend fun fetchRooms(host: String, token: String): Result<List<Pair<Long, String>>>
}
