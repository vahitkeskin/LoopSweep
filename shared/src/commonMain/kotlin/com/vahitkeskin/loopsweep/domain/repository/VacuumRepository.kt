package com.vahitkeskin.loopsweep.domain.repository

interface VacuumRepository {
    suspend fun cleanRoom(host: String, token: String, roomId: Int, repeats: Int): Result<String>
}
