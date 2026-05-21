package com.vahitkeskin.loopsweep.di

import com.vahitkeskin.loopsweep.data.repository.VacuumRepositoryImpl
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository
import com.vahitkeskin.loopsweep.domain.usecase.CleanRoomUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetVacuumPropertiesUseCase

class AppContainer {
    val vacuumRepository: VacuumRepository by lazy {
        VacuumRepositoryImpl()
    }
    
    val cleanRoomUseCase: CleanRoomUseCase by lazy {
        CleanRoomUseCase(vacuumRepository)
    }
    
    val getVacuumPropertiesUseCase: GetVacuumPropertiesUseCase by lazy {
        GetVacuumPropertiesUseCase(vacuumRepository)
    }
}
