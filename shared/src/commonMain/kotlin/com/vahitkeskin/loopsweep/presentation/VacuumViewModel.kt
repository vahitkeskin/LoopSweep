package com.vahitkeskin.loopsweep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.loopsweep.BuildConfig
import com.vahitkeskin.loopsweep.domain.usecase.CleanRoomUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetVacuumPropertiesUseCase
import com.vahitkeskin.loopsweep.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class VacuumViewModel(
    private val cleanRoomUseCase: CleanRoomUseCase,
    private val getVacuumPropertiesUseCase: GetVacuumPropertiesUseCase
) : ViewModel() {
    
    val ipAddress = MutableStateFlow(BuildConfig.VACUUM_IP)
    val token = MutableStateFlow(BuildConfig.VACUUM_TOKEN)
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _statusMessage = MutableStateFlow("Hazır. Başlamak için bir oda kartına dokunun.")
    val statusMessage: StateFlow<String> = _statusMessage

    private val _batteryLevel = MutableStateFlow<Int?>(null)
    val batteryLevel: StateFlow<Int?> = _batteryLevel

    private val _deviceStatusText = MutableStateFlow("Bağlanıyor...")
    val deviceStatusText: StateFlow<String> = _deviceStatusText

    private val _isCharging = MutableStateFlow(false)
    val isCharging: StateFlow<Boolean> = _isCharging

    init {
        startStatusPolling()
    }

    private fun startStatusPolling() {
        viewModelScope.launch {
            while (true) {
                if (ipAddress.value.isNotBlank() && token.value.isNotBlank()) {
                    val result = getVacuumPropertiesUseCase(
                        host = ipAddress.value,
                        token = token.value
                    )
                    result.fold(
                        onSuccess = { props ->
                            _batteryLevel.value = props.batteryLevel
                            _isCharging.value = props.statusCode == 4
                            _deviceStatusText.value = mapStatusText(props.statusCode)
                        },
                        onFailure = { error ->
                            Logger.e("VacuumViewModel", "Failed to fetch vacuum status: ${error.message}", error)
                            _deviceStatusText.value = "Bağlantı Yok"
                        }
                    )
                }
                kotlinx.coroutines.delay(10000) // Poll every 10 seconds
            }
        }
    }

    private fun mapStatusText(statusCode: Int?): String {
        return when (statusCode) {
            0 -> "Uyku Modu"
            1 -> "Beklemede"
            2 -> "Duraklatıldı"
            3 -> "Şarj İstasyonuna Gidiyor"
            4 -> "Şarj Ediliyor"
            5 -> "Süpürüyor"
            6 -> "Süpürüyor & Paspas Yapıyor"
            7 -> "Paspas Yapıyor"
            8 -> "Güncelleniyor"
            null -> "Bilinmeyen Durum"
            else -> "Bilinmeyen Durum (Kod: $statusCode)"
        }
    }
    
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
