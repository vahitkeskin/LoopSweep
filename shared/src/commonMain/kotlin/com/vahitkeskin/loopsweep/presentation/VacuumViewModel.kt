package com.vahitkeskin.loopsweep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.loopsweep.BuildConfig
import com.vahitkeskin.loopsweep.domain.usecase.CleanRoomUseCase
import com.vahitkeskin.loopsweep.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VacuumViewModel(private val cleanRoomUseCase: CleanRoomUseCase) : ViewModel() {
    
    val ipAddress = MutableStateFlow(BuildConfig.VACUUM_IP)
    val token = MutableStateFlow(BuildConfig.VACUUM_TOKEN)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _statusMessage = MutableStateFlow("Hazır. Başlamak için bir oda kartına dokunun.")
    val statusMessage: StateFlow<String> = _statusMessage
    
    fun cleanRoom(roomId: Int, repeats: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Süpürgeye bağlanılıyor..."
            
            val result = cleanRoomUseCase(
                host = ipAddress.value,
                token = token.value,
                roomId = roomId,
                repeats = repeats
            )
            
            result.fold(
                onSuccess = { response ->
                    _statusMessage.value = "Komut başarıyla gönderildi: Oda $roomId, Döngü: $repeats"
                },
                onFailure = { error ->
                    val msg = "Hata: ${error.message ?: "Bilinmeyen Bağlantı Hatası"}"
                    _statusMessage.value = msg
                    Logger.e("VacuumViewModel", msg, error)
                }
            )
            _isLoading.value = false
        }
    }
}
