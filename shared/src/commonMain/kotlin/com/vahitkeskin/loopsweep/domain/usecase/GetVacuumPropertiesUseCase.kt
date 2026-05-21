package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.model.VacuumProperties
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class GetVacuumPropertiesUseCase(private val repository: VacuumRepository) {
    suspend operator fun invoke(host: String, token: String): Result<VacuumProperties> {
        return repository.getProperties(host, token)
    }
}
