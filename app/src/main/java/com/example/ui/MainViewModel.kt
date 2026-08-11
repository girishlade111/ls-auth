package com.example.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.crypto.GoogleAuthMigrationParser
import com.example.crypto.KeystoreEncryption
import com.example.crypto.MigratedAccount
import com.example.crypto.OtpUriParser
import com.example.data.AccountRepository
import com.example.data.AppDatabase
import com.example.data.FolderEntity
import com.example.data.SettingsRepository
import com.example.data.UserSettings
import com.example.model.Account
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

import com.example.util.TimeAccuracyStatus
import com.example.util.TimeSyncChecker

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val accountRepository = AccountRepository(db.accountDao(), db.folderDao(), db.passwordDao())
    val settingsRepository = SettingsRepository(application)
    val googleDriveRepository = com.example.data.GoogleDriveBackupRepository(application)

    val googleDriveState: StateFlow<com.example.data.GoogleDriveState> = googleDriveRepository.driveState
        .stateIn(viewModelScope, SharingStarted.Eagerly, com.example.data.GoogleDriveState())

    val passwords: StateFlow<List<com.example.data.PasswordEntity>> = accountRepository.allPasswords
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun addPassword(password: com.example.data.PasswordEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            accountRepository.insertPassword(password)
            onComplete?.invoke()
        }
    }

    fun updatePassword(password: com.example.data.PasswordEntity, onComplete: (() -> Unit)? = null) {
        viewModelScope.launch {
            accountRepository.updatePassword(password)
            onComplete?.invoke()
        }
    }

    fun deletePassword(id: Long) {
        viewModelScope.launch {
            accountRepository.deletePasswordById(id)
        }
    }

    val userSettings: StateFlow<UserSettings> = settingsRepository.userSettingsFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, UserSettings())

    private val _timeAccuracyStatus = MutableStateFlow<TimeAccuracyStatus>(TimeAccuracyStatus.Idle)
    val timeAccuracyStatus: StateFlow<TimeAccuracyStatus> = _timeAccuracyStatus.asStateFlow()

    val allFolders: StateFlow<List<FolderEntity>> = accountRepository.allFolders
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    // Clock ticker in seconds driving live TOTP updates
    private val _currentTimeSeconds = MutableStateFlow(System.currentTimeMillis() / 1000)
    val currentTimeSeconds: StateFlow<Long> = _currentTimeSeconds.asStateFlow()

    // Filtered accounts list
    val accounts: StateFlow<List<Account>> = combine(
        accountRepository.allAccounts,
        _searchQuery,
        _selectedFolderId
    ) { allAccountsList, query, folderId ->
        allAccountsList.filter { account ->
            val matchesFolder = (folderId == null || account.folderId == folderId)
            val matchesSearch = query.isBlank() ||
                    account.issuer.contains(query, ignoreCase = true) ||
                    account.accountName.contains(query, ignoreCase = true)
            matchesFolder && matchesSearch
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Migration / Import staging list
    private val _stagedMigrationAccounts = MutableStateFlow<List<MigratedAccount>>(emptyList())
    val stagedMigrationAccounts: StateFlow<List<MigratedAccount>> = _stagedMigrationAccounts.asStateFlow()

    init {
        // Start ticking time every 500ms
        viewModelScope.launch {
            while (true) {
                _currentTimeSeconds.value = System.currentTimeMillis() / 1000
                delay(500)
            }
        }

        // Auto-trigger time check if setting enabled on launch
        viewModelScope.launch {
            userSettings.collect { settings ->
                if (settings.checkTimeAccuracyEnabled && _timeAccuracyStatus.value is TimeAccuracyStatus.Idle) {
                    performTimeAccuracyCheck()
                }
            }
        }
    }

    fun setCheckTimeAccuracyEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setCheckTimeAccuracyEnabled(enabled)
            if (enabled) {
                performTimeAccuracyCheck()
            } else {
                _timeAccuracyStatus.value = TimeAccuracyStatus.Idle
            }
        }
    }

    fun performTimeAccuracyCheck() {
        viewModelScope.launch {
            _timeAccuracyStatus.value = TimeAccuracyStatus.Checking
            val result = TimeSyncChecker.checkClockAccuracy()
            _timeAccuracyStatus.value = result
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectFolder(folderId: Long?) {
        _selectedFolderId.value = folderId
    }

    fun addAccount(account: Account, onComplete: () -> Unit) {
        viewModelScope.launch {
            accountRepository.insertAccount(account)
            onComplete()
        }
    }

    fun updateAccount(account: Account, onComplete: () -> Unit) {
        viewModelScope.launch {
            accountRepository.updateAccount(account)
            onComplete()
        }
    }

    fun reorderAccounts(reorderedList: List<Account>) {
        viewModelScope.launch {
            accountRepository.updateAccountOrders(reorderedList)
        }
    }

    fun incrementHotpCounter(account: Account) {
        viewModelScope.launch {
            val newCounter = account.counter + 1
            accountRepository.updateHotpCounter(account.id, newCounter)
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountRepository.deleteAccountById(account.id)
        }
    }

    fun stageMigrationAccounts(accounts: List<MigratedAccount>) {
        _stagedMigrationAccounts.value = accounts
    }

    fun commitSelectedMigrationAccounts(
        selectedIndices: Set<Int>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            val staged = _stagedMigrationAccounts.value
            val accountsToInsert = staged.filterIndexed { index, _ -> index in selectedIndices }.map { mig ->
                Account(
                    accountName = mig.name,
                    issuer = mig.issuer,
                    secretBase32 = mig.secretBase32,
                    type = mig.type,
                    algorithm = mig.algorithm,
                    digits = mig.digits,
                    counter = mig.counter
                )
            }
            if (accountsToInsert.isNotEmpty()) {
                accountRepository.insertAccounts(accountsToInsert)
            }
            _stagedMigrationAccounts.value = emptyList()
            onComplete()
        }
    }

    // --- Google Drive Optional Backup Operations ---
    fun connectGoogleDrive(email: String) {
        viewModelScope.launch {
            googleDriveRepository.connectGoogleAccount(email)
        }
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            googleDriveRepository.disconnectGoogleAccount()
        }
    }

    fun backupToGoogleDrive(password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentAccounts = accounts.value
                val jsonArray = JSONArray()
                currentAccounts.forEach { acc ->
                    val obj = JSONObject().apply {
                        put("accountName", acc.accountName)
                        put("issuer", acc.issuer)
                        put("secretBase32", acc.secretBase32)
                        put("type", acc.type.name)
                        put("algorithm", acc.algorithm.name)
                        put("digits", acc.digits)
                        put("period", acc.period)
                        put("counter", acc.counter)
                    }
                    jsonArray.put(obj)
                }

                val encryptedPayload = KeystoreEncryption.encryptBackup(jsonArray.toString(), password.toCharArray())
                val backupJson = JSONObject().apply {
                    put("version", 1)
                    put("generator", "LS Auth Vault Google Drive Backup")
                    put("algorithm", "AES-256-GCM")
                    put("createdAt", System.currentTimeMillis())
                    put("data", encryptedPayload)
                }.toString(2)

                googleDriveRepository.saveBackupToDrive(backupJson, currentAccounts.size)
                settingsRepository.setHasBackupBeenDone(true)
                onSuccess("Google Drive backup updated successfully (${currentAccounts.size} accounts)")
            } catch (e: Exception) {
                onError("Failed to upload backup to Google Drive: ${e.message}")
            }
        }
    }

    fun restoreFromGoogleDrive(password: String, onSuccess: (List<MigratedAccount>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val payload = googleDriveState.value.lastBackupPayload
                if (payload.isBlank()) {
                    onError("No Google Drive backup file found. Please run a backup first.")
                    return@launch
                }
                decryptBackupForReview(payload, password, onSuccess, onError)
            } catch (e: Exception) {
                onError("Failed to restore Google Drive backup: ${e.message}")
            }
        }
    }

    // --- Universal Multi-App Import/Export ---
    fun parseAndStageUniversalImport(
        rawContent: String,
        onSuccess: (Int) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val parsedAccounts = com.example.crypto.UniversalImportExportParser.parseImportPayload(rawContent)
                if (parsedAccounts.isEmpty()) {
                    onError("No 2FA accounts found in the provided import data.")
                } else {
                    _stagedMigrationAccounts.value = parsedAccounts
                    onSuccess(parsedAccounts.size)
                }
            } catch (e: Exception) {
                onError(e.localizedMessage ?: "Failed to parse import data.")
            }
        }
    }

    fun generateExportBitwardenCsv(): String {
        return com.example.crypto.UniversalImportExportParser.exportToBitwardenCsv(accounts.value)
    }

    fun generateExportStandardUris(): String {
        return com.example.crypto.UniversalImportExportParser.exportToStandardUris(accounts.value)
    }

    fun generateExportStandardCsv(): String {
        return com.example.crypto.UniversalImportExportParser.exportToStandardCsv(accounts.value)
    }

    // Encrypted Backup Export
    fun exportBackup(password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val currentAccounts = accounts.value
                val jsonArray = JSONArray()
                currentAccounts.forEach { acc ->
                    val obj = JSONObject().apply {
                        put("accountName", acc.accountName)
                        put("issuer", acc.issuer)
                        put("secretBase32", acc.secretBase32)
                        put("type", acc.type.name)
                        put("algorithm", acc.algorithm.name)
                        put("digits", acc.digits)
                        put("period", acc.period)
                        put("counter", acc.counter)
                    }
                    jsonArray.put(obj)
                }

                val encryptedPayload = KeystoreEncryption.encryptBackup(jsonArray.toString(), password.toCharArray())
                val backupJson = JSONObject().apply {
                    put("version", 1)
                    put("generator", "LS Auth Vault")
                    put("algorithm", "AES-256-GCM")
                    put("kdf", "PBKDF2WithHmacSHA256")
                    put("kdfIterations", 10000)
                    put("createdAt", System.currentTimeMillis())
                    put("data", encryptedPayload)
                }.toString(2)

                settingsRepository.setHasBackupBeenDone(true)
                onSuccess(backupJson)
            } catch (e: Exception) {
                onError("Failed to export backup: ${e.message}")
            }
        }
    }

    // Encrypted Backup Import / Restore
    fun decryptBackupForReview(
        encryptedPayload: String,
        password: String,
        onSuccess: (List<MigratedAccount>) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val decryptedJson = KeystoreEncryption.decryptBackup(encryptedPayload, password.toCharArray())
                val jsonArray = JSONArray(decryptedJson)
                val restoredList = mutableListOf<MigratedAccount>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val secretBase32 = obj.getString("secretBase32")
                    val secretRaw = try { com.example.crypto.Base32.decode(secretBase32) } catch (e: Exception) { ByteArray(0) }
                    restoredList.add(
                        MigratedAccount(
                            secretBase32 = secretBase32,
                            secretRaw = secretRaw,
                            name = obj.optString("accountName", "Account"),
                            issuer = obj.optString("issuer", ""),
                            algorithm = com.example.crypto.HashAlgorithm.fromString(obj.optString("algorithm", "SHA1")),
                            digits = obj.optInt("digits", 6),
                            type = try { com.example.crypto.OtpType.valueOf(obj.optString("type", "TOTP")) } catch (e: Exception) { com.example.crypto.OtpType.TOTP },
                            counter = obj.optLong("counter", 0L)
                        )
                    )
                }

                if (restoredList.isEmpty()) {
                    onError("No accounts found in backup file.")
                } else {
                    _stagedMigrationAccounts.value = restoredList
                    onSuccess(restoredList)
                }
            } catch (e: Exception) {
                onError("Failed to decrypt backup. Incorrect password or corrupted file.")
            }
        }
    }

    fun importBackup(encryptedPayload: String, password: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val decryptedJson = KeystoreEncryption.decryptBackup(encryptedPayload, password.toCharArray())
                val jsonArray = JSONArray(decryptedJson)
                val restoredList = mutableListOf<Account>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    restoredList.add(
                        Account(
                            accountName = obj.optString("accountName", "Account"),
                            issuer = obj.optString("issuer", ""),
                            secretBase32 = obj.getString("secretBase32"),
                            type = try { com.example.crypto.OtpType.valueOf(obj.optString("type", "TOTP")) } catch (e: Exception) { com.example.crypto.OtpType.TOTP },
                            algorithm = com.example.crypto.HashAlgorithm.fromString(obj.optString("algorithm", "SHA1")),
                            digits = obj.optInt("digits", 6),
                            period = obj.optInt("period", 30),
                            counter = obj.optLong("counter", 0L)
                        )
                    )
                }

                if (restoredList.isNotEmpty()) {
                    accountRepository.insertAccounts(restoredList)
                }
                onSuccess(restoredList.size)
            } catch (e: Exception) {
                onError("Failed to restore backup. Invalid password or corrupted file.")
            }
        }
    }
}
