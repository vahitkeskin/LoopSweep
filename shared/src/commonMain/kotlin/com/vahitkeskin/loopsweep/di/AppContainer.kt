package com.vahitkeskin.loopsweep.di

import com.vahitkeskin.loopsweep.data.repository.VacuumRepositoryImpl
import com.vahitkeskin.loopsweep.domain.repository.VacuumRepository
import com.vahitkeskin.loopsweep.domain.usecase.CleanRoomUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetVacuumPropertiesUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetVacuumTelemetryUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetRoomsUseCase
import com.vahitkeskin.loopsweep.data.local.createDataStore
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

class AppContainer {
    val dataStore: DataStore<Preferences> by lazy {
        createDataStore()
    }

    val vacuumRepository: VacuumRepository by lazy {
        VacuumRepositoryImpl()
    }
    
    val cleanRoomUseCase: CleanRoomUseCase by lazy {
        CleanRoomUseCase(vacuumRepository)
    }
    
    val getVacuumPropertiesUseCase: GetVacuumPropertiesUseCase by lazy {
        GetVacuumPropertiesUseCase(vacuumRepository)
    }

    val getVacuumTelemetryUseCase: GetVacuumTelemetryUseCase by lazy {
        GetVacuumTelemetryUseCase(vacuumRepository)
    }

    val getRoomsUseCase: GetRoomsUseCase by lazy {
        GetRoomsUseCase(vacuumRepository)
    }
}
