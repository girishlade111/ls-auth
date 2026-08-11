package com.example.crypto

import android.util.Base64
import java.io.ByteArrayInputStream

data class MigratedAccount(
    val secretBase32: String,
    val secretRaw: ByteArray,
    val name: String,
    val issuer: String,
    val algorithm: HashAlgorithm,
    val digits: Int,
    val type: OtpType,
    val counter: Long
)

object GoogleAuthMigrationParser {

    /**
     * Parses otpauth-migration://offline?data=... URL
     */
    fun parseMigrationUrl(url: String): List<MigratedAccount> {
        val trimmed = url.trim()
        if (!trimmed.startsWith("otpauth-migration://", ignoreCase = true)) {
            throw IllegalArgumentException("Invalid scheme for migration URL")
        }

        val dataIndex = trimmed.indexOf("data=")
        if (dataIndex == -1) {
            throw IllegalArgumentException("Missing data parameter in migration URL")
        }

        var dataEncoded = trimmed.substring(dataIndex + 5)
        val ampersandIndex = dataEncoded.indexOf('&')
        if (ampersandIndex != -1) {
            dataEncoded = dataEncoded.substring(0, ampersandIndex)
        }

        val decodedData = try {
            Base64.decode(dataEncoded, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
        } catch (e: Exception) {
            try {
                Base64.decode(dataEncoded, Base64.DEFAULT)
            } catch (e2: Exception) {
                throw IllegalArgumentException("Failed to decode Base64 migration payload: ${e.message}")
            }
        }

        return parsePayload(decodedData)
    }

    private fun parsePayload(data: ByteArray): List<MigratedAccount> {
        val stream = ByteArrayInputStream(data)
        val accounts = mutableListOf<MigratedAccount>()

        while (stream.available() > 0) {
            val key = readVarint(stream)
            val fieldNumber = (key shr 3).toInt()
            val wireType = (key and 0x07).toInt()

            if (fieldNumber == 1 && wireType == 2) {
                // otp_parameters repeated message
                val length = readVarint(stream).toInt()
                val msgBytes = ByteArray(length)
                val read = stream.read(msgBytes)
                if (read == length) {
                    val account = parseOtpParameters(msgBytes)
                    if (account != null) {
                        accounts.add(account)
                    }
                }
            } else {
                skipField(wireType, stream)
            }
        }

        return accounts
    }

    private fun parseOtpParameters(bytes: ByteArray): MigratedAccount? {
        val stream = ByteArrayInputStream(bytes)
        var secretRaw = ByteArray(0)
        var name = ""
        var issuer = ""
        var algorithm = HashAlgorithm.SHA1
        var digits = 6
        var type = OtpType.TOTP
        var counter = 0L

        while (stream.available() > 0) {
            val key = readVarint(stream)
            val fieldNumber = (key shr 3).toInt()
            val wireType = (key and 0x07).toInt()

            when (fieldNumber) {
                1 -> { // secret bytes
                    if (wireType == 2) {
                        val len = readVarint(stream).toInt()
                        secretRaw = ByteArray(len)
                        stream.read(secretRaw)
                    } else skipField(wireType, stream)
                }
                2 -> { // name
                    if (wireType == 2) {
                        val len = readVarint(stream).toInt()
                        val b = ByteArray(len)
                        stream.read(b)
                        name = String(b, Charsets.UTF_8)
                    } else skipField(wireType, stream)
                }
                3 -> { // issuer
                    if (wireType == 2) {
                        val len = readVarint(stream).toInt()
                        val b = ByteArray(len)
                        stream.read(b)
                        issuer = String(b, Charsets.UTF_8)
                    } else skipField(wireType, stream)
                }
                4 -> { // algorithm enum: 1=SHA1, 2=SHA256, 3=SHA512
                    if (wireType == 0) {
                        val v = readVarint(stream).toInt()
                        algorithm = when (v) {
                            2 -> HashAlgorithm.SHA256
                            3 -> HashAlgorithm.SHA512
                            else -> HashAlgorithm.SHA1
                        }
                    } else skipField(wireType, stream)
                }
                5 -> { // digits enum: 1=6 digits, 2=8 digits
                    if (wireType == 0) {
                        val v = readVarint(stream).toInt()
                        digits = if (v == 2) 8 else 6
                    } else skipField(wireType, stream)
                }
                6 -> { // type enum: 1=HOTP, 2=TOTP
                    if (wireType == 0) {
                        val v = readVarint(stream).toInt()
                        type = if (v == 1) OtpType.HOTP else OtpType.TOTP
                    } else skipField(wireType, stream)
                }
                7 -> { // counter
                    if (wireType == 0) {
                        counter = readVarint(stream)
                    } else skipField(wireType, stream)
                }
                else -> skipField(wireType, stream)
            }
        }

        if (secretRaw.isEmpty()) return null

        val secretBase32 = Base32.encode(secretRaw)

        // Parse issuer from label if missing in parameters
        var finalIssuer = issuer
        var finalName = name
        if (finalIssuer.isBlank() && name.contains(":")) {
            val parts = name.split(":", limit = 2)
            finalIssuer = parts[0].trim()
            finalName = parts[1].trim()
        }

        return MigratedAccount(
            secretBase32 = secretBase32,
            secretRaw = secretRaw,
            name = finalName.ifBlank { "Account" },
            issuer = finalIssuer,
            algorithm = algorithm,
            digits = digits,
            type = type,
            counter = counter
        )
    }

    private fun readVarint(stream: ByteArrayInputStream): Long {
        var result = 0L
        var shift = 0
        while (stream.available() > 0) {
            val b = stream.read()
            if (b == -1) break
            result = result or ((b and 0x7F).toLong() shl shift)
            if ((b and 0x80) == 0) break
            shift += 7
            if (shift >= 64) break
        }
        return result
    }

    private fun skipField(wireType: Int, stream: ByteArrayInputStream) {
        when (wireType) {
            0 -> readVarint(stream)
            1 -> stream.skip(8)
            2 -> {
                val len = readVarint(stream).toInt()
                stream.skip(len.toLong())
            }
            5 -> stream.skip(4)
            else -> {}
        }
    }
}
