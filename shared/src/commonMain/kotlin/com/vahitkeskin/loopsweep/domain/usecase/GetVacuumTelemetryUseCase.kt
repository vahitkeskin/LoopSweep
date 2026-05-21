package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository

class GetVacuumTelemetryUseCase(private val repository: VacuumRepository) {
    suspend operator fun invoke(host: String, token: String): Result<VacuumTelemetry> {
        return repository.getTelemetry(host, token)
    }
}
