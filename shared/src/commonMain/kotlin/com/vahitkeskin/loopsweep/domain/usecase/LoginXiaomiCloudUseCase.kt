package com.vahitkeskin.loopsweep.domain.usecase

import com.vahitkeskin.loopsweep.domain.model.XiaomiSession
import com.vahitkeskin.loopsweep.domain.repository.XiaomiCloudRepository

class LoginXiaomiCloudUseCase(
    private val repository: XiaomiCloudRepository
) {
    suspend operator fun invoke(username: String, passwordHashHex: String): Result<XiaomiSession> {
        return repository.login(username, passwordHashHex)
    }
}
