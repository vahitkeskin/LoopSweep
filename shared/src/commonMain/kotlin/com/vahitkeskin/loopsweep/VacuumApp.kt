package com.vahitkeskin.loopsweep

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.vahitkeskin.loopsweep.di.AppContainer
import com.vahitkeskin.loopsweep.presentation.VacuumViewModel
import com.vahitkeskin.loopsweep.ui.screen.VacuumScreen

@Composable
fun VacuumApp() {
    // 1. Initialize our Manual DI Container
    val appContainer = remember { AppContainer() }
    
    // 2. Resolve our ViewModel with UseCase injection
    val viewModel = remember { 
        VacuumViewModel(appContainer.cleanRoomUseCase)
    }
    
    // 3. Render the Main Screen
    VacuumScreen(viewModel = viewModel)
}
