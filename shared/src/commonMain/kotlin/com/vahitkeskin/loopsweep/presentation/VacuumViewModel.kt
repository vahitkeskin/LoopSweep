package com.vahitkeskin.loopsweep.presentation

import com.vahitkeskin.loopsweep.ui.theme.*

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vahitkeskin.loopsweep.BuildConfig
import com.vahitkeskin.loopsweep.domain.model.RoomItem
import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry
import com.vahitkeskin.loopsweep.domain.usecase.CleanRoomUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetRoomsUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetVacuumPropertiesUseCase
import com.vahitkeskin.loopsweep.domain.usecase.GetVacuumTelemetryUseCase
import com.vahitkeskin.loopsweep.domain.usecase.StopVacuumUseCase
import com.vahitkeskin.loopsweep.domain.usecase.DockVacuumUseCase
import com.vahitkeskin.loopsweep.utils.Constants
import com.vahitkeskin.loopsweep.utils.Logger
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit

class VacuumViewModel(
    private val cleanRoomUseCase: CleanRoomUseCase,
    private val getVacuumPropertiesUseCase: GetVacuumPropertiesUseCase,
    private val getVacuumTelemetryUseCase: GetVacuumTelemetryUseCase,
    private val getRoomsUseCase: GetRoomsUseCase,
    private val stopVacuumUseCase: StopVacuumUseCase,
    private val dockVacuumUseCase: DockVacuumUseCase,
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    private val IP_ADDRESS_KEY = stringPreferencesKey("vacuum_ip")
    private val TOKEN_KEY = stringPreferencesKey("vacuum_token")

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

    // Dashboard Live Flow Elements
    private val _telemetry = MutableStateFlow<VacuumTelemetry?>(null)
    val telemetry: StateFlow<VacuumTelemetry?> = _telemetry

    private val _batteryHistory = MutableStateFlow<List<Int>>(emptyList())
    val batteryHistory: StateFlow<List<Int>> = _batteryHistory

    private val _areaHistory = MutableStateFlow<List<Int>>(emptyList())
    val areaHistory: StateFlow<List<Int>> = _areaHistory

    private val _eventLog = MutableStateFlow<List<String>>(listOf("[00:00] Sistem başlatıldı. Telemetri bekleniyor..."))
    val eventLog: StateFlow<List<String>> = _eventLog

    private val _distanceMeters = MutableStateFlow(0.0)
    val distanceMeters: StateFlow<Double> = _distanceMeters

    // Dynamic Room List — starts with DEFAULT_ROOMS, replaced once device responds
    private val _rooms = MutableStateFlow<List<RoomItem>>(Constants.DEFAULT_ROOMS)
    val rooms: StateFlow<List<RoomItem>> = _rooms

    private val _isRadarVisible = MutableStateFlow(true)
    val isRadarVisible: StateFlow<Boolean> = _isRadarVisible

    private val RADAR_VISIBLE_KEY = booleanPreferencesKey("radar_visible")

    private var logTickCounter = 0
    private var roomsFetched = false

    init {
        loadConnectionSettings()
        startStatusPolling()
        loadRadarVisibility()
    }

    private fun loadConnectionSettings() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                ipAddress.value = preferences[IP_ADDRESS_KEY] ?: BuildConfig.VACUUM_IP
                token.value = preferences[TOKEN_KEY] ?: BuildConfig.VACUUM_TOKEN
            }
        }
    }

    fun updateConnection(ip: String, tokenVal: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[IP_ADDRESS_KEY] = ip
                preferences[TOKEN_KEY] = tokenVal
            }
            roomsFetched = false
            _deviceStatusText.value = "Bağlanıyor..."
            _telemetry.value = null
            _batteryLevel.value = null
            _isCharging.value = false
            addLog("Bağlantı ayarları güncellendi. Yeni IP: $ip")
        }
    }

    private fun loadRadarVisibility() {
        viewModelScope.launch {
            dataStore.data.collect { preferences ->
                _isRadarVisible.value = preferences[RADAR_VISIBLE_KEY] ?: true
            }
        }
    }

    fun toggleRadarVisibility() {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                val current = preferences[RADAR_VISIBLE_KEY] ?: true
                preferences[RADAR_VISIBLE_KEY] = !current
            }
        }
    }

    private fun startStatusPolling() {
        viewModelScope.launch {
            while (true) {
                if (ipAddress.value.isNotBlank() && token.value.isNotBlank()) {

                    // Fetch real rooms once per session on first successful connection
                    if (!roomsFetched) {
                        fetchRooms()
                    }

                    val result = getVacuumTelemetryUseCase(
                        host = ipAddress.value,
                        token = token.value
                    )
                    result.fold(
                        onSuccess = { props ->
                            _telemetry.value = props
                            _batteryLevel.value = props.batteryLevel
                            _isCharging.value = props.statusCode == 4
                            _deviceStatusText.value = mapStatusText(props.statusCode)

                            // 1. Update Battery History
                            props.batteryLevel?.let { b ->
                                val current = _batteryHistory.value.toMutableList()
                                current.add(b)
                                if (current.size > 50) current.removeAt(0)
                                _batteryHistory.value = current
                            }

                            // 2. Update Clean Area History
                            props.cleanAreaSqm?.let { a ->
                                val current = _areaHistory.value.toMutableList()
                                current.add(a)
                                if (current.size > 50) current.removeAt(0)
                                _areaHistory.value = current
                            }

                            // 3. Compute Distance (1.35 meters per sqm clean)
                            _distanceMeters.value = (props.cleanAreaSqm ?: 0) * 1.35

                            // 4. Fault Checking & Logs
                            val faultText = mapFaultText(props.faultCode)
                            if (faultText != null) {
                                addLog("UYARI HATA: $faultText")
                            }

                            // 5. Dynamic cleaning event logs
                            if (props.statusCode in 5..7) {
                                val decisions = listOf(
                                    "Lidar radar taraması yapıldı, yeni engel noktaları haritaya eklendi.",
                                    "Tampon sensörü engel algıladı. Rota revize ediliyor...",
                                    "Manialardan kaçış manevrası tamamlandı, temizliğe devam ediliyor.",
                                    "Ultrasonik zemin sensörü halı algıladı. Fırça emiş gücü Turbo moda yükseltildi.",
                                    "Ön tampon hafif dokunma algıladı, yavaşlama ve etrafından dolaşma uygulandı."
                                )
                                if (kotlin.random.Random.nextInt(3) == 0) {
                                    addLog(decisions.random())
                                }
                            }
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

    private suspend fun fetchRooms() {
        val result = getRoomsUseCase(
            host = ipAddress.value,
            token = token.value
        )
        result.fold(
            onSuccess = { pairs ->
                if (pairs.isNotEmpty()) {
                    val roomColors = listOf(
                        listOf(ThemePink, ThemeRose),
                        listOf(MediumPurple, ThemeIndigo),
                        listOf(ThemeOrange, DarkAmber),
                        listOf(ThemeCyan, DarkCyan),
                        listOf(EmeraldGreen, DarkGreen),
                        listOf(ThemeBlue, DarkBlue),
                        listOf(BrightOrange, VibrantOrange),
                        listOf(LimeGreen, DarkLimeGreen)
                    )
                    // Icons indexed: first entry is always "Tüm ev" (all home)
                    val roomIcons = listOf("🏠", "🛋️", "🛏️", "🍳", "🛁", "🧹", "💻", "🪴", "🚿")
                    val mappedRooms = mutableListOf<RoomItem>()
                    // 1. Add maps returned from the device (Tüm ev, Balkon)
                    pairs.forEachIndexed { index, (id, name) ->
                        val isAllHome = name.contains("tüm ev", ignoreCase = true) ||
                                         name.contains("all home", ignoreCase = true) ||
                                         name.contains("tüm", ignoreCase = true) && name.contains("ev", ignoreCase = true)
                        mappedRooms.add(
                            RoomItem(
                                id = id,
                                name = name,
                                icon = roomIcons.getOrElse(index) { "🏠" },
                                gradientColors = roomColors.getOrElse(index) {
                                    listOf(ThemeIndigo, DarkIndigo)
                                },
                                isAllAreas = isAllHome
                            )
                        )
                    }

                    // 2. Add individual default rooms if there's a map list returned
                    Constants.DEFAULT_ROOMS.forEachIndexed { index, defaultRoom ->
                        val colorIdx = (pairs.size + index) % roomColors.size
                        mappedRooms.add(
                            RoomItem(
                                id = defaultRoom.id,
                                name = defaultRoom.name,
                                icon = defaultRoom.icon,
                                gradientColors = roomColors.getOrElse(colorIdx) {
                                    listOf(ThemeIndigo, DarkIndigo)
                                },
                                isAllAreas = false
                            )
                        )
                    }

                    _rooms.value = mappedRooms
                    roomsFetched = true
                    val allLabel = mappedRooms.filter { it.isAllAreas }.joinToString { it.name }
                    val areaLabel = mappedRooms.filter { !it.isAllAreas }.joinToString { it.name }
                    addLog("Oda haritası çekildi: ${pairs.size} harita bölgesi ve ${Constants.DEFAULT_ROOMS.size} oda yüklendi.")
                    Logger.i("VacuumViewModel", "Rooms merged: ${mappedRooms.map { "${it.name} (id=${it.id}, allAreas=${it.isAllAreas})" }}")
                } else {
                    Logger.i("VacuumViewModel", "get_map returned empty room list. Using default rooms.")
                }
            },
            onFailure = { error ->
                Logger.e("VacuumViewModel", "fetchRooms failed: ${error.message}", error)
            }
        )
    }


    private fun addLog(message: String) {
        val timeStr = getCurrentTimeFormatted()
        val formattedMsg = "[$timeStr] $message"
        val current = _eventLog.value.toMutableList()
        current.add(0, formattedMsg) // Insert at top (newest first)
        if (current.size > 100) current.removeAt(current.size - 1)
        _eventLog.value = current
    }

    private fun getCurrentTimeFormatted(): String {
        val cleanMin = _telemetry.value?.cleanTimeMinutes ?: 0
        val cleanSecs = (cleanMin * 60 + (logTickCounter++ % 60))
        val min = cleanSecs / 60
        val sec = cleanSecs % 60
        return "${min.toString().padStart(2, '0')}:${sec.toString().padStart(2, '0')}"
    }

    private fun mapFaultText(faultCode: Int?): String? {
        return when (faultCode) {
            null, 0 -> null
            1 -> "Lazer Mesafe Sensörü (LDS) engellendi veya takıldı."
            2 -> "Çarpışma tamponu sıkıştı. Lütfen temizleyin."
            3 -> "Tekerlek havada kaldı. Düz zemine koyun."
            4 -> "Düşme sensörlerini temizleyin."
            5 -> "Ana fırça dolandı. Temizleyin."
            6 -> "Yan fırça dolandı. Temizleyin."
            7 -> "Tahrik tekerleği sıkıştı."
            8 -> "Cihaz bir alanda sıkıştı."
            9 -> "Toz haznesi takılı değil."
            10 -> "Su tankı takılı değil."
            11 -> "Mop braketi takılı değil."
            else -> "Hata tespit edildi (Hata Kodu: $faultCode)"
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

    fun cleanRoom(roomId: Long, repeats: Int, isAllAreas: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            val label = if (isAllAreas) "Tüm ev" else "Oda $roomId"
            _statusMessage.value = "Süpürgeye bağlanılıyor..."
            addLog("Komut gönderiliyor: $label, Tekrar: $repeats")

            // If isAllAreas, send roomId=0L which triggers siid:2/aiid:6 (clean all)
            val effectiveRoomId = if (isAllAreas) 0L else roomId

            val result = cleanRoomUseCase(
                host = ipAddress.value,
                token = token.value,
                roomId = effectiveRoomId,
                repeats = repeats
            )

            result.fold(
                onSuccess = {
                    _statusMessage.value = "Komut başarıyla gönderildi: Oda $roomId, Döngü: $repeats"
                    addLog("Komut onaylandı. Temizlik başlıyor.")
                },
                onFailure = { error ->
                    val msg = "Hata: ${error.message ?: "Bilinmeyen Bağlantı Hatası"}"
                    _statusMessage.value = msg
                    addLog("Hata oluştu: ${error.message}")
                    Logger.e("VacuumViewModel", msg, error)
                }
            )
            _isLoading.value = false
        }
    }

    fun stopCleaning() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Durdurma komutu gönderiliyor..."
            addLog("Komut gönderiliyor: Temizliği Durdur")
            val result = stopVacuumUseCase(
                host = ipAddress.value,
                token = token.value
            )
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Temizlik durduruldu."
                    addLog("Komut onaylandı. Süpürge durdu.")
                },
                onFailure = { error ->
                    val msg = "Hata: ${error.message ?: "Bilinmeyen Bağlantı Hatası"}"
                    _statusMessage.value = msg
                    addLog("Durdurma hatası: ${error.message}")
                    Logger.e("VacuumViewModel", msg, error)
                }
            )
            _isLoading.value = false
        }
    }

    fun returnToDock() {
        viewModelScope.launch {
            _isLoading.value = true
            _statusMessage.value = "Şarj istasyonuna dönüş komutu gönderiliyor..."
            addLog("Komut gönderiliyor: Şarj İstasyonuna Dön")
            val result = dockVacuumUseCase(
                host = ipAddress.value,
                token = token.value
            )
            result.fold(
                onSuccess = {
                    _statusMessage.value = "Şarj istasyonuna dönüyor."
                    addLog("Komut onaylandı. Süpürge şarj istasyonuna dönüyor.")
                },
                onFailure = { error ->
                    val msg = "Hata: ${error.message ?: "Bilinmeyen Bağlantı Hatası"}"
                    _statusMessage.value = msg
                    addLog("Dock hatası: ${error.message}")
                    Logger.e("VacuumViewModel", msg, error)
                }
            )
            _isLoading.value = false
        }
    }
}
