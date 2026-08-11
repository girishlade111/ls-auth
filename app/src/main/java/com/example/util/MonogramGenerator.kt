package com.example.util

import androidx.compose.ui.graphics.Color
import com.example.model.Account
import kotlin.math.abs

/**
 * Data class representing a generated Monogram Icon specification with initials and background color.
 */
data class MonogramIconSpec(
    val initials: String,
    val backgroundColor: Color,
    val textColor: Color = Color.White
)

/**
 * Utility to generate deterministic monogram icons (initials with a background color
 * based on the issuer or account name hash) for accounts that do not have a custom icon assigned.
 */
object MonogramGenerator {

    /**
     * Curated, modern Material 3 inspired color palette for deterministic background selection.
     */
    val PALETTE = listOf(
        Color(0xFF6366F1), // Indigo
        Color(0xFF0284C7), // Sky Blue
        Color(0xFF0D9488), // Teal
        Color(0xFF16A34A), // Green
        Color(0xFFD97706), // Amber
        Color(0xFFDC2626), // Red
        Color(0xFF9333EA), // Purple
        Color(0xFFDB2777), // Pink
        Color(0xFF0891B2), // Cyan
        Color(0xFF4F46E5), // Violet
        Color(0xFF2563EB), // Blue
        Color(0xFF059669)  // Emerald
    )

    /**
     * Checks if an account has a custom icon assigned via its iconKey.
     */
    fun hasCustomIcon(account: Account): Boolean {
        return !account.iconKey.isNullOrBlank()
    }

    /**
     * Checks if an iconKey string specifies a custom icon.
     */
    fun hasCustomIcon(iconKey: String?): Boolean {
        return !iconKey.isNullOrBlank()
    }

    /**
     * Generates a deterministic background color based on the hash of the issuer or name.
     */
    fun generateBackgroundColor(issuerOrName: String): Color {
        val clean = issuerOrName.trim().lowercase()
        if (clean.isEmpty()) return PALETTE[0]

        val hash = abs(clean.hashCode())
        val index = hash % PALETTE.size
        return PALETTE[index]
    }

    /**
     * Generates 1 to 2 uppercase initials from an issuer or account name.
     */
    fun generateInitials(issuerOrName: String): String {
        val clean = issuerOrName.trim()
            .replace(Regex("https?://"), "")
            .replace(Regex("www\\."), "")
            .replace(Regex("\\.(com|org|net|io|co|app|dev|gov|edu|ai)"), "")
            .trim()

        if (clean.isEmpty()) return "?"

        val words = clean.split(Regex("[\\s._\\-]+")).filter { it.isNotBlank() }

        return when {
            words.size >= 2 -> {
                val first = words[0].firstOrNull { it.isLetterOrDigit() } ?: '?'
                val second = words[1].firstOrNull { it.isLetterOrDigit() } ?: '?'
                "$first$second".uppercase()
            }
            words.size == 1 -> {
                val word = words[0]
                val uppercaseLetters = word.filter { it.isUpperCase() }
                if (uppercaseLetters.length >= 2) {
                    "${uppercaseLetters[0]}${uppercaseLetters[1]}".uppercase()
                } else if (word.isNotEmpty()) {
                    "${word[0]}".uppercase()
                } else {
                    "?"
                }
            }
            else -> "?"
        }
    }

    /**
     * Generates a complete MonogramIconSpec (initials & deterministic color) for an issuer & account name.
     */
    fun generateMonogram(issuer: String, accountName: String = ""): MonogramIconSpec {
        val primaryText = issuer.ifBlank { accountName }
        val initials = generateInitials(primaryText)
        val bgColor = generateBackgroundColor(primaryText)

        return MonogramIconSpec(
            initials = initials,
            backgroundColor = bgColor,
            textColor = Color.White
        )
    }

    /**
     * Generates a complete MonogramIconSpec for an Account model.
     */
    fun generateMonogram(account: Account): MonogramIconSpec {
        return generateMonogram(account.issuer, account.accountName)
    }
}
