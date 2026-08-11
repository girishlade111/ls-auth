package com.example

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Pure, local-only TOTP and HOTP engine compliant with RFC 6238 and RFC 4226.
 * Performs Base32 decoding, HMAC-SHA1/256/512 hash calculation, and dynamic truncation.
 * Designed with zero external state or dependencies.
 */
object TotpEngine {

    enum class HashAlgorithm {
        SHA1, SHA256, SHA512
    }

    private val DECODE_TABLE = IntArray(256) { -1 }.apply {
        for (i in 0..25) {
            this['A'.code + i] = i
            this['a'.code + i] = i
        }
        for (i in 0..7) {
            this['2'.code + i] = 26 + i
        }
    }

    /**
     * Decodes a Base32 encoded secret key string into a byte array.
     */
    fun decodeBase32(secretBase32: String): ByteArray {
        val clean = secretBase32.replace("\\s+".toRegex(), "").replace("-", "").trimEnd('=').uppercase()
        if (clean.isEmpty()) return ByteArray(0)

        var currentByte = 0
        var bitsRemaining = 0
        val result = ByteArray((clean.length * 5) / 8)
        var resultIndex = 0

        for (ch in clean) {
            val code = ch.code
            val value = if (code < DECODE_TABLE.size) DECODE_TABLE[code] else -1
            if (value == -1) continue

            currentByte = (currentByte shl 5) or value
            bitsRemaining += 5

            if (bitsRemaining >= 8) {
                bitsRemaining -= 8
                if (resultIndex < result.size) {
                    result[resultIndex++] = (currentByte shr bitsRemaining).toByte()
                }
            }
        }
        return if (resultIndex == result.size) result else result.copyOf(resultIndex)
    }

    /**
     * Computes HMAC bytes for given algorithm, secret key, and counter message buffer.
     */
    fun hmac(algorithm: HashAlgorithm, key: ByteArray, message: ByteArray): ByteArray {
        val macAlg = when (algorithm) {
            HashAlgorithm.SHA1 -> "HmacSHA1"
            HashAlgorithm.SHA256 -> "HmacSHA256"
            HashAlgorithm.SHA512 -> "HmacSHA512"
        }
        val mac = Mac.getInstance(macAlg)
        val secretKeySpec = SecretKeySpec(key, macAlg)
        mac.init(secretKeySpec)
        return mac.doFinal(message)
    }

    /**
     * Dynamic Truncation function as defined in RFC 4226 Section 5.3.
     */
    fun truncate(hash: ByteArray, digits: Int = 6): String {
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        val modulus = when (digits) {
            8 -> 100_000_000
            else -> 1_000_000
        }

        val otp = binary % modulus
        return String.format("%0${digits}d", otp)
    }

    /**
     * Generates an HOTP value for a specific event counter (RFC 4226).
     */
    fun generateHotp(
        secretBase32: String,
        counter: Long,
        digits: Int = 6,
        algorithm: HashAlgorithm = HashAlgorithm.SHA1
    ): String {
        val key = decodeBase32(secretBase32)
        if (key.isEmpty()) return "0".repeat(digits)

        val buffer = ByteArray(8)
        var tempCounter = counter
        for (i in 7 downTo 0) {
            buffer[i] = (tempCounter and 0xFF).toByte()
            tempCounter = tempCounter ushr 8
        }

        val hmacResult = hmac(algorithm, key, buffer)
        return truncate(hmacResult, digits)
    }

    /**
     * Generates a TOTP value for a specific Unix timestamp (RFC 6238).
     */
    fun generateTotp(
        secretBase32: String,
        timeSeconds: Long = System.currentTimeMillis() / 1000,
        period: Int = 30,
        digits: Int = 6,
        algorithm: HashAlgorithm = HashAlgorithm.SHA1
    ): String {
        val timeStep = timeSeconds / period
        return generateHotp(secretBase32, timeStep, digits, algorithm)
    }
}
