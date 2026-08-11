package com.example.util

import java.security.SecureRandom

object PasswordGenerator {

    private val UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray() // Excluded O, I for ambiguity if toggled
    private val UPPER_ALL = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray()
    private val LOWER = "abcdefghijkmnopqrstuvwxyz".toCharArray() // Excluded l for ambiguity
    private val LOWER_ALL = "abcdefghijklmnopqrstuvwxyz".toCharArray()
    private val DIGITS = "23456789".toCharArray() // Excluded 0, 1 for ambiguity
    private val DIGITS_ALL = "0123456789".toCharArray()
    private val SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?".toCharArray()

    data class GeneratorOptions(
        val length: Int = 16,
        val includeUppercase: Boolean = true,
        val includeLowercase: Boolean = true,
        val includeNumbers: Boolean = true,
        val includeSymbols: Boolean = true,
        val avoidAmbiguous: Boolean = true
    )

    enum class PasswordStrength(val label: String, val score: Int) {
        WEAK("Weak", 1),
        FAIR("Fair", 2),
        STRONG("Strong", 3),
        ULTRA("Ultra Secure", 4)
    }

    fun generate(options: GeneratorOptions): String {
        val random = SecureRandom()
        val pools = mutableListOf<CharArray>()

        val u = if (options.avoidAmbiguous) UPPER else UPPER_ALL
        val l = if (options.avoidAmbiguous) LOWER else LOWER_ALL
        val d = if (options.avoidAmbiguous) DIGITS else DIGITS_ALL
        val s = SYMBOLS

        if (options.includeUppercase) pools.add(u)
        if (options.includeLowercase) pools.add(l)
        if (options.includeNumbers) pools.add(d)
        if (options.includeSymbols) pools.add(s)

        if (pools.isEmpty()) pools.add(l) // Fallback to lowercase

        val passwordChars = CharArray(options.length)
        
        // Ensure at least 1 character from each selected pool
        var charIndex = 0
        pools.forEach { pool ->
            if (charIndex < options.length) {
                passwordChars[charIndex++] = pool[random.nextInt(pool.size)]
            }
        }

        // Fill remaining positions
        val combinedPool = pools.flatMap { it.toList() }.toCharArray()
        while (charIndex < options.length) {
            passwordChars[charIndex++] = combinedPool[random.nextInt(combinedPool.size)]
        }

        // Shuffle
        for (i in passwordChars.indices) {
            val j = random.nextInt(passwordChars.size)
            val temp = passwordChars[i]
            passwordChars[i] = passwordChars[j]
            passwordChars[j] = temp
        }

        return String(passwordChars)
    }

    fun calculateStrength(password: String): PasswordStrength {
        if (password.length < 8) return PasswordStrength.WEAK

        var poolSize = 0
        if (password.any { it.isUpperCase() }) poolSize += 26
        if (password.any { it.isLowerCase() }) poolSize += 26
        if (password.any { it.isDigit() }) poolSize += 10
        if (password.any { !it.isLetterOrDigit() }) poolSize += 30

        if (poolSize == 0) return PasswordStrength.WEAK

        val entropy = password.length * (Math.log(poolSize.toDouble()) / Math.log(2.0))

        return when {
            entropy < 40 -> PasswordStrength.WEAK
            entropy < 65 -> PasswordStrength.FAIR
            entropy < 90 -> PasswordStrength.STRONG
            else -> PasswordStrength.ULTRA
        }
    }
}
