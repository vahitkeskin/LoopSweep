package com.vahitkeskin.loopsweep.data.network

import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import com.vahitkeskin.loopsweep.utils.MD5
import com.vahitkeskin.loopsweep.utils.encryptAes128Cbc
import com.vahitkeskin.loopsweep.utils.decryptAes128Cbc
import com.vahitkeskin.loopsweep.utils.hexToByteArray
import com.vahitkeskin.loopsweep.utils.readInt32BE
import com.vahitkeskin.loopsweep.utils.Constants
import com.vahitkeskin.loopsweep.utils.toHexString

import com.vahitkeskin.loopsweep.utils.Logger
import com.vahitkeskin.loopsweep.domain.model.VacuumProperties
import com.vahitkeskin.loopsweep.domain.model.VacuumTelemetry

object VacuumClient {
    private suspend fun sendUdp(host: String, port: Int, requestBytes: ByteArray, timeoutMs: Long = 2000): ByteArray? {
        return withContext(Dispatchers.Default) {
            val selectorManager = SelectorManager(Dispatchers.Default)
            val socket = aSocket(selectorManager).udp().bind()
            try {
                withTimeout(timeoutMs) {
                    val address = InetSocketAddress(host, port)
                    val datagram = Datagram(ByteReadPacket(requestBytes), address)
                    socket.send(datagram)
                    val response = socket.receive()
                    response.packet.readBytes()
                }
            } catch (e: Exception) {
                Logger.e("VacuumClient", "UDP Error (host=$host, port=$port): ${e.message}", e)
                null
            } finally {
                socket.close()
                selectorManager.close()
            }
        }
    }

    private fun buildHelloPacket(): ByteArray {
        val packet = ByteArray(32)
        packet[0] = 0x21.toByte()
        packet[1] = 0x31.toByte()
        packet[2] = 0x00.toByte()
        packet[3] = 0x20.toByte()
        for (i in 4 until 32) {
            packet[i] = 0xFF.toByte()
        }
        return packet
    }

    private fun buildCommandPacket(deviceId: ByteArray, stamp: Int, tokenBytes: ByteArray, payload: ByteArray): ByteArray {
        val length = 32 + payload.size
        val packet = ByteArray(length)
        packet[0] = 0x21.toByte()
        packet[1] = 0x31.toByte()
        packet[2] = ((length ushr 8) and 0xFF).toByte()
        packet[3] = (length and 0xFF).toByte()
        packet[4] = 0x00.toByte()
        packet[5] = 0x00.toByte()
        packet[6] = 0x00.toByte()
        packet[7] = 0x00.toByte()
        deviceId.copyInto(packet, 8, 0, 4)
        packet[12] = ((stamp ushr 24) and 0xFF).toByte()
        packet[13] = ((stamp ushr 16) and 0xFF).toByte()
        packet[14] = ((stamp ushr 8) and 0xFF).toByte()
        packet[15] = (stamp and 0xFF).toByte()
        tokenBytes.copyInto(packet, 16, 0, 16)
        payload.copyInto(packet, 32, 0, payload.size)
        val checksum = MD5.hash(packet)
        checksum.copyInto(packet, 16, 0, 16)
        return packet
    }

    suspend fun sendCommand(host: String, tokenHex: String, roomId: Long, repeats: Int): Result<String> {
        return try {
            val tokenBytes = tokenHex.hexToByteArray()
            if (tokenBytes.size != 16) {
                return Result.failure(Exception("Token must be exactly 32 hex characters"))
            }
            
            // Step 1: Handshake
            val helloPacket = buildHelloPacket()
            Logger.i("VacuumClient", "Atılan Handshake UDP Paketi (Hex): ${helloPacket.toHexString()}")
            val helloResponse = sendUdp(host, Constants.VACUUM_PORT, helloPacket, timeoutMs = 2000)
                ?: return Result.failure(Exception("Handshake failed. Local network error or device is offline."))
            Logger.i("VacuumClient", "Alınan Handshake UDP Paketi (Hex): ${helloResponse.toHexString()}")
            
            if (helloResponse.size < 32) {
                return Result.failure(Exception("Invalid handshake response from device."))
            }
            
            val deviceId = helloResponse.copyOfRange(8, 12)
            val stamp = helloResponse.readInt32BE(12)
            
            val deviceIdLong = (helloResponse.readInt32BE(8).toLong()) and 0xFFFFFFFFL
            val didStr = deviceIdLong.toString()
            Logger.i("VacuumClient", "Handshake success: did=$didStr, stamp=$stamp")
            
            // Key / IV derivation
            val key = MD5.hash(tokenBytes)
            val iv = MD5.hash(key + tokenBytes)
            
            // Step 2: Build correct MIoT payload based on room/map ID
            // Diagnostic findings:
            //   siid:2, aiid:6, in:[]     → clean ALL areas (Tüm ev)
            //   siid:12, aiid:1, in:[id]  → select specific map/area and start cleaning
            //   siid:7, aiid:3, in:[id, mode, oper] -> clean specific room (e.g. 16 to 21)
            val jsonPayload: String
            if (roomId == 0L) {
                // 0 = Tüm ev / all areas — use general start sweep
                jsonPayload = "{\"id\":1,\"method\":\"action\",\"params\":{\"did\":\"$didStr\",\"siid\":2,\"aiid\":6,\"in\":[]}}"
                Logger.i("VacuumClient", "Komut: Tüm ev temizliği (siid=2, aiid=6)")
            } else if (roomId in 1L..99L) {
                // Specific individual room ID (e.g. 16L = Salon).
                // Use Service 7, Action 3 (set-room-clean) with:
                //   clean-room-ids = "$roomId"
                //   clean-room-mode = 0 (Global)
                //   clean-room-oper = 1 (Start)
                jsonPayload = "{\"id\":1,\"method\":\"action\",\"params\":{\"did\":\"$didStr\",\"siid\":7,\"aiid\":3,\"in\":[\"$roomId\",0,1]}}"
                Logger.i("VacuumClient", "Komut: Bölgesel oda temizliği (siid=7, aiid=3, room=$roomId)")
            } else {
                // Specific map/area ID (e.g., 1779096923 = Balkon)
                // siid:12, aiid:1 = select map + start cleaning — confirmed code:0 in diagnostics
                jsonPayload = "{\"id\":1,\"method\":\"action\",\"params\":{\"did\":\"$didStr\",\"siid\":12,\"aiid\":1,\"in\":[$roomId]}}"
                Logger.i("VacuumClient", "Komut: Oda haritası temizliği (siid=12, aiid=1, id=$roomId)")
            }
            Logger.i("VacuumClient", "Atılan Request Payload (JSON): $jsonPayload")
            val payloadBytes = jsonPayload.encodeToByteArray()
            val encryptedPayload = encryptAes128Cbc(payloadBytes, key, iv)
            
            // Step 3: Build command packet
            val commandPacket = buildCommandPacket(deviceId, stamp + 1, tokenBytes, encryptedPayload)
            Logger.i("VacuumClient", "Atılan Request UDP Paketi (Hex): ${commandPacket.toHexString()}")
            
            // Step 4: Send & Receive
            val responsePacket = sendUdp(host, Constants.VACUUM_PORT, commandPacket, timeoutMs = 3000)
                ?: return Result.failure(Exception("Clean command sent, but vacuum cleaner did not respond."))
            Logger.i("VacuumClient", "Alınan Response UDP Paketi (Hex): ${responsePacket.toHexString()}")
            
            if (responsePacket.size < 32) {
                return Result.failure(Exception("Received corrupted response packet."))
            }
            
            val encryptedResponsePayload = responsePacket.copyOfRange(32, responsePacket.size)
            if (encryptedResponsePayload.isEmpty()) {
                Logger.i("VacuumClient", "Alınan Response ACK (Boş Payload)")
                Result.success("Success (Ack)")
            } else {
                val decryptedResponsePayload = decryptAes128Cbc(encryptedResponsePayload, key, iv)
                val responseString = decryptedResponsePayload.decodeToString()
                Logger.i("VacuumClient", "Alınan Response Payload (JSON): $responseString")
                
                val hasError = responseString.contains("\"code\":-") && !responseString.contains("\"code\":0")
                if (!hasError) {
                    Result.success(responseString)
                } else {
                    Result.failure(Exception("Cihaz hatası: $responseString"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun parsePropertyValue(json: String, siid: Int, piid: Int): Int? {
        try {
            val resultStart = json.indexOf("\"result\":")
            if (resultStart == -1) return null
            val arrayStart = json.indexOf("[", resultStart)
            val arrayEnd = json.indexOf("]", arrayStart)
            if (arrayStart == -1 || arrayEnd == -1) return null
            val arrayContent = json.substring(arrayStart + 1, arrayEnd)
            
            val objects = arrayContent.split("}")
            for (obj in objects) {
                if (obj.contains("\"siid\":$siid") && obj.contains("\"piid\":$piid")) {
                    val valuePattern = "\"value\":\\s*(\\d+)".toRegex()
                    val match = valuePattern.find(obj)
                    if (match != null) {
                        return match.groupValues[1].toIntOrNull()
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("VacuumClient", "Error parsing property value (siid=$siid, piid=$piid): ${e.message}", e)
        }
        return null
    }

    suspend fun getProperties(host: String, tokenHex: String): Result<VacuumProperties> {
        return try {
            val tokenBytes = tokenHex.hexToByteArray()
            if (tokenBytes.size != 16) {
                return Result.failure(Exception("Token must be exactly 32 hex characters"))
            }
            
            // Step 1: Handshake
            val helloPacket = buildHelloPacket()
            Logger.i("VacuumClient", "Atılan Handshake UDP Paketi (Hex): ${helloPacket.toHexString()}")
            val helloResponse = sendUdp(host, Constants.VACUUM_PORT, helloPacket, timeoutMs = 2000)
                ?: return Result.failure(Exception("Handshake failed. Local network error or device is offline."))
            Logger.i("VacuumClient", "Alınan Handshake UDP Paketi (Hex): ${helloResponse.toHexString()}")
            
            if (helloResponse.size < 32) {
                return Result.failure(Exception("Invalid handshake response from device."))
            }
            
            val deviceId = helloResponse.copyOfRange(8, 12)
            val stamp = helloResponse.readInt32BE(12)
            
            val deviceIdLong = (helloResponse.readInt32BE(8).toLong()) and 0xFFFFFFFFL
            val didStr = deviceIdLong.toString()
            Logger.i("VacuumClient", "Handshake success for getProperties: did=$didStr, stamp=$stamp")
            
            // Key / IV derivation
            val key = MD5.hash(tokenBytes)
            val iv = MD5.hash(key + tokenBytes)
            
            // Step 2: Encrypt Payload (MIoT Get Properties: siid 2 piid 1, siid 3 piid 1)
            val jsonPayload = "{\"id\":100,\"method\":\"get_properties\",\"params\":[{\"did\":\"$didStr\",\"siid\":2,\"piid\":1},{\"did\":\"$didStr\",\"siid\":3,\"piid\":1}]}"
            Logger.i("VacuumClient", "Atılan Request Properties Payload (JSON): $jsonPayload")
            val payloadBytes = jsonPayload.encodeToByteArray()
            val encryptedPayload = encryptAes128Cbc(payloadBytes, key, iv)
            
            // Step 3: Build command packet
            val commandPacket = buildCommandPacket(deviceId, stamp + 1, tokenBytes, encryptedPayload)
            Logger.i("VacuumClient", "Atılan Request Properties UDP Paketi (Hex): ${commandPacket.toHexString()}")
            
            // Step 4: Send & Receive
            val responsePacket = sendUdp(host, Constants.VACUUM_PORT, commandPacket, timeoutMs = 3000)
                ?: return Result.failure(Exception("Properties query sent, but vacuum cleaner did not respond."))
            Logger.i("VacuumClient", "Alınan Response Properties UDP Paketi (Hex): ${responsePacket.toHexString()}")
            
            if (responsePacket.size < 32) {
                return Result.failure(Exception("Received corrupted response packet."))
            }
            
            val encryptedResponsePayload = responsePacket.copyOfRange(32, responsePacket.size)
            if (encryptedResponsePayload.isEmpty()) {
                return Result.failure(Exception("Properties query received empty payload response."))
            } else {
                val decryptedResponsePayload = decryptAes128Cbc(encryptedResponsePayload, key, iv)
                val responseString = decryptedResponsePayload.decodeToString()
                Logger.i("VacuumClient", "Alınan Response Properties Payload (JSON): $responseString")
                
                val statusCode = parsePropertyValue(responseString, 2, 1)
                val batteryLevel = parsePropertyValue(responseString, 3, 1)
                
                Result.success(VacuumProperties(batteryLevel = batteryLevel, statusCode = statusCode))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseStringPropertyValue(json: String, siid: Int, piid: Int): String? {
        try {
            val resultStart = json.indexOf("\"result\":")
            if (resultStart == -1) return null
            val arrayStart = json.indexOf("[", resultStart)
            val arrayEnd = json.indexOf("]", arrayStart)
            if (arrayStart == -1 || arrayEnd == -1) return null
            val arrayContent = json.substring(arrayStart + 1, arrayEnd)
            
            val objects = arrayContent.split("}")
            for (obj in objects) {
                if (obj.contains("\"siid\":$siid") && obj.contains("\"piid\":$piid")) {
                    val valuePattern = "\"value\":\\s*\"([^\"]*)\"".toRegex()
                    val match = valuePattern.find(obj)
                    if (match != null) {
                        return match.groupValues[1]
                    }
                    val rawPattern = "\"value\":\\s*([^,}]+)".toRegex()
                    val rawMatch = rawPattern.find(obj)
                    if (rawMatch != null) {
                        val value = rawMatch.groupValues[1].trim()
                        if (value != "null") return value
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("VacuumClient", "Error parsing string property (siid=$siid, piid=$piid): ${e.message}", e)
        }
        return null
    }

    suspend fun getTelemetry(host: String, tokenHex: String): Result<VacuumTelemetry> {
        return try {
            val tokenBytes = tokenHex.hexToByteArray()
            if (tokenBytes.size != 16) {
                return Result.failure(Exception("Token must be exactly 32 hex characters"))
            }
            
            // Step 1: Handshake
            val helloPacket = buildHelloPacket()
            Logger.i("VacuumClient", "Atılan Handshake UDP Paketi (Hex): ${helloPacket.toHexString()}")
            val helloResponse = sendUdp(host, Constants.VACUUM_PORT, helloPacket, timeoutMs = 2000)
                ?: return Result.failure(Exception("Handshake failed. Local network error or device is offline."))
            Logger.i("VacuumClient", "Alınan Handshake UDP Paketi (Hex): ${helloResponse.toHexString()}")
            
            if (helloResponse.size < 32) {
                return Result.failure(Exception("Invalid handshake response from device."))
            }
            
            val deviceId = helloResponse.copyOfRange(8, 12)
            val stamp = helloResponse.readInt32BE(12)
            
            val deviceIdLong = (helloResponse.readInt32BE(8).toLong()) and 0xFFFFFFFFL
            val didStr = deviceIdLong.toString()
            Logger.i("VacuumClient", "Handshake success for getTelemetry: did=$didStr, stamp=$stamp")
            
            // Key / IV derivation
            val key = MD5.hash(tokenBytes)
            val iv = MD5.hash(key + tokenBytes)
            
            // Step 2: Encrypt Payload
            val jsonPayload = "{\"id\":101,\"method\":\"get_properties\",\"params\":[" +
                    "{\"did\":\"$didStr\",\"siid\":2,\"piid\":1}," +
                    "{\"did\":\"$didStr\",\"siid\":2,\"piid\":2}," +
                    "{\"did\":\"$didStr\",\"siid\":3,\"piid\":1}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":5}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":6}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":22}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":23}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":8}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":10}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":12}," +
                    "{\"did\":\"$didStr\",\"siid\":7,\"piid\":14}," +
                    "{\"did\":\"$didStr\",\"siid\":10,\"piid\":5}" +
                    "]}"
            
            Logger.i("VacuumClient", "Atılan Request Telemetry Payload (JSON): $jsonPayload")
            val payloadBytes = jsonPayload.encodeToByteArray()
            val encryptedPayload = encryptAes128Cbc(payloadBytes, key, iv)
            
            // Step 3: Build command packet
            val commandPacket = buildCommandPacket(deviceId, stamp + 1, tokenBytes, encryptedPayload)
            Logger.i("VacuumClient", "Atılan Request Telemetry UDP Paketi (Hex): ${commandPacket.toHexString()}")
            
            // Step 4: Send & Receive
            val responsePacket = sendUdp(host, Constants.VACUUM_PORT, commandPacket, timeoutMs = 3000)
                ?: return Result.failure(Exception("Telemetry query sent, but vacuum cleaner did not respond."))
            Logger.i("VacuumClient", "Alınan Response Telemetry UDP Paketi (Hex): ${responsePacket.toHexString()}")
            
            if (responsePacket.size < 32) {
                return Result.failure(Exception("Received corrupted response packet."))
            }
            
            val encryptedResponsePayload = responsePacket.copyOfRange(32, responsePacket.size)
            if (encryptedResponsePayload.isEmpty()) {
                return Result.failure(Exception("Telemetry query received empty payload response."))
            } else {
                val decryptedResponsePayload = decryptAes128Cbc(encryptedResponsePayload, key, iv)
                val responseString = decryptedResponsePayload.decodeToString()
                Logger.i("VacuumClient", "Alınan Response Telemetry Payload (JSON): $responseString")
                
                val statusCode = parsePropertyValue(responseString, 2, 1)
                val faultCode = parsePropertyValue(responseString, 2, 2)
                val batteryLevel = parsePropertyValue(responseString, 3, 1)
                val suctionState = parsePropertyValue(responseString, 7, 5)
                val waterState = parsePropertyValue(responseString, 7, 6)
                val cleanTime = parsePropertyValue(responseString, 7, 22)
                val cleanArea = parsePropertyValue(responseString, 7, 23)
                val sideBrush = parsePropertyValue(responseString, 7, 8)
                val mainBrush = parsePropertyValue(responseString, 7, 10)
                val filter = parsePropertyValue(responseString, 7, 12)
                val mop = parsePropertyValue(responseString, 7, 14)
                val cleaningPath = parseStringPropertyValue(responseString, 10, 5)
                
                Result.success(
                    VacuumTelemetry(
                        batteryLevel = batteryLevel,
                        statusCode = statusCode,
                        faultCode = faultCode,
                        cleanTimeMinutes = cleanTime,
                        cleanAreaSqm = cleanArea,
                        suctionState = suctionState,
                        waterState = waterState,
                        sideBrushLife = sideBrush,
                        mainBrushLife = mainBrush,
                        filterLife = filter,
                        mopLife = mop,
                        cleaningPath = cleaningPath
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRooms(host: String, tokenHex: String): Result<List<Pair<Long, String>>> {
        return try {
            val tokenBytes = tokenHex.hexToByteArray()
            if (tokenBytes.size != 16) {
                return Result.failure(Exception("Token must be exactly 32 hex characters"))
            }
            
            val helloPacket = buildHelloPacket()
            val helloResponse = sendUdp(host, Constants.VACUUM_PORT, helloPacket, timeoutMs = 2000)
                ?: return Result.failure(Exception("Handshake failed. Local network error or device is offline."))
            
            if (helloResponse.size < 32) {
                return Result.failure(Exception("Invalid handshake response from device."))
            }
            
            val deviceId = helloResponse.copyOfRange(8, 12)
            val stamp = helloResponse.readInt32BE(12)
            
            val deviceIdLong = (helloResponse.readInt32BE(8).toLong()) and 0xFFFFFFFFL
            val didStr = deviceIdLong.toString()
            Logger.i("VacuumClient", "Handshake success for fetchRooms: did=$didStr, stamp=$stamp")
            
            val key = MD5.hash(tokenBytes)
            val iv = MD5.hash(key + tokenBytes)
            
            val jsonPayload = "{\"id\":105,\"method\":\"get_map\",\"params\":[]}"
            Logger.i("VacuumClient", "Atılan Request get_map Payload (JSON): $jsonPayload")
            val payloadBytes = jsonPayload.encodeToByteArray()
            val encryptedPayload = encryptAes128Cbc(payloadBytes, key, iv)
            
            val commandPacket = buildCommandPacket(deviceId, stamp + 1, tokenBytes, encryptedPayload)
            val responsePacket = sendUdp(host, Constants.VACUUM_PORT, commandPacket, timeoutMs = 3000)
                ?: return Result.failure(Exception("get_map query sent, but vacuum cleaner did not respond."))
            
            if (responsePacket.size < 32) {
                return Result.failure(Exception("Received corrupted response packet."))
            }
            
            val encryptedResponsePayload = responsePacket.copyOfRange(32, responsePacket.size)
            if (encryptedResponsePayload.isEmpty()) {
                return Result.failure(Exception("get_map query received empty payload response."))
            } else {
                val decryptedResponsePayload = decryptAes128Cbc(encryptedResponsePayload, key, iv)
                val responseString = decryptedResponsePayload.decodeToString()
                Logger.i("VacuumClient", "Alınan Response get_map Payload (JSON): $responseString")
                
                val rooms = parseRoomsFromGetMap(responseString)
                Result.success(rooms)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseRoomsFromGetMap(json: String): List<Pair<Long, String>> {
        val roomsList = mutableListOf<Pair<Long, String>>()
        try {
            // The device returns a nested escaped JSON string inside the 'value' field.
            // Example raw: {"result":{"out":[{"piid":4,"value":"[{\"name\":\"Tüm ev\",\"id\":1763994619,...}]"}]}}
            // Strategy 1: Extract the value string content first, then parse name+id pairs.
            val valuePattern = """\"value\"\s*:\s*\"(\[.*?\])\"""".toRegex()
            val valueMatch = valuePattern.find(json)
            val searchTarget = if (valueMatch != null) {
                // Unescape the inner JSON string so we can parse it cleanly
                valueMatch.groupValues[1]
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\")
            } else {
                json // fallback: search raw response
            }
            Logger.d("VacuumClient", "parseRoomsFromGetMap: searchTarget=$searchTarget")

            // Strategy 2: Match name then id in each room object
            // Works on both escaped and unescaped JSON
            val roomPattern = """\"name\":\s*\"([^\"]+)\"[^}]*\"id\":\s*(\d+)""".toRegex()
            val matches = roomPattern.findAll(searchTarget)
            for (match in matches) {
                val name = match.groupValues[1].trim()
                val id = match.groupValues[2].toLongOrNull()
                if (id != null && name.isNotBlank()) {
                    roomsList.add(Pair(id, name))
                    Logger.i("VacuumClient", "Parsed room: name='$name', id=$id")
                }
            }

            // Strategy 3 fallback: scan for id+name in reverse order (in case order differs)
            if (roomsList.isEmpty()) {
                val altPattern = """\"id\":\s*(\d+)[^}]*\"name\":\s*\"([^\"]+)\"""".toRegex()
                val altMatches = altPattern.findAll(searchTarget)
                for (match in altMatches) {
                    val id = match.groupValues[1].toLongOrNull()
                    val name = match.groupValues[2].trim()
                    if (id != null && name.isNotBlank()) {
                        roomsList.add(Pair(id, name))
                        Logger.i("VacuumClient", "Parsed room (alt): name='$name', id=$id")
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("VacuumClient", "Error parsing rooms from get_map: ${e.message}", e)
        }
        return roomsList
    }
}

