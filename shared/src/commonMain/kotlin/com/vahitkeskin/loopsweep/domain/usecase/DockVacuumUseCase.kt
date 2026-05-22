package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class DockVacuumUseCase(private val repository: VacuumRepository) {
    suspend operator fun invoke(host: String, token: String): Result<String> {
        return repository.dockVacuum(host, token)
    }
}
