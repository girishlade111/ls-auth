package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.model.Account
import com.example.security.AppLockManager
import com.example.ui.MainViewModel
import com.example.ui.screens.AccountDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ImportExportScreen
import com.example.ui.screens.LockScreen
import com.example.ui.screens.ManualEntryScreen
import com.example.ui.screens.MigrationReviewScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PasswordVaultScreen
import com.example.ui.screens.PasswordlessLoginScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WearableCompanionScreen
import com.example.ui.theme.LSAuthTheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onUserInteraction() {
        super.onUserInteraction()
        AppLockManager.recordUserInteraction()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Disable FLAG_SECURE to allow streaming emulator preview rendering
        AppLockManager.applySecureFlag(this, false)

        // Observe process lifecycle for auto-lock on app background/foreground
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            LifecycleEventObserver { _, event ->
                val settings = viewModel.userSettings.value
                when (event) {
                    Lifecycle.Event.ON_STOP -> AppLockManager.onAppBackgrounded(settings)
                    Lifecycle.Event.ON_START -> AppLockManager.onAppForegrounded(settings)
                    else -> {}
                }
            }
        )

        // Periodic inactivity timer check
        lifecycleScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000L)
                val settings = viewModel.userSettings.value
                AppLockManager.checkInactivityTimeout(settings)
            }
        }

        setContent {
            val userSettings by viewModel.userSettings.collectAsState()
            val accounts by viewModel.accounts.collectAsState()
            val folders by viewModel.allFolders.collectAsState()
            val timeSeconds by viewModel.currentTimeSeconds.collectAsState()
            val searchQuery by viewModel.searchQuery.collectAsState()
            val selectedFolderId by viewModel.selectedFolderId.collectAsState()
            val isUnlocked by AppLockManager.isUnlocked.collectAsState()
            val stagedMigrationAccounts by viewModel.stagedMigrationAccounts.collectAsState()
            val timeAccuracyStatus by viewModel.timeAccuracyStatus.collectAsState()

            LSAuthTheme(themeMode = userSettings.themeMode) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when {
                        !userSettings.isOnboardingCompleted -> {
                            OnboardingScreen(
                                onCompleteOnboarding = {
                                    lifecycleScope.launch {
                                        viewModel.settingsRepository.setOnboardingCompleted(true)
                                        AppLockManager.unlock()
                                    }
                                }
                            )
                        }

                        userSettings.isAppLockEnabled && !isUnlocked -> {
                            LockScreen(
                                onVerifyPin = { pin ->
                                    kotlinx.coroutines.runBlocking {
                                        val ok = viewModel.settingsRepository.verifyPin(pin, userSettings)
                                        if (ok) AppLockManager.unlock()
                                        ok
                                    }
                                },
                                onBiometricClick = {
                                    AppLockManager.promptBiometric(
                                        activity = this@MainActivity,
                                        onSuccess = { AppLockManager.unlock() },
                                        onError = {}
                                    )
                                },
                                useBiometric = userSettings.useBiometric
                            )
                        }

                        else -> {
                            val navController = rememberNavController()

                            NavHost(
                                navController = navController,
                                startDestination = "home"
                            ) {
                                composable("home") {
                                    HomeScreen(
                                        accounts = accounts,
                                        folders = folders,
                                        timeSeconds = timeSeconds,
                                        searchQuery = searchQuery,
                                        selectedFolderId = selectedFolderId,
                                        blurCodesByDefault = userSettings.blurCodesByDefault,
                                        hasBackupBeenDone = userSettings.hasBackupBeenDone,
                                         onSearchQueryChange = { viewModel.setSearchQuery(it) },
                                        onFolderSelect = { viewModel.selectFolder(it) },
                                        onHotpIncrement = { viewModel.incrementHotpCounter(it) },
                                        onDeleteAccount = { viewModel.deleteAccount(it) },
                                        onEditAccount = { account -> navController.navigate("account_detail/${account.id}") },
                                        onReorderAccounts = { viewModel.reorderAccounts(it) },
                                        onNavigateToScanQr = { navController.navigate("scan_qr") },
                                        onNavigateToManualEntry = { navController.navigate("manual_entry") },
                                        onNavigateToSettings = { navController.navigate("settings") },
                                        onNavigateToAccountDetail = { id -> navController.navigate("account_detail/$id") },
                                        onNavigateToPasswordVault = { navController.navigate("password_vault") },
                                        onNavigateToPasswordlessAuth = { navController.navigate("passwordless_auth") },
                                        onNavigateToWearableSync = { navController.navigate("wearable_sync") },
                                        userSettings = userSettings,
                                        timeAccuracyStatus = timeAccuracyStatus
                                    )
                                }

                                composable("scan_qr") {
                                    QrScannerScreen(
                                        onScanSuccess = { account ->
                                            viewModel.addAccount(account) {
                                                navController.popBackStack()
                                            }
                                        },
                                        onMigrationSuccess = { staged ->
                                            viewModel.stageMigrationAccounts(staged)
                                            navController.navigate("migration_review") {
                                                popUpTo("scan_qr") { inclusive = true }
                                            }
                                        },
                                        onBack = { navController.popBackStack() },
                                        userSettings = userSettings
                                    )
                                }

                                composable("manual_entry") {
                                    ManualEntryScreen(
                                        folders = folders,
                                        timeSeconds = timeSeconds,
                                        onSaveAccount = { account ->
                                            viewModel.addAccount(account) {
                                                navController.popBackStack()
                                            }
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable("migration_review") {
                                    MigrationReviewScreen(
                                        migratedAccounts = stagedMigrationAccounts,
                                        onCommitImport = { selectedSet ->
                                            viewModel.commitSelectedMigrationAccounts(selectedSet) {
                                                navController.navigate("home") {
                                                    popUpTo("home") { inclusive = true }
                                                }
                                            }
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable(
                                    route = "account_detail/{accountId}",
                                    arguments = listOf(navArgument("accountId") { type = NavType.LongType })
                                ) { backStackEntry ->
                                    val accountId = backStackEntry.arguments?.getLong("accountId") ?: 0L
                                    val account = accounts.find { it.id == accountId }

                                    if (account != null) {
                                        AccountDetailScreen(
                                            account = account,
                                            folders = folders,
                                            onUpdateAccount = { updated ->
                                                viewModel.updateAccount(updated) {
                                                    navController.popBackStack()
                                                }
                                            },
                                            onDeleteAccount = { acc ->
                                                viewModel.deleteAccount(acc)
                                                navController.popBackStack()
                                            },
                                            onCopyText = { text, label ->
                                                val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                                clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
                                            },
                                            onBack = { navController.popBackStack() }
                                        )
                                    }
                                }

                                composable("settings") {
                                    SettingsScreen(
                                        userSettings = userSettings,
                                        timeAccuracyStatus = timeAccuracyStatus,
                                        onSetPin = { pin ->
                                            lifecycleScope.launch { viewModel.settingsRepository.setPin(pin) }
                                        },
                                        onRemovePin = {
                                            lifecycleScope.launch { viewModel.settingsRepository.removePin() }
                                        },
                                        onSetBiometric = { enabled ->
                                            lifecycleScope.launch { viewModel.settingsRepository.setUseBiometric(enabled) }
                                        },
                                        onSetAutoLockTimeout = { secs ->
                                            lifecycleScope.launch { viewModel.settingsRepository.setAutoLockTimeout(secs) }
                                        },
                                        onSetBlurCodesDefault = { enabled ->
                                            lifecycleScope.launch { viewModel.settingsRepository.setBlurCodesByDefault(enabled) }
                                        },
                                        onSetThemeMode = { theme ->
                                            lifecycleScope.launch { viewModel.settingsRepository.setThemeMode(theme) }
                                         },
                                         onSetHapticsEnabled = { enabled ->
                                             lifecycleScope.launch { viewModel.settingsRepository.setHapticsEnabled(enabled) }
                                         },
                                         onSetHapticIntensity = { intensity ->
                                             lifecycleScope.launch { viewModel.settingsRepository.setHapticIntensity(intensity) }
                                         },
                                         onSetHapticDuration = { duration ->
                                             lifecycleScope.launch { viewModel.settingsRepository.setHapticDuration(duration) }
                                         },
                                         onSetCopyCodeHapticEnabled = { enabled ->
                                             lifecycleScope.launch { viewModel.settingsRepository.setCopyCodeHapticEnabled(enabled) }
                                         },
                                         onSetScanSuccessHapticEnabled = { enabled ->
                                             lifecycleScope.launch { viewModel.settingsRepository.setScanSuccessHapticEnabled(enabled) }
                                         },
                                         onSetErrorHapticEnabled = { enabled ->
                                             lifecycleScope.launch { viewModel.settingsRepository.setErrorHapticEnabled(enabled) }
                                        },
                                        onSetCheckTimeAccuracyEnabled = { enabled ->
                                            viewModel.setCheckTimeAccuracyEnabled(enabled)
                                        },
                                        onPerformTimeCheck = {
                                            viewModel.performTimeAccuracyCheck()
                                        },
                                        onNavigateToImportExport = {
                                            navController.navigate("import_export")
                                        },
                                        onExportBackup = { pwd, onSuccess, onError ->
                                            viewModel.exportBackup(pwd, onSuccess, onError)
                                        },
                                        onImportBackup = { payload, pwd, onSuccess, onError ->
                                            viewModel.importBackup(payload, pwd, onSuccess, onError)
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                 composable("import_export") {
                                    val currentAccounts by viewModel.accounts.collectAsState()
                                    val currentFolders by viewModel.allFolders.collectAsState()
                                    val googleDriveState by viewModel.googleDriveState.collectAsState()

                                    ImportExportScreen(
                                        accounts = currentAccounts,
                                        foldersCount = currentFolders.size,
                                        googleDriveState = googleDriveState,
                                        onConnectGoogleDrive = { email -> viewModel.connectGoogleDrive(email) },
                                        onDisconnectGoogleDrive = { viewModel.disconnectGoogleDrive() },
                                        onBackupToGoogleDrive = { pwd, onSuccess, onError ->
                                            viewModel.backupToGoogleDrive(pwd, onSuccess, onError)
                                        },
                                        onRestoreFromGoogleDrive = { pwd, onSuccess, onError ->
                                            viewModel.restoreFromGoogleDrive(
                                                password = pwd,
                                                onSuccess = { staged ->
                                                    onSuccess()
                                                    navController.navigate("migration_review")
                                                },
                                                onError = onError
                                            )
                                        },
                                        onParseUniversalImport = { rawContent, onSuccess, onError ->
                                            viewModel.parseAndStageUniversalImport(
                                                rawContent = rawContent,
                                                onSuccess = { count ->
                                                    onSuccess(count)
                                                    navController.navigate("migration_review")
                                                },
                                                onError = onError
                                            )
                                        },
                                        onGenerateExportBitwardenCsv = { viewModel.generateExportBitwardenCsv() },
                                        onGenerateExportStandardUris = { viewModel.generateExportStandardUris() },
                                        onGenerateExportStandardCsv = { viewModel.generateExportStandardCsv() },
                                        onNavigateToScanQr = { navController.navigate("scan_qr") },
                                        onDecryptAndStageBackup = { payload, password, onSuccess, onError ->
                                            viewModel.decryptBackupForReview(
                                                encryptedPayload = payload,
                                                password = password,
                                                onSuccess = { staged ->
                                                    onSuccess()
                                                    navController.navigate("migration_review")
                                                },
                                                onError = onError
                                            )
                                        },
                                        onExportBackup = { pwd, onSuccess, onError ->
                                            viewModel.exportBackup(pwd, onSuccess, onError)
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable("password_vault") {
                                    val passwords by viewModel.passwords.collectAsState()
                                    val currentAccounts by viewModel.accounts.collectAsState()

                                    PasswordVaultScreen(
                                        passwords = passwords,
                                        accounts = currentAccounts,
                                        onAddPassword = { viewModel.addPassword(it) },
                                        onUpdatePassword = { viewModel.updatePassword(it) },
                                        onDeletePassword = { viewModel.deletePassword(it) },
                                        onCopyText = { text, label ->
                                            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable("passwordless_auth") {
                                    PasswordlessLoginScreen(
                                        onBack = { navController.popBackStack() }
                                    )
                                }

                                composable("wearable_sync") {
                                    val currentAccounts by viewModel.accounts.collectAsState()

                                    WearableCompanionScreen(
                                        accounts = currentAccounts,
                                        timeSeconds = timeSeconds,
                                        onCopyText = { text, label ->
                                            val clipboard = getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                            clipboard.setPrimaryClip(android.content.ClipData.newPlainText(label, text))
                                        },
                                        onBack = { navController.popBackStack() }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

