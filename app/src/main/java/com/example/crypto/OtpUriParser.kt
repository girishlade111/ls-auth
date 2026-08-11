package com.example.crypto

import android.net.Uri
import java.net.URLDecoder

data class ParsedOtpUri(
    val type: OtpType,
    val secretBase32: String,
    val secretBytes: ByteArray,
    val accountName: String,
    val issuer: String,
    val algorithm: HashAlgorithm,
    val digits: Int,
    val period: Int,
    val counter: Long
)

object OtpUriParser {

    /**
     * Parses an otpauth:// URI string into ParsedOtpUri structure.
     * Throws IllegalArgumentException if URI is invalid.
     */
    fun parse(uriString: String): ParsedOtpUri {
        val trimmed = uriString.trim()
        if (!trimmed.startsWith("otpauth://", ignoreCase = true)) {
            throw IllegalArgumentException("Not a 2FA setup URI (must start with otpauth://)")
        }

        val uri = Uri.parse(trimmed)
        val host = uri.host?.lowercase() ?: throw IllegalArgumentException("Missing OTP type in URI")

        val type = when (host) {
            "totp" -> OtpType.TOTP
            "hotp" -> OtpType.HOTP
            else -> throw IllegalArgumentException("Unsupported OTP type: '$host'")
        }

        // Parse path/label
        val rawPath = uri.path?.trimStart('/') ?: ""
        val decodedLabel = try {
            URLDecoder.decode(rawPath, "UTF-8")
        } catch (e: Exception) {
            rawPath
        }

        var labelIssuer = ""
        var accountName = decodedLabel

        if (decodedLabel.contains(":")) {
            val parts = decodedLabel.split(":", limit = 2)
            labelIssuer = parts[0].trim()
            accountName = parts[1].trim()
        }

        // Secret parameter
        val secretParam = uri.getQueryParameter("secret")
            ?: throw IllegalArgumentException("Missing required 'secret' parameter in URI")

        val cleanSecret = secretParam.replace("\\s+".toRegex(), "").replace("-", "").uppercase()
        val secretBytes = try {
            Base32.decode(cleanSecret)
        } catch (e: Exception) {
            throw IllegalArgumentException("Invalid Base32 secret key: ${e.message}")
        }

        // Issuer parameter (prefer explicit query param over label issuer prefix)
        val queryIssuer = uri.getQueryParameter("issuer")?.let {
            try { URLDecoder.decode(it, "UTF-8") } catch (e: Exception) { it }
        }
        val finalIssuer = when {
            !queryIssuer.isNull0orBlank() -> queryIssuer!!
            labelIssuer.isNotBlank() -> labelIssuer
            else -> ""
        }

        // Algorithm
        val algoParam = uri.getQueryParameter("algorithm")
        val algorithm = HashAlgorithm.fromString(algoParam)

        // Digits
        val digitsParam = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6
        val digits = if (digitsParam == 8) 8 else 6

        // Period
        val periodParam = uri.getQueryParameter("period")?.toIntOrNull() ?: 30
        val period = if (periodParam > 0) periodParam else 30

        // Counter
        val counterParam = uri.getQueryParameter("counter")?.toLongOrNull() ?: 0L
        val counter = if (counterParam >= 0) counterParam else 0L

        return ParsedOtpUri(
            type = type,
            secretBase32 = cleanSecret,
            secretBytes = secretBytes,
            accountName = accountName.ifBlank { "Account" },
            issuer = finalIssuer,
            algorithm = algorithm,
            digits = digits,
            period = period,
            counter = counter
        )
    }

    private fun String?.isNull0orBlank(): Boolean = this == null || this.isBlank()
}
