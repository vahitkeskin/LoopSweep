package com.vahitkeskin.loopsweep.utils

import kotlin.math.abs
import kotlin.math.sin

object MD5 {
    fun hash(data: ByteArray): ByteArray {
        val INIT_A = 0x67452301
        val INIT_B = 0xEFCDAB89L.toInt()
        val INIT_C = 0x98BADCFEL.toInt()
        val INIT_D = 0x10325476
        
        val SHIFT_AMTS = intArrayOf(
            7, 12, 17, 22,
            5, 9, 14, 20,
            4, 11, 16, 23,
            6, 10, 15, 21
        )
        
        val TABLE_T = IntArray(64) { i ->
            (sin(i + 1.0).let { abs(it) } * 4294967296.0).toLong().toInt()
        }
        
        val messageLenBytes = data.size
        val numBlocks = ((messageLenBytes + 8) ushr 6) + 1
        val totalLen = numBlocks shl 6
        val paddingBytes = ByteArray(totalLen - messageLenBytes)
        paddingBytes[0] = 0x80.toByte()
        
        val messageLenBits = messageLenBytes.toLong() shl 3
        for (i in 0..7) {
            paddingBytes[paddingBytes.size - 8 + i] = (messageLenBits ushr (8 * i)).toByte()
        }
        
        var a = INIT_A
        var b = INIT_B
        var c = INIT_C
        var d = INIT_D
        
        val buffer = IntArray(16)
        for (i in 0 until numBlocks) {
            val index = i shl 6
            for (j in 0..15) {
                val byteIndex = index + (j shl 2)
                var value = 0
                for (k in 0..3) {
                    val bIdx = byteIndex + k
                    val byteValue = if (bIdx < messageLenBytes) {
                        data[bIdx].toInt() and 0xFF
                    } else {
                        paddingBytes[bIdx - messageLenBytes].toInt() and 0xFF
                    }
                    value = value or (byteValue shl (k shl 3))
                }
                buffer[j] = value
            }
            
            val originalA = a
            val originalB = b
            val originalC = c
            val originalD = d
            
            for (j in 0..63) {
                val div16 = j ushr 4
                var f = 0
                var bufferIndex = 0
                when (div16) {
                    0 -> {
                        f = (b and c) or (b.inv() and d)
                        bufferIndex = j
                    }
                    1 -> {
                        f = (b and d) or (c and d.inv())
                        bufferIndex = (j * 5 + 1) and 0x0F
                    }
                    2 -> {
                        f = b xor c xor d
                        bufferIndex = (j * 3 + 5) and 0x0F
                    }
                    3 -> {
                        f = c xor (b or d.inv())
                        bufferIndex = (j * 7) and 0x0F
                    }
                }
                val temp = d
                d = c
                c = b
                b = b + rotateLeft(a + f + TABLE_T[j] + buffer[bufferIndex], SHIFT_AMTS[(div16 shl 2) or (j and 3)])
                a = temp
            }
            
            a += originalA
            b += originalB
            c += originalC
            d += originalD
        }
        
        val md5 = ByteArray(16)
        for (i in 0..3) {
            val valToPack = when (i) {
                0 -> a
                1 -> b
                2 -> c
                else -> d
            }
            for (j in 0..3) {
                md5[(i shl 2) + j] = (valToPack ushr (j shl 3)).toByte()
            }
        }
        return md5
    }

    private fun rotateLeft(x: Int, amount: Int): Int {
        return (x shl amount) or (x ushr (32 - amount))
    }
}

class AES128(key: ByteArray) {
    private val w = IntArray(44)

    companion object {
        private val SBOX = intArrayOf(
            0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
            0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
            0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
            0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
            0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
            0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
            0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
            0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
            0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
            0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
            0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
            0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
            0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
            0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
            0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
            0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
        )

        private val INV_SBOX = intArrayOf(
            0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
            0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
            0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
            0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
            0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
            0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
            0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
            0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
            0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
            0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
            0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
            0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
            0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
            0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
            0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
            0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
        )

        private val RCON = intArrayOf(
            0x00, 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80, 0x1b, 0x36
        )
    }

    init {
        for (i in 0 until 4) {
            w[i] = ((key[i * 4].toInt() and 0xFF) shl 24) or
                   ((key[i * 4 + 1].toInt() and 0xFF) shl 16) or
                   ((key[i * 4 + 2].toInt() and 0xFF) shl 8) or
                   (key[i * 4 + 3].toInt() and 0xFF)
        }
        for (i in 4 until 44) {
            var temp = w[i - 1]
            if (i % 4 == 0) {
                temp = (temp shl 8) or (temp ushr 24)
                temp = ((SBOX[(temp ushr 24) and 0xFF] shl 24) or
                        (SBOX[(temp ushr 16) and 0xFF] shl 16) or
                        (SBOX[(temp ushr 8) and 0xFF] shl 8) or
                        SBOX[temp and 0xFF])
                temp = temp xor (RCON[i / 4] shl 24)
            }
            w[i] = w[i - 4] xor temp
        }
    }

    fun encryptBlock(input: ByteArray, inOffset: Int, output: ByteArray, outOffset: Int) {
        val state = IntArray(16) { i -> input[inOffset + i].toInt() and 0xFF }
        addRoundKey(state, 0)
        for (round in 1 until 10) {
            subBytes(state)
            shiftRows(state)
            mixColumns(state)
            addRoundKey(state, round)
        }
        subBytes(state)
        shiftRows(state)
        addRoundKey(state, 10)
        for (i in 0 until 16) {
            output[outOffset + i] = state[i].toByte()
        }
    }

    fun decryptBlock(input: ByteArray, inOffset: Int, output: ByteArray, outOffset: Int) {
        val state = IntArray(16) { i -> input[inOffset + i].toInt() and 0xFF }
        addRoundKey(state, 10)
        for (round in 9 downTo 1) {
            invShiftRows(state)
            invSubBytes(state)
            addRoundKey(state, round)
            invMixColumns(state)
        }
        invShiftRows(state)
        invSubBytes(state)
        addRoundKey(state, 0)
        for (i in 0 until 16) {
            output[outOffset + i] = state[i].toByte()
        }
    }

    private fun addRoundKey(state: IntArray, round: Int) {
        for (c in 0 until 4) {
            val keyWord = w[round * 4 + c]
            state[c * 4] = state[c * 4] xor ((keyWord ushr 24) and 0xFF)
            state[c * 4 + 1] = state[c * 4 + 1] xor ((keyWord ushr 16) and 0xFF)
            state[c * 4 + 2] = state[c * 4 + 2] xor ((keyWord ushr 8) and 0xFF)
            state[c * 4 + 3] = state[c * 4 + 3] xor (keyWord and 0xFF)
        }
    }

    private fun subBytes(state: IntArray) {
        for (i in 0 until 16) {
            state[i] = SBOX[state[i]]
        }
    }

    private fun invSubBytes(state: IntArray) {
        for (i in 0 until 16) {
            state[i] = INV_SBOX[state[i]]
        }
    }

    private fun shiftRows(state: IntArray) {
        var temp = state[1]
        state[1] = state[5]
        state[5] = state[9]
        state[9] = state[13]
        state[13] = temp

        temp = state[2]
        state[2] = state[10]
        state[10] = temp
        temp = state[6]
        state[6] = state[14]
        state[14] = temp

        temp = state[3]
        state[3] = state[15]
        state[15] = state[11]
        state[11] = state[7]
        state[7] = temp
    }

    private fun invShiftRows(state: IntArray) {
        var temp = state[13]
        state[13] = state[9]
        state[9] = state[5]
        state[5] = state[1]
        state[1] = temp

        temp = state[2]
        state[2] = state[10]
        state[10] = temp
        temp = state[6]
        state[6] = state[14]
        state[14] = temp

        temp = state[3]
        state[3] = state[7]
        state[7] = state[11]
        state[11] = state[15]
        state[15] = temp
    }

    private fun mixColumns(state: IntArray) {
        for (c in 0 until 4) {
            val s0 = state[c * 4]
            val s1 = state[c * 4 + 1]
            val s2 = state[c * 4 + 2]
            val s3 = state[c * 4 + 3]
            state[c * 4] = gmul(2, s0) xor gmul(3, s1) xor s2 xor s3
            state[c * 4 + 1] = s0 xor gmul(2, s1) xor gmul(3, s2) xor s3
            state[c * 4 + 2] = s0 xor s1 xor gmul(2, s2) xor gmul(3, s3)
            state[c * 4 + 3] = gmul(3, s0) xor s1 xor s2 xor gmul(2, s3)
        }
    }

    private fun invMixColumns(state: IntArray) {
        for (c in 0 until 4) {
            val s0 = state[c * 4]
            val s1 = state[c * 4 + 1]
            val s2 = state[c * 4 + 2]
            val s3 = state[c * 4 + 3]
            state[c * 4] = gmul(14, s0) xor gmul(11, s1) xor gmul(13, s2) xor gmul(9, s3)
            state[c * 4 + 1] = gmul(9, s0) xor gmul(14, s1) xor gmul(11, s2) xor gmul(13, s3)
            state[c * 4 + 2] = gmul(13, s0) xor gmul(9, s1) xor gmul(14, s2) xor gmul(11, s3)
            state[c * 4 + 3] = gmul(11, s0) xor gmul(13, s1) xor gmul(9, s2) xor gmul(14, s3)
        }
    }

    private fun gmul(g: Int, a: Int): Int {
        if (g == 1) return a
        var p = 0
        var hiBitSet: Int
        var tempA = a
        var tempG = g
        for (i in 0 until 8) {
            if ((tempG and 1) != 0) {
                p = p xor tempA
            }
            hiBitSet = tempA and 0x80
            tempA = (tempA shl 1) and 0xFF
            if (hiBitSet != 0) {
                tempA = tempA xor 0x1B
            }
            tempG = tempG ushr 1
        }
        return p
    }
}

fun encryptAes128Cbc(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    val aes = AES128(key)
    val paddingLen = 16 - (data.size % 16)
    val padded = ByteArray(data.size + paddingLen)
    data.copyInto(padded, 0, 0, data.size)
    for (i in data.size until padded.size) {
        padded[i] = paddingLen.toByte()
    }
    
    val encrypted = ByteArray(padded.size)
    var prevBlock = iv
    for (i in 0 until padded.size step 16) {
        val block = ByteArray(16)
        for (j in 0 until 16) {
            block[j] = (padded[i + j].toInt() xor prevBlock[j].toInt()).toByte()
        }
        aes.encryptBlock(block, 0, encrypted, i)
        prevBlock = encrypted.copyOfRange(i, i + 16)
    }
    return encrypted
}

fun decryptAes128Cbc(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
    val aes = AES128(key)
    val decrypted = ByteArray(data.size)
    var prevBlock = iv
    for (i in 0 until data.size step 16) {
        val block = ByteArray(16)
        aes.decryptBlock(data, i, block, 0)
        for (j in 0 until 16) {
            decrypted[i + j] = (block[j].toInt() xor prevBlock[j].toInt()).toByte()
        }
        prevBlock = data.copyOfRange(i, i + 16)
    }
    
    val paddingLen = decrypted[decrypted.size - 1].toInt() and 0xFF
    if (paddingLen < 1 || paddingLen > 16) return decrypted
    val outputLen = decrypted.size - paddingLen
    val output = ByteArray(outputLen)
    decrypted.copyInto(output, 0, 0, outputLen)
    return output
}

fun String.hexToByteArray(): ByteArray {
    val clean = this.replace(" ", "").lowercase()
    val size = clean.length / 2
    val result = ByteArray(size)
    for (i in 0 until size) {
        val high = clean[i * 2].hexToInt()
        val low = clean[i * 2 + 1].hexToInt()
        result[i] = ((high shl 4) or low).toByte()
    }
    return result
}

private fun Char.hexToInt(): Int {
    return when (this) {
        in '0'..'9' -> this - '0'
        in 'a'..'f' -> this - 'a' + 10
        else -> throw IllegalArgumentException("Invalid hex character: $this")
    }
}

fun ByteArray.readInt32BE(offset: Int): Int {
    return ((this[offset].toInt() and 0xFF) shl 24) or
           ((this[offset + 1].toInt() and 0xFF) shl 16) or
           ((this[offset + 2].toInt() and 0xFF) shl 8) or
           (this[offset + 3].toInt() and 0xFF)
}

fun ByteArray.toHexString(): String {
    val hexChars = "0123456789abcdef"
    val result = StringBuilder(this.size * 2)
    for (b in this) {
        val i = b.toInt() and 0xFF
        result.append(hexChars[i ushr 4])
        result.append(hexChars[i and 0x0F])
    }
    return result.toString()
}

object Base64 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/"
    private val DECODE_TABLE = IntArray(256) { -1 }

    init {
        for (i in ALPHABET.indices) {
            DECODE_TABLE[ALPHABET[i].code] = i
        }
        DECODE_TABLE['='.code] = 0
    }

    fun encode(data: ByteArray): String {
        val result = StringBuilder()
        var i = 0
        while (i < data.size) {
            val b0 = data[i].toInt() and 0xFF
            val b1 = if (i + 1 < data.size) data[i + 1].toInt() and 0xFF else 0
            val b2 = if (i + 2 < data.size) data[i + 2].toInt() and 0xFF else 0

            val c0 = b0 ushr 2
            val c1 = ((b0 and 0x03) shl 4) or (b1 ushr 4)
            val c2 = ((b1 and 0x0F) shl 2) or (b2 ushr 6)
            val c3 = b2 and 0x3F

            result.append(ALPHABET[c0])
            result.append(ALPHABET[c1])
            if (i + 1 < data.size) {
                result.append(ALPHABET[c2])
            } else {
                result.append('=')
            }
            if (i + 2 < data.size) {
                result.append(ALPHABET[c3])
            } else {
                result.append('=')
            }
            i += 3
        }
        return result.toString()
    }

    fun decode(data: String): ByteArray {
        val clean = data.filter { it in ALPHABET }
        val len = clean.length
        val byteLen = (len * 3) / 4
        val result = ByteArray(byteLen)
        var i = 0
        var j = 0
        while (i < len) {
            val c0 = DECODE_TABLE[clean[i].code]
            val c1 = if (i + 1 < len) DECODE_TABLE[clean[i + 1].code] else 0
            val c2 = if (i + 2 < len) DECODE_TABLE[clean[i + 2].code] else 0
            val c3 = if (i + 3 < len) DECODE_TABLE[clean[i + 3].code] else 0

            val b0 = (c0 shl 2) or (c1 ushr 4)
            val b1 = ((c1 and 0x0F) shl 4) or (c2 ushr 2)
            val b2 = ((c2 and 0x03) shl 6) or c3

            if (j < byteLen) result[j++] = b0.toByte()
            if (j < byteLen) result[j++] = b1.toByte()
            if (j < byteLen) result[j++] = b2.toByte()
            i += 4
        }
        return result
    }
}

object SHA256 {
    private val K = longArrayOf(
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
    ).map { it.toInt() }.toIntArray()

    fun hash(message: ByteArray): ByteArray {
        val messageLenBytes = message.size
        val numBlocks = ((messageLenBytes + 8) ushr 6) + 1
        val totalLen = numBlocks shl 6
        val paddingBytes = ByteArray(totalLen - messageLenBytes)
        paddingBytes[0] = 0x80.toByte()

        val messageLenBits = messageLenBytes.toLong() shl 3
        for (i in 0..7) {
            paddingBytes[paddingBytes.size - 8 + i] = (messageLenBits ushr (8 * (7 - i))).toByte()
        }

        var h0 = 0x6a09e667
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab
        var h7 = 0x5be0cd19

        val w = IntArray(64)
        for (i in 0 until numBlocks) {
            val index = i shl 6
            for (j in 0..15) {
                val byteIndex = index + (j shl 2)
                var value = 0
                for (k in 0..3) {
                    val bIdx = byteIndex + k
                    val byteValue = if (bIdx < messageLenBytes) {
                        message[bIdx].toInt() and 0xFF
                    } else {
                        paddingBytes[bIdx - messageLenBytes].toInt() and 0xFF
                    }
                    value = value or (byteValue shl (8 * (3 - k)))
                }
                w[j] = value
            }

            for (j in 16..63) {
                val s0 = rightRotate(w[j - 15], 7) xor rightRotate(w[j - 15], 18) xor (w[j - 15] ushr 3)
                val s1 = rightRotate(w[j - 2], 17) xor rightRotate(w[j - 2], 19) xor (w[j - 2] ushr 10)
                w[j] = w[j - 16] + s0 + w[j - 7] + s1
            }

            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7

            for (j in 0..63) {
                val S1 = rightRotate(e, 6) xor rightRotate(e, 11) xor rightRotate(e, 25)
                val ch = (e and f) xor (e.inv() and g)
                val temp1 = h + S1 + ch + K[j] + w[j]
                val S0 = rightRotate(a, 2) xor rightRotate(a, 13) xor rightRotate(a, 22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val temp2 = S0 + maj

                h = g
                g = f
                f = e
                e = d + temp1
                d = c
                c = b
                b = a
                a = temp1 + temp2
            }

            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h
        }

        val result = ByteArray(32)
        writeInt(result, 0, h0)
        writeInt(result, 4, h1)
        writeInt(result, 8, h2)
        writeInt(result, 12, h3)
        writeInt(result, 16, h4)
        writeInt(result, 20, h5)
        writeInt(result, 24, h6)
        writeInt(result, 28, h7)
        return result
    }

    private fun rightRotate(x: Int, amount: Int): Int {
        return (x ushr amount) or (x shl (32 - amount))
    }

    private fun writeInt(dest: ByteArray, offset: Int, value: Int) {
        dest[offset] = (value ushr 24).toByte()
        dest[offset + 1] = (value ushr 16).toByte()
        dest[offset + 2] = (value ushr 8).toByte()
        dest[offset + 3] = value.toByte()
    }
}

object HMACSHA256 {
    fun sign(key: ByteArray, message: ByteArray): ByteArray {
        var localKey = key
        if (localKey.size > 64) {
            localKey = SHA256.hash(localKey)
        }
        if (localKey.size < 64) {
            val padded = ByteArray(64)
            localKey.copyInto(padded, 0, 0, localKey.size)
            localKey = padded
        }

        val oKeyPad = ByteArray(64)
        val iKeyPad = ByteArray(64)
        for (i in 0..63) {
            oKeyPad[i] = (localKey[i].toInt() xor 0x5c).toByte()
            iKeyPad[i] = (localKey[i].toInt() xor 0x36).toByte()
        }

        val innerHash = SHA256.hash(iKeyPad + message)
        return SHA256.hash(oKeyPad + innerHash)
    }
}

