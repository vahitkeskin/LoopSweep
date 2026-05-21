package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class CleanRoomUseCase(private val repository: VacuumRepository) {
    suspend operator fun invoke(host: String, token: String, roomId: Int, repeats: Int): Result<String> {
        return repository.cleanRoom(host, token, roomId, repeats)
    }
}
