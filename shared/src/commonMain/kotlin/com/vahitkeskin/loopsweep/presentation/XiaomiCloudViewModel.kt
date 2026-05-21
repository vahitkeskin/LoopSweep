package com.vahitkeskin.loopsweep.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.loopsweep.domain.model.XiaomiDevice
import com.vahitkeskin.loopsweep.domain.usecase.GetXiaomiDevicesUseCase
import com.vahitkeskin.loopsweep.domain.usecase.LoginXiaomiCloudUseCase
import com.vahitkeskin.loopsweep.utils.MD5
import com.vahitkeskin.loopsweep.utils.toHexString
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class XiaomiCloudViewModel(
    private val loginXiaomiCloudUseCase: LoginXiaomiCloudUseCase,
    private val getXiaomiDevicesUseCase: GetXiaomiDevicesUseCase
) : ViewModel() {

    val username = MutableStateFlow("")
    val password = MutableStateFlow("")
    val region = MutableStateFlow("cn") // Default is China

    val manualUserId = MutableStateFlow("")
    val manualServiceToken = MutableStateFlow("")
    val manualSsecurity = MutableStateFlow("")

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _devices = MutableStateFlow<List<XiaomiDevice>>(emptyList())
    val devices: StateFlow<List<XiaomiDevice>> = _devices

    private val _loginStatus = MutableStateFlow<String?>(null)
    val loginStatus: StateFlow<String?> = _loginStatus

    val regionsList = listOf(
        RegionInfo("cn", "Çin (Mainland China)"),
        RegionInfo("de", "Avrupa (Germany/Europe)"),
        RegionInfo("us", "Amerika (United States)"),
        RegionInfo("sg", "Singapur (Singapore)"),
        RegionInfo("ru", "Rusya (Russia)"),
        RegionInfo("in", "Hindistan (India)")
    )

    fun loginAndFetchDevices() {
        if (username.value.isBlank() || password.value.isBlank()) {
            _error.value = "E-posta/Telefon ve şifre boş bırakılamaz."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _devices.value = emptyList()
            _loginStatus.value = "Xiaomi hesabına giriş yapılıyor..."

            // Calculate MD5 of password as hex string
            val passHash = MD5.hash(password.value.encodeToByteArray()).toHexString().uppercase()

            val loginResult = loginXiaomiCloudUseCase(username.value, passHash)
            loginResult.fold(
                onSuccess = { session ->
                    _loginStatus.value = "Giriş başarılı! Cihaz listesi çekiliyor..."
                    // Automatically pre-fill manual tokens in case user wants to copy them
                    manualUserId.value = session.userId
                    manualServiceToken.value = session.serviceToken
                    manualSsecurity.value = session.ssecurity

                    val devicesResult = getXiaomiDevicesUseCase.executeWithSession(session, region.value)
                    devicesResult.fold(
                        onSuccess = { deviceList ->
                            _devices.value = deviceList
                            _isLoading.value = false
                            if (deviceList.isEmpty()) {
                                _loginStatus.value = "Giriş başarılı ancak bu bölgede (${region.value.uppercase()}) cihaz bulunamadı."
                            } else {
                                _loginStatus.value = "Başarılı! ${deviceList.size} cihaz bulundu."
                            }
                        },
                        onFailure = { err ->
                            _error.value = "Cihazlar yüklenemedi: ${err.message}"
                            _loginStatus.value = null
                            _isLoading.value = false
                        }
                    )
                },
                onFailure = { err ->
                    _error.value = err.message ?: "Bilinmeyen giriş hatası."
                    _loginStatus.value = null
                    _isLoading.value = false
                }
            )
        }
    }

    fun fetchDevicesWithManualTokens() {
        if (manualUserId.value.isBlank() || manualServiceToken.value.isBlank() || manualSsecurity.value.isBlank()) {
            _error.value = "UserId, ServiceToken ve Ssecurity alanları boş bırakılamaz."
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            _devices.value = emptyList()
            _loginStatus.value = "Manuel tokenlar ile cihaz listesi çekiliyor..."

            val devicesResult = getXiaomiDevicesUseCase.executeWithTokens(
                userId = manualUserId.value.trim(),
                serviceToken = manualServiceToken.value.trim(),
                ssecurity = manualSsecurity.value.trim(),
                region = region.value
            )

            devicesResult.fold(
                onSuccess = { deviceList ->
                    _devices.value = deviceList
                    _isLoading.value = false
                    if (deviceList.isEmpty()) {
                        _loginStatus.value = "Bağlantı başarılı ancak bu bölgede (${region.value.uppercase()}) cihaz bulunamadı."
                    } else {
                        _loginStatus.value = "Başarılı! ${deviceList.size} cihaz bulundu."
                    }
                },
                onFailure = { err ->
                    _error.value = "Manuel cihaz listesi çekme hatası: ${err.message}"
                    _loginStatus.value = null
                    _isLoading.value = false
                }
            )
        }
    }

    fun clear() {
        username.value = ""
        password.value = ""
        manualUserId.value = ""
        manualServiceToken.value = ""
        manualSsecurity.value = ""
        _devices.value = emptyList()
        _error.value = null
        _loginStatus.value = null
    }
}

data class RegionInfo(
    val code: String,
    val displayName: String
)
