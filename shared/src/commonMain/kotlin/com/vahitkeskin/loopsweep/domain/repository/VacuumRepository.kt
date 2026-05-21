package com.vahitkeskin.loopsweep.domain.repository

import com.vahitkeskin.loopsweep.domain.model.VacuumProperties

interface VacuumRepository {
    suspend fun cleanRoom(host: String, token: String, roomId: Int, repeats: Int): Result<String>
    suspend fun getProperties(host: String, token: String): Result<VacuumProperties>
}
