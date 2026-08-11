package com.example.model

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.util.BrandVectors

data class AppIconItem(
    val id: String,
    val name: String,
    val category: IconCategory,
    val brandColor: Color,
    val aliases: List<String>,
    val iconVector: ImageVector
)

enum class IconCategory(val title: String) {
    POPULAR("Popular Services"),
    TECH("Tech & Cloud"),
    SOCIAL("Social & Messaging"),
    GAMING("Gaming & Esports"),
    FINANCE("Finance & Crypto"),
    DEV("Developer & Productivity"),
    SECURITY("Security & Passwords"),
    ENTERTAINMENT("Media & Streaming"),
    SHOPPING("Shopping & Services"),
    GENERAL("General Icons")
}

object AppIconPack {

    val items: List<AppIconItem> = listOf(
        // POPULAR & FEATURED
        AppIconItem(
            id = "google",
            name = "Google",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFF4285F4),
            aliases = listOf("google", "gmail", "youtube", "drive", "google.com"),
            iconVector = BrandVectors.Google
        ),
        AppIconItem(
            id = "microsoft",
            name = "Microsoft",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFF00A4EF),
            aliases = listOf("microsoft", "outlook", "azure", "office365", "live", "msft"),
            iconVector = BrandVectors.Microsoft
        ),
        AppIconItem(
            id = "github",
            name = "GitHub",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFF24292E),
            aliases = listOf("github", "github.com"),
            iconVector = BrandVectors.GitHub
        ),
        AppIconItem(
            id = "apple",
            name = "Apple",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFF1C1C1E),
            aliases = listOf("apple", "icloud", "apple.com"),
            iconVector = BrandVectors.Apple
        ),
        AppIconItem(
            id = "aws",
            name = "Amazon Web Services",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFFFF9900),
            aliases = listOf("amazon", "aws", "amazon web services"),
            iconVector = BrandVectors.AmazonAws
        ),
        AppIconItem(
            id = "facebook",
            name = "Meta / Facebook",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFF0668E1),
            aliases = listOf("facebook", "meta", "fb"),
            iconVector = BrandVectors.MetaFacebook
        ),
        AppIconItem(
            id = "discord",
            name = "Discord",
            category = IconCategory.POPULAR,
            brandColor = Color(0xFF5865F2),
            aliases = listOf("discord", "discordapp"),
            iconVector = BrandVectors.Discord
        ),

        // TECH & CLOUD
        AppIconItem(
            id = "cloudflare",
            name = "Cloudflare",
            category = IconCategory.TECH,
            brandColor = Color(0xFFF38020),
            aliases = listOf("cloudflare"),
            iconVector = BrandVectors.Cloudflare
        ),
        AppIconItem(
            id = "digitalocean",
            name = "DigitalOcean",
            category = IconCategory.TECH,
            brandColor = Color(0xFF0080FF),
            aliases = listOf("digitalocean"),
            iconVector = BrandVectors.DigitalOcean
        ),
        AppIconItem(
            id = "heroku",
            name = "Heroku",
            category = IconCategory.TECH,
            brandColor = Color(0xFF430098),
            aliases = listOf("heroku"),
            iconVector = BrandVectors.AmazonAws
        ),
        AppIconItem(
            id = "yahoo",
            name = "Yahoo",
            category = IconCategory.TECH,
            brandColor = Color(0xFF6001D2),
            aliases = listOf("yahoo"),
            iconVector = BrandVectors.Google
        ),

        // SOCIAL & MESSAGING
        AppIconItem(
            id = "instagram",
            name = "Instagram",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFFE4405F),
            aliases = listOf("instagram", "ig"),
            iconVector = BrandVectors.Instagram
        ),
        AppIconItem(
            id = "whatsapp",
            name = "WhatsApp",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFF25D366),
            aliases = listOf("whatsapp"),
            iconVector = BrandVectors.WhatsApp
        ),
        AppIconItem(
            id = "twitter",
            name = "X / Twitter",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFF1DA1F2),
            aliases = listOf("twitter", "x.com", "x"),
            iconVector = BrandVectors.XTwitter
        ),
        AppIconItem(
            id = "telegram",
            name = "Telegram",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFF26A5E4),
            aliases = listOf("telegram", "t.me"),
            iconVector = BrandVectors.Telegram
        ),
        AppIconItem(
            id = "reddit",
            name = "Reddit",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFFFF4500),
            aliases = listOf("reddit"),
            iconVector = BrandVectors.Reddit
        ),
        AppIconItem(
            id = "linkedin",
            name = "LinkedIn",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFF0A66C2),
            aliases = listOf("linkedin"),
            iconVector = BrandVectors.LinkedIn
        ),
        AppIconItem(
            id = "snapchat",
            name = "Snapchat",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFFE5C100),
            aliases = listOf("snapchat", "snap"),
            iconVector = BrandVectors.Snapchat
        ),
        AppIconItem(
            id = "tiktok",
            name = "TikTok",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFF000000),
            aliases = listOf("tiktok"),
            iconVector = BrandVectors.TikTok
        ),
        AppIconItem(
            id = "pinterest",
            name = "Pinterest",
            category = IconCategory.SOCIAL,
            brandColor = Color(0xFFBD081C),
            aliases = listOf("pinterest"),
            iconVector = BrandVectors.Pinterest
        ),

        // GAMING & ESPORTS
        AppIconItem(
            id = "steam",
            name = "Steam",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF171A21),
            aliases = listOf("steam", "steampowered"),
            iconVector = BrandVectors.Steam
        ),
        AppIconItem(
            id = "epicgames",
            name = "Epic Games",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF313131),
            aliases = listOf("epic", "epic games", "fortnite"),
            iconVector = BrandVectors.EpicGames
        ),
        AppIconItem(
            id = "playstation",
            name = "PlayStation",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF003791),
            aliases = listOf("playstation", "psn", "sony"),
            iconVector = BrandVectors.PlayStation
        ),
        AppIconItem(
            id = "xbox",
            name = "Xbox",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF107C41),
            aliases = listOf("xbox", "xbox live"),
            iconVector = BrandVectors.Xbox
        ),
        AppIconItem(
            id = "nintendo",
            name = "Nintendo",
            category = IconCategory.GAMING,
            brandColor = Color(0xFFE60012),
            aliases = listOf("nintendo", "switch"),
            iconVector = BrandVectors.Nintendo
        ),
        AppIconItem(
            id = "roblox",
            name = "Roblox",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF000000),
            aliases = listOf("roblox"),
            iconVector = BrandVectors.Roblox
        ),
        AppIconItem(
            id = "ea",
            name = "EA / Origin",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF1A1A1A),
            aliases = listOf("ea", "origin", "electronic arts"),
            iconVector = BrandVectors.EpicGames
        ),
        AppIconItem(
            id = "ubisoft",
            name = "Ubisoft",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF000000),
            aliases = listOf("ubisoft", "uplay"),
            iconVector = BrandVectors.Xbox
        ),
        AppIconItem(
            id = "twitch",
            name = "Twitch",
            category = IconCategory.GAMING,
            brandColor = Color(0xFF9146FF),
            aliases = listOf("twitch", "twitch.tv"),
            iconVector = BrandVectors.Twitch
        ),

        // DEVELOPER & PRODUCTIVITY
        AppIconItem(
            id = "gitlab",
            name = "GitLab",
            category = IconCategory.DEV,
            brandColor = Color(0xFFFC6D26),
            aliases = listOf("gitlab"),
            iconVector = BrandVectors.GitLab
        ),
        AppIconItem(
            id = "bitbucket",
            name = "Bitbucket",
            category = IconCategory.DEV,
            brandColor = Color(0xFF0052CC),
            aliases = listOf("bitbucket", "atlassian"),
            iconVector = BrandVectors.Bitbucket
        ),
        AppIconItem(
            id = "notion",
            name = "Notion",
            category = IconCategory.DEV,
            brandColor = Color(0xFF111111),
            aliases = listOf("notion"),
            iconVector = BrandVectors.Notion
        ),
        AppIconItem(
            id = "slack",
            name = "Slack",
            category = IconCategory.DEV,
            brandColor = Color(0xFF4A154B),
            aliases = listOf("slack"),
            iconVector = BrandVectors.Slack
        ),
        AppIconItem(
            id = "jetbrains",
            name = "JetBrains",
            category = IconCategory.DEV,
            brandColor = Color(0xFF000000),
            aliases = listOf("jetbrains", "intellij"),
            iconVector = BrandVectors.JetBrains
        ),
        AppIconItem(
            id = "openai",
            name = "OpenAI / ChatGPT",
            category = IconCategory.DEV,
            brandColor = Color(0xFF10A37F),
            aliases = listOf("openai", "chatgpt"),
            iconVector = BrandVectors.OpenAi
        ),
        AppIconItem(
            id = "claude",
            name = "Anthropic / Claude",
            category = IconCategory.DEV,
            brandColor = Color(0xFFD97757),
            aliases = listOf("anthropic", "claude"),
            iconVector = BrandVectors.Claude
        ),
        AppIconItem(
            id = "adobe",
            name = "Adobe",
            category = IconCategory.DEV,
            brandColor = Color(0xFFFF0000),
            aliases = listOf("adobe", "creative cloud"),
            iconVector = BrandVectors.Adobe
        ),
        AppIconItem(
            id = "salesforce",
            name = "Salesforce",
            category = IconCategory.DEV,
            brandColor = Color(0xFF00A1E0),
            aliases = listOf("salesforce"),
            iconVector = BrandVectors.Salesforce
        ),
        AppIconItem(
            id = "zoom",
            name = "Zoom",
            category = IconCategory.DEV,
            brandColor = Color(0xFF2D8CFF),
            aliases = listOf("zoom"),
            iconVector = BrandVectors.Zoom
        ),
        AppIconItem(
            id = "workday",
            name = "Workday",
            category = IconCategory.DEV,
            brandColor = Color(0xFF005CB9),
            aliases = listOf("workday"),
            iconVector = BrandVectors.Microsoft
        ),

        // FINANCE & CRYPTO
        AppIconItem(
            id = "binance",
            name = "Binance",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFFF0B90B),
            aliases = listOf("binance", "bnb"),
            iconVector = BrandVectors.Binance
        ),
        AppIconItem(
            id = "coinbase",
            name = "Coinbase",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFF0052FF),
            aliases = listOf("coinbase"),
            iconVector = BrandVectors.Coinbase
        ),
        AppIconItem(
            id = "paypal",
            name = "PayPal",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFF003087),
            aliases = listOf("paypal"),
            iconVector = BrandVectors.PayPal
        ),
        AppIconItem(
            id = "stripe",
            name = "Stripe",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFF635BFF),
            aliases = listOf("stripe"),
            iconVector = BrandVectors.Stripe
        ),
        AppIconItem(
            id = "bank",
            name = "Banking / Financial",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFF059669),
            aliases = listOf("bank", "chase", "capitalone", "citi", "fidelity", "wells fargo"),
            iconVector = BrandVectors.Coinbase
        ),
        AppIconItem(
            id = "revolut",
            name = "Revolut",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFF191C1F),
            aliases = listOf("revolut"),
            iconVector = BrandVectors.Revolut
        ),
        AppIconItem(
            id = "wise",
            name = "Wise",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFF163300),
            aliases = listOf("wise", "transferwise"),
            iconVector = BrandVectors.Wise
        ),
        AppIconItem(
            id = "crypto",
            name = "Crypto / Metamask",
            category = IconCategory.FINANCE,
            brandColor = Color(0xFFF7931A),
            aliases = listOf("crypto", "metamask", "kraken", "kucoin"),
            iconVector = BrandVectors.Binance
        ),

        // SECURITY & PASSWORDS
        AppIconItem(
            id = "proton",
            name = "Proton Mail / VPN",
            category = IconCategory.SECURITY,
            brandColor = Color(0xFF6D4AFF),
            aliases = listOf("proton", "protonmail", "proton.me"),
            iconVector = BrandVectors.Proton
        ),
        AppIconItem(
            id = "bitwarden",
            name = "Bitwarden",
            category = IconCategory.SECURITY,
            brandColor = Color(0xFF175DDC),
            aliases = listOf("bitwarden"),
            iconVector = BrandVectors.Bitwarden
        ),
        AppIconItem(
            id = "onepassword",
            name = "1Password",
            category = IconCategory.SECURITY,
            brandColor = Color(0xFF0072CE),
            aliases = listOf("1password", "onepassword"),
            iconVector = BrandVectors.OnePassword
        ),
        AppIconItem(
            id = "lastpass",
            name = "LastPass",
            category = IconCategory.SECURITY,
            brandColor = Color(0xFFD32F2F),
            aliases = listOf("lastpass"),
            iconVector = BrandVectors.LastPass
        ),
        AppIconItem(
            id = "nordvpn",
            name = "NordVPN",
            category = IconCategory.SECURITY,
            brandColor = Color(0xFF4687FF),
            aliases = listOf("nordvpn", "nord"),
            iconVector = BrandVectors.NordVpn
        ),
        AppIconItem(
            id = "security",
            name = "Security Shield",
            category = IconCategory.SECURITY,
            brandColor = Color(0xFF0D9488),
            aliases = listOf("security", "vault", "auth"),
            iconVector = BrandVectors.Proton
        ),

        // ENTERTAINMENT & STREAMING
        AppIconItem(
            id = "spotify",
            name = "Spotify",
            category = IconCategory.ENTERTAINMENT,
            brandColor = Color(0xFF1DB954),
            aliases = listOf("spotify"),
            iconVector = BrandVectors.Spotify
        ),
        AppIconItem(
            id = "netflix",
            name = "Netflix",
            category = IconCategory.ENTERTAINMENT,
            brandColor = Color(0xFFE50914),
            aliases = listOf("netflix"),
            iconVector = BrandVectors.Netflix
        ),
        AppIconItem(
            id = "dropbox",
            name = "Dropbox",
            category = IconCategory.ENTERTAINMENT,
            brandColor = Color(0xFF0061FE),
            aliases = listOf("dropbox"),
            iconVector = BrandVectors.Dropbox
        ),

        // SHOPPING & SERVICES
        AppIconItem(
            id = "shopify",
            name = "Shopify",
            category = IconCategory.SHOPPING,
            brandColor = Color(0xFF95BF47),
            aliases = listOf("shopify"),
            iconVector = BrandVectors.Shopify
        ),
        AppIconItem(
            id = "ebay",
            name = "eBay",
            category = IconCategory.SHOPPING,
            brandColor = Color(0xFFE53238),
            aliases = listOf("ebay"),
            iconVector = BrandVectors.EBay
        ),
        AppIconItem(
            id = "uber",
            name = "Uber",
            category = IconCategory.SHOPPING,
            brandColor = Color(0xFF000000),
            aliases = listOf("uber", "uber eats"),
            iconVector = BrandVectors.Uber
        )
    )

    fun findById(iconKey: String?): AppIconItem? {
        if (iconKey.isNullOrBlank()) return null
        val clean = iconKey.trim().lowercase()
        return items.firstOrNull { it.id == clean }
    }

    fun matchByIssuer(text: String): AppIconItem? {
        if (text.isBlank()) return null
        val clean = text.trim().lowercase()
            .replace(Regex("https?://"), "")
            .replace(Regex("www\\."), "")

        // 1. Direct id / exact alias match
        items.firstOrNull { item ->
            item.id == clean || item.aliases.any { it.equals(clean, ignoreCase = true) }
        }?.let { return it }

        // 2. Substring or domain match
        return items.firstOrNull { item ->
            clean.contains(item.id) || item.aliases.any { alias -> clean.contains(alias.lowercase()) }
        }
    }

    fun resolveIcon(issuer: String, accountName: String = "", iconKey: String? = null): AppIconItem? {
        // 1. Explicit user icon key
        if (!iconKey.isNullOrBlank()) {
            val custom = findById(iconKey)
            if (custom != null) return custom
        }

        // 2. Match by issuer
        if (issuer.isNotBlank()) {
            val matched = matchByIssuer(issuer)
            if (matched != null) return matched
        }

        // 3. Fallback match by account name / email / handle
        if (accountName.isNotBlank()) {
            val matched = matchByIssuer(accountName)
            if (matched != null) return matched
        }

        return null
    }

    fun search(query: String): List<AppIconItem> {
        if (query.isBlank()) return items
        val clean = query.trim().lowercase()
        return items.filter { item ->
            item.name.lowercase().contains(clean) ||
                    item.id.lowercase().contains(clean) ||
                    item.aliases.any { it.contains(clean) }
        }
    }
}
