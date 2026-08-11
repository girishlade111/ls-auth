package com.example.crypto

import java.io.ByteArrayOutputStream

/**
 * RFC 4648 Base32 Encoder and Decoder for 2FA Secret Keys.
 */
object Base32 {
    private const val ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"
    private val DECODE_TABLE = IntArray(256) { -1 }.apply {
        for (i in ALPHABET.indices) {
            this[ALPHABET[i].code] = i
            this[ALPHABET[i].lowercaseChar().code] = i
        }
    }

    /**
     * Checks if a string is a valid Base32 representation.
     */
    fun isValidBase32(base32: String): Boolean {
        val cleanInput = base32.replace("\\s+".toRegex(), "").replace("-", "").trimEnd('=')
        if (cleanInput.isEmpty()) return false
        for (char in cleanInput) {
            val charCode = char.code
            val value = if (charCode < DECODE_TABLE.size) DECODE_TABLE[charCode] else -1
            if (value == -1) return false
        }
        return true
    }

    /**
     * Decodes a Base32 string to raw key bytes.
     * Normalizes input by stripping spaces, hyphens, '=' padding, and uppercasing.
     * Rejects any invalid characters with a clear exception.
     */
    fun decode(base32: String): ByteArray {
        val cleanInput = base32.replace("\\s+".toRegex(), "")
            .replace("-", "")
            .trimEnd('=')

        if (cleanInput.isEmpty()) {
            return ByteArray(0)
        }

        val out = ByteArrayOutputStream()
        var buffer = 0
        var bitsLeft = 0

        for (char in cleanInput) {
            val charCode = char.code
            val value = if (charCode < DECODE_TABLE.size) DECODE_TABLE[charCode] else -1
            if (value == -1) {
                throw IllegalArgumentException("Invalid Base32 character: '$char'")
            }

            buffer = (buffer shl 5) or value
            bitsLeft += 5

            if (bitsLeft >= 8) {
                out.write((buffer shr (bitsLeft - 8)) and 0xFF)
                bitsLeft -= 8
            }
        }

        return out.toByteArray()
    }

    /**
     * Encodes raw byte array into Base32 string (without padding).
     */
    fun encode(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""
        val result = StringBuilder()
        var buffer = 0
        var bitsLeft = 0

        for (b in bytes) {
            buffer = (buffer shl 8) or (b.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                val index = (buffer shr (bitsLeft - 5)) and 0x1F
                result.append(ALPHABET[index])
                bitsLeft -= 5
            }
        }

        if (bitsLeft > 0) {
            val index = (buffer shl (5 - bitsLeft)) and 0x1F
            result.append(ALPHABET[index])
        }

        return result.toString()
    }
}
