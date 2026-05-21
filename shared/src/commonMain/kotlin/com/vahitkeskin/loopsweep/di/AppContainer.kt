package com.vahitkeskin.loopsweep.di

import com.vahitkeskin.loopsweep.data.repository.VacuumRepositoryImpl
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository
import com.vahitkeskin.loopsweep.domain.usecase.CleanRoomUseCase

class AppContainer {
    val vacuumRepository: VacuumRepository by lazy {
        VacuumRepositoryImpl()
    }
    
    val cleanRoomUseCase: CleanRoomUseCase by lazy {
        CleanRoomUseCase(vacuumRepository)
    }
}
