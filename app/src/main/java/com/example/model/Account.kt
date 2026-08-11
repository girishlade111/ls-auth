package com.example.model

import com.example.crypto.Base32
import com.example.crypto.HashAlgorithm
import com.example.crypto.OtpAlgorithm
import com.example.crypto.OtpType
import com.example.data.AccountEntity

data class Account(
    val id: Long = 0,
    val accountName: String,
    val issuer: String,
    val secretBase32: String,
    val type: OtpType = OtpType.TOTP,
    val algorithm: HashAlgorithm = HashAlgorithm.SHA1,
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Long = 0L,
    val folderId: Long? = null,
    val iconKey: String? = null,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Generates the current code string.
     */
    fun getCurrentCode(timeSeconds: Long = System.currentTimeMillis() / 1000): String {
        return try {
            val keyBytes = Base32.decode(secretBase32)
            if (type == OtpType.TOTP) {
                OtpAlgorithm.generateTotp(
                    key = keyBytes,
                    timeSeconds = timeSeconds,
                    period = period,
                    digits = digits,
                    algorithm = algorithm
                )
            } else {
                OtpAlgorithm.generateHotp(
                    key = keyBytes,
                    counter = counter,
                    digits = digits,
                    algorithm = algorithm
                )
            }
        } catch (e: Exception) {
            "0".repeat(digits)
        }
    }

    /**
     * Formats code with spaces for visual scanability (e.g., "123 456" or "1234 5678").
     */
    fun getFormattedCode(timeSeconds: Long = System.currentTimeMillis() / 1000): String {
        val rawCode = getCurrentCode(timeSeconds)
        return if (rawCode.length == 6) {
            "${rawCode.substring(0, 3)} ${rawCode.substring(3)}"
        } else if (rawCode.length == 8) {
            "${rawCode.substring(0, 4)} ${rawCode.substring(4)}"
        } else {
            rawCode
        }
    }

    fun getRemainingSeconds(timeSeconds: Long = System.currentTimeMillis() / 1000): Int {
        return if (type == OtpType.TOTP) {
            OtpAlgorithm.getRemainingSeconds(timeSeconds, period)
        } else {
            period
        }
    }

    fun getPeriodProgress(timeSeconds: Long = System.currentTimeMillis() / 1000): Float {
        return if (type == OtpType.TOTP) {
            OtpAlgorithm.getPeriodProgress(timeSeconds, period)
        } else {
            1.0f
        }
    }

    fun toOtpAuthUri(): String {
        val encodedLabel = if (issuer.isNotBlank()) {
            android.net.Uri.encode(issuer) + ":" + android.net.Uri.encode(accountName)
        } else {
            android.net.Uri.encode(accountName)
        }
        val builder = android.net.Uri.Builder()
            .scheme("otpauth")
            .authority(type.name.lowercase())
            .path(encodedLabel)
            .appendQueryParameter("secret", secretBase32)

        if (issuer.isNotBlank()) {
            builder.appendQueryParameter("issuer", issuer)
        }
        builder.appendQueryParameter("algorithm", algorithm.name)
        builder.appendQueryParameter("digits", digits.toString())
        builder.appendQueryParameter("period", period.toString())

        if (type == OtpType.HOTP) {
            builder.appendQueryParameter("counter", counter.toString())
        }

        return builder.build().toString()
    }
}
