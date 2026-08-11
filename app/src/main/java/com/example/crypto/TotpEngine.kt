package com.example.crypto

/**
 * Pure, local-only TOTP and HOTP engine compliant with RFC 6238 and RFC 4226.
 *
 * Provides thread-safe, pure functions to generate and verify one-time passwords from
 * Base32 secret keys using HMAC-SHA1, HMAC-SHA256, or HMAC-SHA512.
 */
class TotpEngine {

    companion object {

        /**
         * Generates a TOTP code from a Base32 encoded secret key.
         *
         * @param secretBase32 Raw Base32 encoded secret key string.
         * @param timeSeconds Unix timestamp in seconds (defaults to current system time).
         * @param period Validity duration per code step in seconds (default 30).
         * @param digits Code length (6 or 8 digits).
         * @param algorithm Hash algorithm (SHA1, SHA256, SHA512).
         * @return Formatted zero-padded OTP code string.
         */
        fun generateTotp(
            secretBase32: String,
            timeSeconds: Long = System.currentTimeMillis() / 1000,
            period: Int = 30,
            digits: Int = 6,
            algorithm: HashAlgorithm = HashAlgorithm.SHA1
        ): String {
            val keyBytes = try {
                Base32.decode(secretBase32)
            } catch (e: Exception) {
                return "0".repeat(digits)
            }
            return OtpAlgorithm.generateTotp(
                key = keyBytes,
                timeSeconds = timeSeconds,
                period = period,
                digits = digits,
                algorithm = algorithm
            )
        }

        /**
         * Generates an HOTP code from a Base32 encoded secret key and event counter.
         *
         * @param secretBase32 Raw Base32 encoded secret key string.
         * @param counter Unsigned 64-bit event counter C.
         * @param digits Code length (6 or 8 digits).
         * @param algorithm Hash algorithm (SHA1, SHA256, SHA512).
         * @return Formatted zero-padded OTP code string.
         */
        fun generateHotp(
            secretBase32: String,
            counter: Long,
            digits: Int = 6,
            algorithm: HashAlgorithm = HashAlgorithm.SHA1
        ): String {
            val keyBytes = try {
                Base32.decode(secretBase32)
            } catch (e: Exception) {
                return "0".repeat(digits)
            }
            return OtpAlgorithm.generateHotp(
                key = keyBytes,
                counter = counter,
                digits = digits,
                algorithm = algorithm
            )
        }

        /**
         * Verifies a given user-entered TOTP code against a secret key within a time window.
         *
         * @param secretBase32 Raw Base32 encoded secret key string.
         * @param inputCode The code provided by the user to verify.
         * @param timeSeconds Unix timestamp in seconds to check against.
         * @param windowSteps Allowed clock skew steps (+/- windowSteps). Default is 1 step.
         * @param period Validity duration per step in seconds.
         * @param digits Expected code length.
         * @param algorithm Hash algorithm.
         * @return True if valid within the clock skew window, false otherwise.
         */
        fun verifyTotp(
            secretBase32: String,
            inputCode: String,
            timeSeconds: Long = System.currentTimeMillis() / 1000,
            windowSteps: Int = 1,
            period: Int = 30,
            digits: Int = 6,
            algorithm: HashAlgorithm = HashAlgorithm.SHA1
        ): Boolean {
            val cleanCode = inputCode.trim().replace("\\s+".toRegex(), "")
            if (cleanCode.length != digits) return false

            for (stepOffset in -windowSteps..windowSteps) {
                val checkTime = timeSeconds + (stepOffset * period)
                val expected = generateTotp(
                    secretBase32 = secretBase32,
                    timeSeconds = checkTime,
                    period = period,
                    digits = digits,
                    algorithm = algorithm
                )
                if (expected == cleanCode) {
                    return true
                }
            }
            return false
        }

        /**
         * Returns remaining seconds until the current TOTP step expires.
         */
        fun getRemainingSeconds(
            timeSeconds: Long = System.currentTimeMillis() / 1000,
            period: Int = 30
        ): Int = OtpAlgorithm.getRemainingSeconds(timeSeconds, period)

        /**
         * Returns normalized progress (1.0 down to 0.0) of current time window.
         */
        fun getPeriodProgress(
            timeSeconds: Long = System.currentTimeMillis() / 1000,
            period: Int = 30
        ): Float = OtpAlgorithm.getPeriodProgress(timeSeconds, period)
    }
}
