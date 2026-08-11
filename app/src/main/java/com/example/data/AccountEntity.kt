package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountName: String,
    val issuer: String,
    val secretEncrypted: String,
    val type: String = "TOTP", // TOTP or HOTP
    val algorithm: String = "SHA1", // SHA1, SHA256, SHA512
    val digits: Int = 6, // 6 or 8
    val period: Int = 30, // seconds for TOTP
    val counter: Long = 0L, // HOTP counter
    val folderId: Long? = null,
    val iconKey: String? = null,
    val displayOrder: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
