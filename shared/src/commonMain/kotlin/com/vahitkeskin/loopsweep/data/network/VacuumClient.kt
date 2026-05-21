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

import com.vahitkeskin.loopsweep.utils.Logger

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

    suspend fun sendCommand(host: String, tokenHex: String, roomId: Int, repeats: Int): Result<String> {
        return try {
            val tokenBytes = tokenHex.hexToByteArray()
            if (tokenBytes.size != 16) {
                return Result.failure(Exception("Token must be exactly 32 hex characters"))
            }
            
            // Step 1: Handshake
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
            Logger.i("VacuumClient", "Handshake success: did=$didStr, stamp=$stamp")
            
            // Key / IV derivation
            val key = MD5.hash(tokenBytes)
            val iv = MD5.hash(key + tokenBytes)
            
            // Step 2: Encrypt Payload (MIoT Action: Service ID 2, Action ID 6: start-room-sweep)
            val jsonPayload = "{\"id\":1,\"method\":\"action\",\"params\":{\"did\":\"$didStr\",\"siid\":2,\"aiid\":6,\"in\":[\"$roomId\"]}}"
            Logger.i("VacuumClient", "Sending payload: $jsonPayload")
            val payloadBytes = jsonPayload.encodeToByteArray()
            val encryptedPayload = encryptAes128Cbc(payloadBytes, key, iv)
            
            // Step 3: Build command packet
            val commandPacket = buildCommandPacket(deviceId, stamp + 1, tokenBytes, encryptedPayload)
            
            // Step 4: Send & Receive
            val responsePacket = sendUdp(host, Constants.VACUUM_PORT, commandPacket, timeoutMs = 3000)
                ?: return Result.failure(Exception("Clean command sent, but vacuum cleaner did not respond."))
            
            if (responsePacket.size < 32) {
                return Result.failure(Exception("Received corrupted response packet."))
            }
            
            val encryptedResponsePayload = responsePacket.copyOfRange(32, responsePacket.size)
            if (encryptedResponsePayload.isEmpty()) {
                Logger.i("VacuumClient", "Command ACK received (empty payload)")
                Result.success("Success (Ack)")
            } else {
                val decryptedResponsePayload = decryptAes128Cbc(encryptedResponsePayload, key, iv)
                val responseString = decryptedResponsePayload.decodeToString()
                Logger.i("VacuumClient", "Decrypted response: $responseString")
                
                if (responseString.contains("\"result\":") || responseString.lowercase().contains("ok")) {
                    Result.success(responseString)
                } else {
                    Result.failure(Exception("Device error: $responseString"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
