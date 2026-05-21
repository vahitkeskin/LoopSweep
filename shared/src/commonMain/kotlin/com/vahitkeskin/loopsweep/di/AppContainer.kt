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

    val xiaomiCloudClient: com.vahitkeskin.loopsweep.data.network.XiaomiCloudClient by lazy {
        com.vahitkeskin.loopsweep.data.network.XiaomiCloudClient()
    }

    val xiaomiCloudRepository: com.vahitkeskin.loopsweep.domain.repository.XiaomiCloudRepository by lazy {
        com.vahitkeskin.loopsweep.data.repository.XiaomiCloudRepositoryImpl(xiaomiCloudClient)
    }

    val loginXiaomiCloudUseCase: com.vahitkeskin.loopsweep.domain.usecase.LoginXiaomiCloudUseCase by lazy {
        com.vahitkeskin.loopsweep.domain.usecase.LoginXiaomiCloudUseCase(xiaomiCloudRepository)
    }

    val getXiaomiDevicesUseCase: com.vahitkeskin.loopsweep.domain.usecase.GetXiaomiDevicesUseCase by lazy {
        com.vahitkeskin.loopsweep.domain.usecase.GetXiaomiDevicesUseCase(xiaomiCloudRepository)
    }
}
