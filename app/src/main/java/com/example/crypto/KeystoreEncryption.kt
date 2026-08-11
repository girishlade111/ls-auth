package com.example.crypto

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.security.KeyStore
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

object KeystoreEncryption {

    private const val ANDROID_KEYSTORE = "AndroidKeyStore"
    private const val MASTER_KEY_ALIAS = "LSAuthMasterKeyAlias"
    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val GCM_TAG_LENGTH = 128
    private const val IV_LENGTH = 12

    init {
        initMasterKey()
    }

    private fun initMasterKey() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        if (!keyStore.containsAlias(MASTER_KEY_ALIAS)) {
            val keyGenerator = javax.crypto.KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE
            )
            val spec = KeyGenParameterSpec.Builder(
                MASTER_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
            keyGenerator.init(spec)
            keyGenerator.generateKey()
        }
    }

    private fun getMasterKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        val entry = keyStore.getEntry(MASTER_KEY_ALIAS, null) as KeyStore.SecretKeyEntry
        return entry.secretKey
    }

    /**
     * Encrypts plaintext string using KeyStore Master Key.
     * Output format: Base64(IV + CipherText)
     */
    fun encryptSecret(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getMasterKey())
        val iv = cipher.iv
        val encrypted = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
        val combined = ByteArray(iv.size + encrypted.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encrypted, 0, combined, iv.size, encrypted.size)
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    /**
     * Decrypts ciphertext string using KeyStore Master Key.
     */
    fun decryptSecret(encryptedBase64: String): String {
        if (encryptedBase64.isEmpty()) return ""
        val combined = Base64.decode(encryptedBase64, Base64.NO_WRAP)
        if (combined.size <= IV_LENGTH) return ""
        val iv = ByteArray(IV_LENGTH)
        val ciphertext = ByteArray(combined.size - IV_LENGTH)
        System.arraycopy(combined, 0, iv, 0, IV_LENGTH)
        System.arraycopy(combined, IV_LENGTH, ciphertext, 0, ciphertext.size)

        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getMasterKey(), spec)
        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, Charsets.UTF_8)
    }

    /**
     * Encrypts data for backup export using PBKDF2 derived key from user password.
     * Returns salt + IV + Ciphertext encoded as Base64.
     */
    fun encryptBackup(payloadJson: String, passwordCharArr: CharArray): String {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)

        val iv = ByteArray(IV_LENGTH)
        SecureRandom().nextBytes(iv)

        val secretKey = deriveKeyFromPassword(passwordCharArr, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, spec)

        val ciphertext = cipher.doFinal(payloadJson.toByteArray(Charsets.UTF_8))

        val result = ByteArray(salt.size + iv.size + ciphertext.size)
        System.arraycopy(salt, 0, result, 0, salt.size)
        System.arraycopy(iv, 0, result, salt.size, iv.size)
        System.arraycopy(ciphertext, 0, result, salt.size + iv.size, ciphertext.size)

        return Base64.encodeToString(result, Base64.NO_WRAP)
    }

    /**
     * Decrypts backup file content using user password.
     * Supports both raw Base64 string and JSON backup envelope string containing "data" or "payload".
     */
    fun decryptBackup(encryptedInput: String, passwordCharArr: CharArray): String {
        val trimmed = encryptedInput.trim()
        val base64Data = if (trimmed.startsWith("{")) {
            try {
                val json = org.json.JSONObject(trimmed)
                when {
                    json.has("data") -> json.getString("data")
                    json.has("payload") -> json.getString("payload")
                    else -> trimmed
                }
            } catch (e: Exception) {
                trimmed
            }
        } else {
            trimmed
        }

        val combined = Base64.decode(base64Data, Base64.NO_WRAP)
        if (combined.size <= 16 + IV_LENGTH) {
            throw IllegalArgumentException("Invalid or corrupted backup file payload")
        }

        val salt = ByteArray(16)
        val iv = ByteArray(IV_LENGTH)
        val ciphertext = ByteArray(combined.size - 16 - IV_LENGTH)

        System.arraycopy(combined, 0, salt, 0, 16)
        System.arraycopy(combined, 16, iv, 0, IV_LENGTH)
        System.arraycopy(combined, 16 + IV_LENGTH, ciphertext, 0, ciphertext.size)

        val secretKey = deriveKeyFromPassword(passwordCharArr, salt)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

        val decrypted = cipher.doFinal(ciphertext)
        return String(decrypted, Charsets.UTF_8)
    }

    private fun deriveKeyFromPassword(password: CharArray, salt: ByteArray): SecretKey {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(password, salt, 10_000, 256)
        val tmp = factory.generateSecret(spec)
        return SecretKeySpec(tmp.encoded, "AES")
    }
}
