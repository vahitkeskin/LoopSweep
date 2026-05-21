package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class GetRoomsUseCase(private val repository: VacuumRepository) {
    suspend operator fun invoke(host: String, token: String): Result<List<Pair<Long, String>>> {
        return repository.fetchRooms(host, token)
    }
}
