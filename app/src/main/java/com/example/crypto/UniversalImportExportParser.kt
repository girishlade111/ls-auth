package com.example.crypto

import android.net.Uri
import com.example.model.Account
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLDecoder

object UniversalImportExportParser {

    /**
     * Parses a string payload into a list of MigratedAccount items.
     * Auto-detects format: Bitwarden CSV, Standard JSON, Bitwarden JSON, otpauth:// URIs, or Google Auth Migration URIs.
     */
    fun parseImportPayload(content: String): List<MigratedAccount> {
        val trimmed = content.trim()
        val results = mutableListOf<MigratedAccount>()

        return when {
            // Google Auth Migration
            trimmed.startsWith("otpauth-migration://", ignoreCase = true) -> {
                GoogleAuthMigrationParser.parseMigrationUrl(trimmed)
            }

            // Single or Multiple otpauth:// URIs (e.g. from Microsoft Authenticator or Zoho text export)
            trimmed.contains("otpauth://", ignoreCase = true) -> {
                parseOtpAuthUris(trimmed)
            }

            // JSON Format (Bitwarden export, standard JSON, or custom vault backup)
            trimmed.startsWith("{") || trimmed.startsWith("[") -> {
                parseJsonFormat(trimmed)
            }

            // CSV Format (Bitwarden CSV, Microsoft Auth CSV, Zoho CSV, or standard CSV)
            trimmed.contains(",") || trimmed.contains(";") -> {
                parseCsvFormat(trimmed)
            }

            else -> throw IllegalArgumentException("Unrecognized import format. Please ensure content is a valid CSV, JSON, or otpauth URI.")
        }
    }

    /**
     * Parses single or multi-line otpauth:// URIs.
     */
    fun parseOtpAuthUris(content: String): List<MigratedAccount> {
        val list = mutableListOf<MigratedAccount>()
        content.lines().forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("otpauth://", ignoreCase = true)) {
                try {
                    val account = parseSingleOtpAuthUri(trimmedLine)
                    if (account != null) list.add(account)
                } catch (e: Exception) {
                    // Ignore malformed single line
                }
            }
        }
        return list
    }

    private fun parseSingleOtpAuthUri(uriString: String): MigratedAccount? {
        val uri = Uri.parse(uriString)
        val typeStr = uri.host ?: "totp"
        val otpType = if (typeStr.equals("hotp", ignoreCase = true)) OtpType.HOTP else OtpType.TOTP

        val path = uri.path?.removePrefix("/") ?: ""
        var issuer = uri.getQueryParameter("issuer") ?: ""
        var name = path

        if (name.contains(":")) {
            val parts = name.split(":", limit = 2)
            if (issuer.isEmpty()) issuer = URLDecoder.decode(parts[0], "UTF-8")
            name = URLDecoder.decode(parts[1], "UTF-8")
        } else {
            name = URLDecoder.decode(name, "UTF-8")
        }

        val secret = uri.getQueryParameter("secret")?.uppercase()?.replace(" ", "") ?: return null
        val algoStr = uri.getQueryParameter("algorithm") ?: "SHA1"
        val algorithm = try { HashAlgorithm.valueOf(algoStr.uppercase()) } catch (e: Exception) { HashAlgorithm.SHA1 }
        val digits = uri.getQueryParameter("digits")?.toIntOrNull() ?: 6
        val counter = uri.getQueryParameter("counter")?.toLongOrNull() ?: 0L

        return MigratedAccount(
            secretBase32 = secret,
            secretRaw = try { Base32.decode(secret) } catch (e: Exception) { ByteArray(0) },
            name = name,
            issuer = issuer,
            algorithm = algorithm,
            digits = digits,
            type = otpType,
            counter = counter
        )
    }

    /**
     * Parses JSON formats: Bitwarden, Standard Vault, 2FA exports.
     */
    private fun parseJsonFormat(jsonStr: String): List<MigratedAccount> {
        val list = mutableListOf<MigratedAccount>()

        try {
            if (jsonStr.startsWith("[")) {
                val array = JSONArray(jsonStr)
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    val acc = parseJsonObjectAccount(obj)
                    if (acc != null) list.add(acc)
                }
            } else {
                val root = JSONObject(jsonStr)
                // Bitwarden JSON export has "items" array
                if (root.has("items")) {
                    val items = root.getJSONArray("items")
                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val name = item.optString("name", "Account")
                        val login = item.optJSONObject("login")
                        val totp = login?.optString("totp", null)

                        if (!totp.isNullOrBlank()) {
                            val acc = if (totp.startsWith("otpauth://", ignoreCase = true)) {
                                parseSingleOtpAuthUri(totp)
                            } else {
                                // Plain secret string in Bitwarden
                                val cleanSecret = extractSecretFromBitwardenTotp(totp)
                                if (cleanSecret.isNotBlank()) {
                                    MigratedAccount(
                                        secretBase32 = cleanSecret,
                                        secretRaw = try { Base32.decode(cleanSecret) } catch (e: Exception) { ByteArray(0) },
                                        name = name,
                                        issuer = name,
                                        algorithm = HashAlgorithm.SHA1,
                                        digits = 6,
                                        type = OtpType.TOTP,
                                        counter = 0L
                                    )
                                } else null
                            }
                            if (acc != null) list.add(acc)
                        }
                    }
                } else if (root.has("accounts")) {
                    val accountsArray = root.getJSONArray("accounts")
                    for (i in 0 until accountsArray.length()) {
                        val obj = accountsArray.getJSONObject(i)
                        val acc = parseJsonObjectAccount(obj)
                        if (acc != null) list.add(acc)
                    }
                } else {
                    val acc = parseJsonObjectAccount(root)
                    if (acc != null) list.add(acc)
                }
            }
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse JSON backup: ${e.localizedMessage}")
        }

        return list
    }

    private fun parseJsonObjectAccount(obj: JSONObject): MigratedAccount? {
        val secret = obj.optString("secret", obj.optString("secretBase32", "")).uppercase().replace(" ", "")
        if (secret.isBlank()) return null

        val name = obj.optString("accountName", obj.optString("name", "Account"))
        val issuer = obj.optString("issuer", "")
        val algoStr = obj.optString("algorithm", "SHA1")
        val algorithm = try { HashAlgorithm.valueOf(algoStr.uppercase()) } catch (e: Exception) { HashAlgorithm.SHA1 }
        val digits = obj.optInt("digits", 6)
        val typeStr = obj.optString("type", "TOTP")
        val type = if (typeStr.equals("HOTP", ignoreCase = true)) OtpType.HOTP else OtpType.TOTP
        val counter = obj.optLong("counter", 0L)

        return MigratedAccount(
            secretBase32 = secret,
            secretRaw = try { Base32.decode(secret) } catch (e: Exception) { ByteArray(0) },
            name = name,
            issuer = issuer,
            algorithm = algorithm,
            digits = digits,
            type = type,
            counter = counter
        )
    }

    /**
     * Parses CSV files (Bitwarden, Microsoft Authenticator, Zoho OneAuth, Google Auth CSVs, Standard CSVs).
     */
    private fun parseCsvFormat(csvStr: String): List<MigratedAccount> {
        val list = mutableListOf<MigratedAccount>()
        val lines = csvStr.lines().filter { it.isNotBlank() }
        if (lines.isEmpty()) return list

        val header = lines.first().split(",").map { it.trim().lowercase().removeSurrounding("\"") }
        val isBitwardenCsv = header.contains("login_totp") || header.contains("login_username")
        val isStandardCsv = header.contains("secret") || header.contains("secretbase32")

        val secretIdx = header.indexOfFirst { it == "secret" || it == "secretbase32" || it == "totp" || it == "login_totp" || it == "key" }
        val issuerIdx = header.indexOfFirst { it == "issuer" || it == "service" || it == "name" || it == "folder" }
        val nameIdx = header.indexOfFirst { it == "account" || it == "accountname" || it == "username" || it == "login_username" || it == "name" }

        lines.drop(if (isBitwardenCsv || isStandardCsv) 1 else 0).forEach { line ->
            val cols = parseCsvLine(line)
            if (cols.isNotEmpty()) {
                val rawTotp = if (secretIdx >= 0 && secretIdx < cols.size) cols[secretIdx] else ""
                val issuer = if (issuerIdx >= 0 && issuerIdx < cols.size) cols[issuerIdx] else "Imported"
                val name = if (nameIdx >= 0 && nameIdx < cols.size) cols[nameIdx] else "Account"

                if (rawTotp.startsWith("otpauth://", ignoreCase = true)) {
                    val acc = parseSingleOtpAuthUri(rawTotp)
                    if (acc != null) list.add(acc)
                } else {
                    val cleanSecret = extractSecretFromBitwardenTotp(rawTotp)
                    if (cleanSecret.length >= 8) {
                        list.add(
                            MigratedAccount(
                                secretBase32 = cleanSecret,
                                secretRaw = try { Base32.decode(cleanSecret) } catch (e: Exception) { ByteArray(0) },
                                name = name,
                                issuer = issuer,
                                algorithm = HashAlgorithm.SHA1,
                                digits = 6,
                                type = OtpType.TOTP,
                                counter = 0L
                            )
                        )
                    }
                }
            }
        }

        return list
    }

    private fun parseCsvLine(line: String): List<String> {
        val tokens = mutableListOf<String>()
        var sb = StringBuilder()
        var inQuotes = false
        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch == ',' && !inQuotes -> {
                    tokens.add(sb.toString().trim().removeSurrounding("\""))
                    sb = StringBuilder()
                }
                else -> sb.append(ch)
            }
        }
        tokens.add(sb.toString().trim().removeSurrounding("\""))
        return tokens
    }

    private fun extractSecretFromBitwardenTotp(totpStr: String): String {
        val trimmed = totpStr.trim()
        if (trimmed.startsWith("otpauth://", ignoreCase = true)) {
            val uri = Uri.parse(trimmed)
            return uri.getQueryParameter("secret")?.uppercase()?.replace(" ", "") ?: ""
        }
        // Bitwarden sometimes exports plain secrets or "steam://" or parameters
        val cleaned = trimmed.replace(" ", "").uppercase()
        return cleaned.takeWhile { it in 'A'..'Z' || it in '2'..'7' }
    }

    // --- EXPORTERS ---

    /**
     * Exports accounts into Bitwarden CSV format.
     */
    fun exportToBitwardenCsv(accounts: List<Account>): String {
        val sb = StringBuilder()
        sb.append("folder,favorite,type,name,notes,fields,reprompt,login_uri,login_username,login_password,login_totp\n")
        accounts.forEach { acc ->
            val folder = "2FA Tokens"
            val type = "login"
            val name = if (acc.issuer.isNotBlank()) "${acc.issuer} (${acc.accountName})" else acc.accountName
            val username = acc.accountName
            val totpUri = acc.toOtpAuthUri()
            sb.append("\"$folder\",0,$type,\"$name\",,,0,,\"$username\",,\"$totpUri\"\n")
        }
        return sb.toString()
    }

    /**
     * Exports accounts into Standard OTP URIs list (compatible with Microsoft Authenticator, Zoho OneAuth, Google Auth, etc.).
     */
    fun exportToStandardUris(accounts: List<Account>): String {
        return accounts.joinToString("\n") { it.toOtpAuthUri() }
    }

    /**
     * Exports accounts into Standard CSV format.
     */
    fun exportToStandardCsv(accounts: List<Account>): String {
        val sb = StringBuilder()
        sb.append("issuer,account_name,secret,type,algorithm,digits,period\n")
        accounts.forEach { acc ->
            sb.append("\"${acc.issuer}\",\"${acc.accountName}\",\"${acc.secretBase32}\",\"${acc.type.name}\",\"${acc.algorithm.name}\",${acc.digits},${acc.period}\n")
        }
        return sb.toString()
    }
}
