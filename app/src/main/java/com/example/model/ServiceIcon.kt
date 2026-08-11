package com.example.model

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

import com.example.util.MonogramGenerator

enum class ServiceBrand(val brandName: String, val aliases: List<String>, val brandColor: Color) {
    GOOGLE("Google", listOf("google", "google.com", "google inc", "gmail", "youtube"), Color(0xFF4285F4)),
    MICROSOFT("Microsoft", listOf("microsoft", "outlook", "live", "azure", "office365", "msft"), Color(0xFF00A4EF)),
    GITHUB("GitHub", listOf("github", "github.com"), Color(0xFF24292E)),
    AMAZON("Amazon", listOf("amazon", "aws", "amazon.com", "amazon web services"), Color(0xFFFF9900)),
    META("Meta", listOf("facebook", "meta", "fb", "meta.com", "instagram", "whatsapp"), Color(0xFF0668E1)),
    DISCORD("Discord", listOf("discord", "discordapp", "discord.com"), Color(0xFF5865F2)),
    DROPBOX("Dropbox", listOf("dropbox", "dropbox.com"), Color(0xFF0061FE)),
    PAYPAL("PayPal", listOf("paypal", "paypal.com"), Color(0xFF003087)),
    STEAM("Steam", listOf("steam", "steampowered"), Color(0xFF171A21)),
    TWITTER("X / Twitter", listOf("twitter", "x.com", "x", "twitter.com"), Color(0xFF1DA1F2)),
    LINKEDIN("LinkedIn", listOf("linkedin", "linkedin.com"), Color(0xFF0A66C2)),
    REDDIT("Reddit", listOf("reddit", "reddit.com"), Color(0xFFFF4500)),
    SLACK("Slack", listOf("slack", "slack.com"), Color(0xFF4A154B)),
    APPLE("Apple", listOf("apple", "apple.com", "icloud"), Color(0xFF000000)),
    SPOTIFY("Spotify", listOf("spotify", "spotify.com"), Color(0xFF1DB954)),
    TWITCH("Twitch", listOf("twitch", "twitch.tv"), Color(0xFF9146FF)),
    CRYPTO("Crypto", listOf("binance", "coinbase", "kraken", "crypto", "metamask", "bitbuy"), Color(0xFFF0B90B)),
    BANK("Banking", listOf("bank", "chase", "capitalone", "wells fargo", "citi", "fidelity", "vanguard"), Color(0xFF059669)),
    GENERIC("Generic", emptyList(), Color(0xFF6366F1));

    companion object {
        fun match(issuer: String): ServiceBrand {
            if (issuer.isBlank()) return GENERIC
            val clean = issuer.trim().lowercase()

            for (brand in entries) {
                if (brand == GENERIC) continue
                if (brand.aliases.any { clean.contains(it) }) {
                    return brand
                }
            }
            return GENERIC
        }
    }
}

object MonogramColorResolver {
    fun getColor(issuer: String): Color = MonogramGenerator.generateBackgroundColor(issuer)
    fun getInitials(name: String): String = MonogramGenerator.generateInitials(name)
}
