package com.vahitkeskin.loopsweep.data.network

import com.vahitkeskin.loopsweep.domain.model.XiaomiDevice
import com.vahitkeskin.loopsweep.domain.model.XiaomiSession
import com.vahitkeskin.loopsweep.getEpochSeconds
import com.vahitkeskin.loopsweep.utils.Base64
import com.vahitkeskin.loopsweep.utils.HMACSHA256
import com.vahitkeskin.loopsweep.utils.SHA256
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlin.random.Random

class XiaomiCloudClient {
    private val client = HttpClient {
        followRedirects = false
    }

    suspend fun login(username: String, passwordHashHex: String): Result<XiaomiSession> {
        return try {
            val url = "https://account.xiaomi.com/pass/serviceLoginAuth2?sid=xiaomiio&_json=true"
            val formBody = "sid=xiaomiio&_json=true&user=${encodeUrl(username)}&hash=${passwordHashHex.uppercase()}"
            
            val response = client.post(url) {
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(formBody)
            }
            
            val bodyText = response.bodyAsText()
            val cleanJson = bodyText.removePrefix("&&&START&&&")
            
            val code = extractJsonIntField(cleanJson, "code")
            if (code != 0) {
                val desc = extractJsonField(cleanJson, "description") ?: "Bilinmeyen hata"
                return Result.failure(Exception("Xiaomi Giriş Hatası (Kod $code): $desc"))
            }
            
            val ssecurity = extractJsonField(cleanJson, "ssecurity")
                ?: return Result.failure(Exception("ssecurity değeri yanıtta bulunamadı."))
            val location = extractJsonField(cleanJson, "location")
                ?: return Result.failure(Exception("location değeri yanıtta bulunamadı."))
            val userId = extractJsonField(cleanJson, "userId")
                ?: return Result.failure(Exception("userId değeri yanıtta bulunamadı."))
                
            // Step 2: GET location to extract serviceToken cookie
            val redirectResponse = client.get(location)
            val cookies = redirectResponse.headers.getAll(HttpHeaders.SetCookie) ?: emptyList()
            var serviceToken: String? = null
            for (cookie in cookies) {
                if (cookie.contains("serviceToken=")) {
                    val match = Regex("serviceToken=([^;]+)").find(cookie)
                    serviceToken = match?.groupValues?.get(1)
                    break
                }
            }
            
            if (serviceToken == null) {
                return Result.failure(Exception("serviceToken cookie'si alınamadı. Lütfen daha sonra tekrar deneyin."))
            }
            
            Result.success(
                XiaomiSession(
                    userId = userId,
                    serviceToken = serviceToken,
                    ssecurity = ssecurity
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchDevices(
        userId: String,
        serviceToken: String,
        ssecurity: String,
        region: String
    ): Result<List<XiaomiDevice>> {
        return try {
            val domain = if (region.lowercase() == "cn") "api.io.mi.com" else "${region.lowercase()}.api.io.mi.com"
            val url = "https://$domain/app/home/device_list"
            
            val postData = "{\"getVirtualModel\":false,\"getHuamiDevices\":0}"
            
            // Generate Nonce
            val randomBytes = ByteArray(8)
            Random.nextBytes(randomBytes)
            val timeMinutes = getEpochSeconds() / 60
            val timeBytes = ByteArray(4)
            timeBytes[0] = (timeMinutes ushr 24).toByte()
            timeBytes[1] = (timeMinutes ushr 16).toByte()
            timeBytes[2] = (timeMinutes ushr 8).toByte()
            timeBytes[3] = timeMinutes.toByte()
            
            val nonceBytes = ByteArray(12)
            randomBytes.copyInto(nonceBytes, 0, 0, 8)
            timeBytes.copyInto(nonceBytes, 8, 0, 4)
            val nonce = Base64.encode(nonceBytes)
            
            // Signature logic
            val signedNonceBytes = SHA256.hash(Base64.decode(ssecurity) + Base64.decode(nonce))
            val signedNonceBase64 = Base64.encode(signedNonceBytes)
            
            val path = "/app/home/device_list"
            val message = "$path&$signedNonceBase64&$nonce&data=$postData"
            val signatureBytes = HMACSHA256.sign(signedNonceBytes, message.encodeToByteArray())
            val signature = Base64.encode(signatureBytes)
            
            val formBody = "data=${encodeUrl(postData)}&_nonce=${encodeUrl(nonce)}&signature=${encodeUrl(signature)}"
            
            val response = client.post(url) {
                header(HttpHeaders.Cookie, "userId=$userId; serviceToken=$serviceToken")
                header(HttpHeaders.UserAgent, "Android-7.1.1-1.0.0-ONEPLUS A3003-1-184000007")
                contentType(ContentType.Application.FormUrlEncoded)
                setBody(formBody)
            }
            
            val bodyText = response.bodyAsText()
            val code = extractJsonIntField(bodyText, "code")
            if (code != 0) {
                val messageStr = extractJsonField(bodyText, "message") ?: "Bilinmeyen hata"
                return Result.failure(Exception("Cihaz listesi çekme hatası (Kod $code): $messageStr"))
            }
            
            val devices = parseDevices(bodyText)
            Result.success(devices)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseDevices(json: String): List<XiaomiDevice> {
        val listIndex = json.indexOf("\"list\"")
        if (listIndex == -1) return emptyList()
        val arrayStart = json.indexOf("[", listIndex)
        if (arrayStart == -1) return emptyList()
        
        var bracketCount = 1
        var i = arrayStart + 1
        var inString = false
        var escape = false
        while (i < json.length && bracketCount > 0) {
            val c = json[i]
            if (escape) {
                escape = false
            } else if (c == '\\') {
                escape = true
            } else if (c == '"') {
                inString = !inString
            } else if (!inString) {
                if (c == '[') bracketCount++
                else if (c == ']') bracketCount--
            }
            i++
        }
        if (bracketCount > 0) return emptyList()
        val listContent = json.substring(arrayStart + 1, i - 1)
        
        val objectStrings = mutableListOf<String>()
        var braceCount = 0
        var start = -1
        inString = false
        escape = false
        for (idx in listContent.indices) {
            val c = listContent[idx]
            if (escape) {
                escape = false
            } else if (c == '\\') {
                escape = true
            } else if (c == '"') {
                inString = !inString
            } else if (!inString) {
                if (c == '{') {
                    if (braceCount == 0) {
                        start = idx
                    }
                    braceCount++
                } else if (c == '}') {
                    braceCount--
                    if (braceCount == 0 && start != -1) {
                        objectStrings.add(listContent.substring(start, idx + 1))
                    }
                }
            }
        }
        
        val devices = mutableListOf<XiaomiDevice>()
        for (obj in objectStrings) {
            val did = extractJsonField(obj, "did") ?: continue
            val name = extractJsonField(obj, "name") ?: "Bilinmeyen Cihaz"
            val model = extractJsonField(obj, "model") ?: ""
            val ip = extractJsonField(obj, "localip") ?: ""
            val token = extractJsonField(obj, "token") ?: ""
            
            val isOnlineRegex = Regex("\"isOnline\"\\s*:\\s*(true|false|1|0)")
            val isOnlineMatch = isOnlineRegex.find(obj)
            val isOnline = isOnlineMatch?.groupValues?.get(1)?.let { it == "true" || it == "1" } ?: false
            
            devices.add(XiaomiDevice(name = name, model = model, ip = ip, token = token, did = did, isOnline = isOnline))
        }
        return devices
    }

    private fun extractJsonField(json: String, fieldName: String): String? {
        val regex = Regex("\"$fieldName\"\\s*:\\s*\"([^\"]+)\"")
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.let { decodeUnicodeEscapes(it) }
    }

    private fun extractJsonIntField(json: String, fieldName: String): Int? {
        val regex = Regex("\"$fieldName\"\\s*:\\s*([0-9\\-]+)")
        val match = regex.find(json)
        return match?.groupValues?.get(1)?.toIntOrNull()
    }

    private fun decodeUnicodeEscapes(str: String): String {
        val regex = Regex("\\\\u([0-9a-fA-F]{4})")
        return regex.replace(str) { matchResult ->
            val hex = matchResult.groupValues[1]
            try {
                hex.toInt(16).toChar().toString()
            } catch (e: Exception) {
                matchResult.value
            }
        }
    }

    private fun encodeUrl(s: String): String {
        val allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-._~"
        val sb = StringBuilder()
        val hexChars = "0123456789ABCDEF"
        for (c in s) {
            if (c in allowed) {
                sb.append(c)
            } else {
                val bytes = c.toString().encodeToByteArray()
                for (b in bytes) {
                    val i = b.toInt() and 0xFF
                    sb.append('%')
                    sb.append(hexChars[i ushr 4])
                    sb.append(hexChars[i and 0x0F])
                }
            }
        }
        return sb.toString()
    }
}
