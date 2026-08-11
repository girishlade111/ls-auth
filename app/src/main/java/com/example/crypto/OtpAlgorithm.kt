package com.example.crypto

import java.nio.ByteBuffer
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

enum class HashAlgorithm(val macName: String, val paramName: String) {
    SHA1("HmacSHA1", "SHA1"),
    SHA256("HmacSHA256", "SHA256"),
    SHA512("HmacSHA512", "SHA512");

    companion object {
        fun fromString(str: String?): HashAlgorithm {
            if (str == null) return SHA1
            val upper = str.uppercase().replace("-", "")
            return when {
                upper.contains("512") -> SHA512
                upper.contains("256") -> SHA256
                else -> SHA1
            }
        }
    }
}

enum class OtpType {
    TOTP, HOTP
}

object OtpAlgorithm {

    /**
     * Computes HOTP code per RFC 4226.
     * @param key Raw key bytes K (Base32 decoded)
     * @param counter Unsigned 64-bit counter C
     * @param digits Code length (6 or 8)
     * @param algorithm HMAC algorithm (SHA1, SHA256, SHA512)
     */
    fun generateHotp(
        key: ByteArray,
        counter: Long,
        digits: Int = 6,
        algorithm: HashAlgorithm = HashAlgorithm.SHA1
    ): String {
        if (key.isEmpty()) return "0".repeat(digits)

        // 1. Encode counter C as 8 bytes, big-endian
        val buffer = ByteBuffer.allocate(8).putLong(counter)
        val counterBytes = buffer.array()

        // 2. HMAC calculation
        val mac = Mac.getInstance(algorithm.macName)
        val macKey = SecretKeySpec(key, algorithm.macName)
        mac.init(macKey)
        val hash = mac.doFinal(counterBytes)

        // 3. Dynamic Truncation
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)

        // 4. Modulo digits
        val modulus = 10.0.pow(digits.toDouble()).toInt()
        val codeInt = binary % modulus

        // 5. Zero-pad on left to exact digit count
        return "%0${digits}d".format(codeInt)
    }

    /**
     * Computes TOTP code per RFC 6238.
     * @param key Raw key bytes K
     * @param timeSeconds Current Unix timestamp in seconds
     * @param period Validity window in seconds (default 30)
     * @param digits Code length (6 or 8)
     * @param algorithm HMAC algorithm
     */
    fun generateTotp(
        key: ByteArray,
        timeSeconds: Long = System.currentTimeMillis() / 1000,
        period: Int = 30,
        digits: Int = 6,
        algorithm: HashAlgorithm = HashAlgorithm.SHA1
    ): String {
        val counter = Math.floorDiv(timeSeconds, period.toLong())
        return generateHotp(key, counter, digits, algorithm)
    }

    /**
     * Calculates seconds remaining in the current TOTP period based on clock.
     */
    fun getRemainingSeconds(
        timeSeconds: Long = System.currentTimeMillis() / 1000,
        period: Int = 30
    ): Int {
        val rem = (timeSeconds % period).toInt()
        val remaining = period - rem
        return if (remaining == 0) period else remaining
    }

    /**
     * Calculates float progress (1.0 down to 0.0) of current time period.
     */
    fun getPeriodProgress(
        timeSeconds: Long = System.currentTimeMillis() / 1000,
        period: Int = 30
    ): Float {
        val remaining = getRemainingSeconds(timeSeconds, period)
        return remaining.toFloat() / period.toFloat()
    }
}
