package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ls_auth_settings")

data class UserSettings(
    val isAppLockEnabled: Boolean = false,
    val pinHash: String = "",
    val pinSalt: String = "",
    val useBiometric: Boolean = false,
    val autoLockTimeoutSeconds: Int = 60,
    val requireReauthToReveal: Boolean = false,
    val blurCodesByDefault: Boolean = false,
    val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
    val reduceMotion: Boolean = false,
    val hasBackupBeenDone: Boolean = false,
    val sortOrder: String = "MANUAL", // MANUAL, ALPHABETICAL, MOST_USED
    val isOnboardingCompleted: Boolean = false,
    val hapticsEnabled: Boolean = true,
    val hapticIntensity: String = "MEDIUM", // LIGHT, MEDIUM, STRONG
    val hapticDuration: String = "MEDIUM", // SHORT, MEDIUM, LONG
    val copyCodeHapticEnabled: Boolean = true,
    val scanSuccessHapticEnabled: Boolean = true,
    val errorHapticEnabled: Boolean = true,
    val checkTimeAccuracyEnabled: Boolean = false
)

class SettingsRepository(private val context: Context) {

    private object Keys {
        val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
        val PIN_HASH = stringPreferencesKey("pin_hash")
        val PIN_SALT = stringPreferencesKey("pin_salt")
        val USE_BIOMETRIC = booleanPreferencesKey("use_biometric")
        val AUTO_LOCK_TIMEOUT = intPreferencesKey("auto_lock_timeout")
        val REQUIRE_REAUTH_TO_REVEAL = booleanPreferencesKey("require_reauth_to_reveal")
        val BLUR_CODES_DEFAULT = booleanPreferencesKey("blur_codes_default")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val REDUCE_MOTION = booleanPreferencesKey("reduce_motion")
        val HAS_BACKUP_BEEN_DONE = booleanPreferencesKey("has_backup_been_done")
        val SORT_ORDER = stringPreferencesKey("sort_order")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")
        val HAPTIC_DURATION = stringPreferencesKey("haptic_duration")
        val COPY_CODE_HAPTIC_ENABLED = booleanPreferencesKey("copy_code_haptic_enabled")
        val SCAN_SUCCESS_HAPTIC_ENABLED = booleanPreferencesKey("scan_success_haptic_enabled")
        val ERROR_HAPTIC_ENABLED = booleanPreferencesKey("error_haptic_enabled")
        val CHECK_TIME_ACCURACY_ENABLED = booleanPreferencesKey("check_time_accuracy_enabled")
    }

    val userSettingsFlow: Flow<UserSettings> = context.dataStore.data.map { prefs ->
        UserSettings(
            isAppLockEnabled = prefs[Keys.APP_LOCK_ENABLED] ?: false,
            pinHash = prefs[Keys.PIN_HASH] ?: "",
            pinSalt = prefs[Keys.PIN_SALT] ?: "",
            useBiometric = prefs[Keys.USE_BIOMETRIC] ?: false,
            autoLockTimeoutSeconds = prefs[Keys.AUTO_LOCK_TIMEOUT] ?: 60,
            requireReauthToReveal = prefs[Keys.REQUIRE_REAUTH_TO_REVEAL] ?: false,
            blurCodesByDefault = prefs[Keys.BLUR_CODES_DEFAULT] ?: false,
            themeMode = prefs[Keys.THEME_MODE] ?: "SYSTEM",
            reduceMotion = prefs[Keys.REDUCE_MOTION] ?: false,
            hasBackupBeenDone = prefs[Keys.HAS_BACKUP_BEEN_DONE] ?: false,
            sortOrder = prefs[Keys.SORT_ORDER] ?: "MANUAL",
            isOnboardingCompleted = prefs[Keys.ONBOARDING_COMPLETED] ?: false,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            hapticIntensity = prefs[Keys.HAPTIC_INTENSITY] ?: "MEDIUM",
            hapticDuration = prefs[Keys.HAPTIC_DURATION] ?: "MEDIUM",
            copyCodeHapticEnabled = prefs[Keys.COPY_CODE_HAPTIC_ENABLED] ?: true,
            scanSuccessHapticEnabled = prefs[Keys.SCAN_SUCCESS_HAPTIC_ENABLED] ?: true,
            errorHapticEnabled = prefs[Keys.ERROR_HAPTIC_ENABLED] ?: true,
            checkTimeAccuracyEnabled = prefs[Keys.CHECK_TIME_ACCURACY_ENABLED] ?: false
        )
    }

    suspend fun setPin(pin: String) {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        val saltHex = salt.joinToString("") { "%02x".format(it) }
        val hashHex = hashPin(pin, salt)

        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_HASH] = hashHex
            prefs[Keys.PIN_SALT] = saltHex
            prefs[Keys.APP_LOCK_ENABLED] = true
        }
    }

    suspend fun verifyPin(pin: String, currentSettings: UserSettings): Boolean {
        if (currentSettings.pinHash.isEmpty() || currentSettings.pinSalt.isEmpty()) return true
        val salt = currentSettings.pinSalt.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
        val computedHash = hashPin(pin, salt)
        return computedHash == currentSettings.pinHash
    }

    suspend fun removePin() {
        context.dataStore.edit { prefs ->
            prefs[Keys.PIN_HASH] = ""
            prefs[Keys.PIN_SALT] = ""
            prefs[Keys.APP_LOCK_ENABLED] = false
            prefs[Keys.USE_BIOMETRIC] = false
        }
    }

    suspend fun setUseBiometric(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.USE_BIOMETRIC] = enabled
        }
    }

    suspend fun setAutoLockTimeout(seconds: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.AUTO_LOCK_TIMEOUT] = seconds
        }
    }

    suspend fun setRequireReauthToReveal(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REQUIRE_REAUTH_TO_REVEAL] = enabled
        }
    }

    suspend fun setBlurCodesByDefault(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.BLUR_CODES_DEFAULT] = enabled
        }
    }

    suspend fun setThemeMode(themeMode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = themeMode
        }
    }

    suspend fun setReduceMotion(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.REDUCE_MOTION] = enabled
        }
    }

    suspend fun setHasBackupBeenDone(done: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_BACKUP_BEEN_DONE] = done
        }
    }

    suspend fun setSortOrder(sortOrder: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SORT_ORDER] = sortOrder
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun setHapticIntensity(intensity: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC_INTENSITY] = intensity
        }
    }

    suspend fun setHapticDuration(duration: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAPTIC_DURATION] = duration
        }
    }

    suspend fun setCopyCodeHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.COPY_CODE_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setScanSuccessHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SCAN_SUCCESS_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setErrorHapticEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ERROR_HAPTIC_ENABLED] = enabled
        }
    }

    suspend fun setCheckTimeAccuracyEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CHECK_TIME_ACCURACY_ENABLED] = enabled
        }
    }

    private fun hashPin(pin: String, salt: ByteArray): String {
        val factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec = PBEKeySpec(pin.toCharArray(), salt, 50_000, 256)
        val hash = factory.generateSecret(spec).encoded
        return hash.joinToString("") { "%02x".format(it) }
    }
}
