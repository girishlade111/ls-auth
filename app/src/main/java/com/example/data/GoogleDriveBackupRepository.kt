package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.driveDataStore: DataStore<Preferences> by preferencesDataStore(name = "google_drive_backup_prefs")

data class GoogleDriveState(
    val isConnected: Boolean = false,
    val accountEmail: String = "",
    val lastBackupTimestamp: Long = 0L,
    val lastBackupItemCount: Int = 0,
    val autoBackupEnabled: Boolean = false,
    val lastBackupPayload: String = ""
)

class GoogleDriveBackupRepository(private val context: Context) {

    private object Keys {
        val IS_CONNECTED = booleanPreferencesKey("drive_is_connected")
        val ACCOUNT_EMAIL = stringPreferencesKey("drive_account_email")
        val LAST_BACKUP_TIMESTAMP = longPreferencesKey("drive_last_backup_timestamp")
        val LAST_BACKUP_ITEM_COUNT = booleanPreferencesKey("drive_last_backup_item_count_key")
        val AUTO_BACKUP_ENABLED = booleanPreferencesKey("drive_auto_backup_enabled")
        val STORED_DRIVE_PAYLOAD = stringPreferencesKey("drive_stored_payload")
        val ITEM_COUNT_INT = longPreferencesKey("drive_last_backup_item_count_int")
    }

    val driveState: Flow<GoogleDriveState> = context.driveDataStore.data.map { prefs ->
        GoogleDriveState(
            isConnected = prefs[Keys.IS_CONNECTED] ?: false,
            accountEmail = prefs[Keys.ACCOUNT_EMAIL] ?: "",
            lastBackupTimestamp = prefs[Keys.LAST_BACKUP_TIMESTAMP] ?: 0L,
            lastBackupItemCount = (prefs[Keys.ITEM_COUNT_INT] ?: 0L).toInt(),
            autoBackupEnabled = prefs[Keys.AUTO_BACKUP_ENABLED] ?: false,
            lastBackupPayload = prefs[Keys.STORED_DRIVE_PAYLOAD] ?: ""
        )
    }

    suspend fun connectGoogleAccount(email: String) {
        context.driveDataStore.edit { prefs ->
            prefs[Keys.IS_CONNECTED] = true
            prefs[Keys.ACCOUNT_EMAIL] = email.ifBlank { "user@gmail.com" }
        }
    }

    suspend fun disconnectGoogleAccount() {
        context.driveDataStore.edit { prefs ->
            prefs[Keys.IS_CONNECTED] = false
            prefs[Keys.ACCOUNT_EMAIL] = ""
            prefs[Keys.AUTO_BACKUP_ENABLED] = false
        }
    }

    suspend fun saveBackupToDrive(payload: String, itemCount: Int) {
        context.driveDataStore.edit { prefs ->
            prefs[Keys.STORED_DRIVE_PAYLOAD] = payload
            prefs[Keys.LAST_BACKUP_TIMESTAMP] = System.currentTimeMillis()
            prefs[Keys.ITEM_COUNT_INT] = itemCount.toLong()
        }
    }

    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        context.driveDataStore.edit { prefs ->
            prefs[Keys.AUTO_BACKUP_ENABLED] = enabled
        }
    }
}
