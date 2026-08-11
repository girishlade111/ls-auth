package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.util.HapticAction
import com.example.util.HapticManager
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import com.example.util.TimeAccuracyStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    timeAccuracyStatus: TimeAccuracyStatus = TimeAccuracyStatus.Idle,
    onSetPin: (String) -> Unit,
    onRemovePin: () -> Unit,
    onSetBiometric: (Boolean) -> Unit,
    onSetAutoLockTimeout: (Int) -> Unit,
    onSetBlurCodesDefault: (Boolean) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onSetHapticsEnabled: (Boolean) -> Unit = {},
    onSetHapticIntensity: (String) -> Unit = {},
    onSetHapticDuration: (String) -> Unit = {},
    onSetCopyCodeHapticEnabled: (Boolean) -> Unit = {},
    onSetScanSuccessHapticEnabled: (Boolean) -> Unit = {},
    onSetErrorHapticEnabled: (Boolean) -> Unit = {},
    onSetCheckTimeAccuracyEnabled: (Boolean) -> Unit = {},
    onPerformTimeCheck: () -> Unit = {},
    onNavigateToImportExport: () -> Unit = {},
    onExportBackup: (String, (String) -> Unit, (String) -> Unit) -> Unit,
    onImportBackup: (String, String, (Int) -> Unit, (String) -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showPinDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var exportedPayloadDialog by remember { mutableStateOf<String?>(null) }
    var pendingJsonSaveContent by remember { mutableStateOf<String?>(null) }

    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            pendingJsonSaveContent?.let { content ->
                try {
                    context.contentResolver.openOutputStream(targetUri)?.use { stream ->
                        stream.write(content.toByteArray(Charsets.UTF_8))
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Encrypted JSON backup file saved!")
                    }
                    exportedPayloadDialog = null
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Error saving backup file: ${e.message}")
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings & Vault Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {

                // Section 1: SECURITY
                Text(
                    text = "SECURITY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // App Lock PIN
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "App Lock PIN",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (userSettings.isAppLockEnabled) "PIN protection active" else "App lock disabled",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Button(
                                onClick = {
                                    if (userSettings.isAppLockEnabled) {
                                        onRemovePin()
                                    } else {
                                        showPinDialog = true
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("app_lock_toggle_button")
                            ) {
                                Text(if (userSettings.isAppLockEnabled) "Disable" else "Setup PIN")
                            }
                        }

                        if (userSettings.isAppLockEnabled) {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Biometric Unlock",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Use Fingerprint or Face to unlock vault",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = userSettings.useBiometric,
                                    onCheckedChange = onSetBiometric,
                                    modifier = Modifier.testTag("biometric_switch")
                                )
                            }

                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "Auto-Lock Timeout",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val timeoutOptions = listOf(0 to "Immediate", 15 to "15 Sec", 60 to "1 Min", 300 to "5 Min")
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                timeoutOptions.forEachIndexed { index, (secs, label) ->
                                    SegmentedButton(
                                        selected = userSettings.autoLockTimeoutSeconds == secs,
                                        onClick = { onSetAutoLockTimeout(secs) },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = timeoutOptions.size)
                                    ) { Text(label) }
                                }
                            }
                        }

                        Divider(modifier = Modifier.padding(vertical = 12.dp))

                        // Blur / Hide Codes Default
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Blur / Hide Codes by Default",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Blur 2FA codes on home screen by default, requiring a tap to reveal them for enhanced privacy in public spaces.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = userSettings.blurCodesByDefault,
                                onCheckedChange = onSetBlurCodesDefault,
                                modifier = Modifier.testTag("blur_codes_switch")
                            )
                        }
                    }
                }

                // Section 2: BACKUP & RESTORE
                Text(
                    text = "BACKUP & RESTORE",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Encrypted Vault Backup",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Export vault encrypted with AES-256 + PBKDF2 master password.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = onNavigateToImportExport,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("open_import_export_screen_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Open Dedicated Import / Export Screen")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { showExportDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("export_backup_button")
                            ) {
                                Text("Quick Export")
                            }

                            OutlinedButton(
                                onClick = { showImportDialog = true },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("restore_backup_button")
                            ) {
                                Text("Quick Restore")
                            }
                        }
                    }
                }

                // Section 3: GENERAL
                Text(
                    text = "GENERAL",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                // Theme Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Appearance Theme",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Choose light, dark, or system default color scheme",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        val themes = listOf("SYSTEM" to "System", "LIGHT" to "Light", "DARK" to "Dark Slate")
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            themes.forEachIndexed { index, (key, label) ->
                                SegmentedButton(
                                    selected = userSettings.themeMode == key,
                                    onClick = { onSetThemeMode(key) },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = themes.size)
                                ) { Text(label) }
                            }
                        }
                    }
                }

                // Section: Time & Clock Accuracy
                Text(
                    text = "TIME & CLOCK ACCURACY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Check Time Accuracy",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Verify device clock drift against network servers to prevent invalid 2FA codes",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = userSettings.checkTimeAccuracyEnabled,
                                onCheckedChange = onSetCheckTimeAccuracyEnabled,
                                modifier = Modifier.testTag("check_time_accuracy_switch")
                            )
                        }

                        if (userSettings.checkTimeAccuracyEnabled) {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            when (timeAccuracyStatus) {
                                is TimeAccuracyStatus.Checking -> {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Checking network time accuracy...",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                is TimeAccuracyStatus.Success -> {
                                    val isWarning = timeAccuracyStatus.isWarning
                                    val bannerBg = if (isWarning) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                                    val bannerContent = if (isWarning) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer

                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = bannerBg,
                                        contentColor = bannerContent,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Column(modifier = Modifier.padding(12.dp)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = if (isWarning) Icons.Default.Warning else Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = if (isWarning) "Clock Drift Warning" else "Clock Synchronized",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = timeAccuracyStatus.message,
                                                style = MaterialTheme.typography.bodySmall
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedButton(
                                        onClick = onPerformTimeCheck,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("recheck_time_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Re-verify Clock Accuracy")
                                    }
                                }

                                is TimeAccuracyStatus.Error -> {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = timeAccuracyStatus.message,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    OutlinedButton(
                                        onClick = onPerformTimeCheck,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("retry_time_check_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Retry Time Check")
                                    }
                                }

                                else -> {
                                    OutlinedButton(
                                        onClick = onPerformTimeCheck,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("start_time_check_button"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Perform Time Accuracy Check")
                                    }
                                }
                            }
                        }
                    }
                }

                // Section: Haptic Feedback & Vibration
                Text(
                    text = "HAPTIC FEEDBACK & VIBRATION",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Haptic Feedback",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Touch vibration feedback for code copying, QR scanning & errors",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Switch(
                                checked = userSettings.hapticsEnabled,
                                onCheckedChange = onSetHapticsEnabled,
                                modifier = Modifier.testTag("haptics_master_switch")
                            )
                        }

                        if (userSettings.hapticsEnabled) {
                            Divider(modifier = Modifier.padding(vertical = 12.dp))

                            Text(
                                text = "Vibration Intensity",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val intensities = listOf("LIGHT" to "Soft", "MEDIUM" to "Normal", "STRONG" to "Strong")
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                intensities.forEachIndexed { index, (key, label) ->
                                    SegmentedButton(
                                        selected = userSettings.hapticIntensity == key,
                                        onClick = {
                                            onSetHapticIntensity(key)
                                            val updated = userSettings.copy(hapticIntensity = key)
                                            HapticManager.performHaptic(context, HapticAction.COPY_CODE, updated)
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = intensities.size)
                                    ) { Text(label) }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Vibration Duration",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            val durations = listOf("SHORT" to "Short", "MEDIUM" to "Medium", "LONG" to "Extended")
                            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                                durations.forEachIndexed { index, (key, label) ->
                                    SegmentedButton(
                                        selected = userSettings.hapticDuration == key,
                                        onClick = {
                                            onSetHapticDuration(key)
                                            val updated = userSettings.copy(hapticDuration = key)
                                            HapticManager.performHaptic(context, HapticAction.COPY_CODE, updated)
                                        },
                                        shape = SegmentedButtonDefaults.itemShape(index = index, count = durations.size)
                                    ) { Text(label) }
                                }
                            }

                            Divider(modifier = Modifier.padding(vertical = 14.dp))

                            Text(
                                text = "ACTION CUSTOMIZATION & TEST",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // Copy Code Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Copy Passcode",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Haptic feedback when tapping to copy passcode",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = userSettings.copyCodeHapticEnabled,
                                    onCheckedChange = onSetCopyCodeHapticEnabled,
                                    modifier = Modifier.testTag("copy_haptic_switch")
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    HapticManager.performHaptic(context, HapticAction.COPY_CODE, userSettings)
                                },
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Test Copy Haptic")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Scan Success Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Successful Scan",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Double pulse vibration on successful QR scan",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = userSettings.scanSuccessHapticEnabled,
                                    onCheckedChange = onSetScanSuccessHapticEnabled,
                                    modifier = Modifier.testTag("scan_haptic_switch")
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    HapticManager.performHaptic(context, HapticAction.SCAN_SUCCESS, userSettings)
                                },
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Test Scan Success Haptic")
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Error State Action
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Error Alerts",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Triple alert burst on invalid QR or auth error",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = userSettings.errorHapticEnabled,
                                    onCheckedChange = onSetErrorHapticEnabled,
                                    modifier = Modifier.testTag("error_haptic_switch")
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    HapticManager.performHaptic(context, HapticAction.ERROR_STATE, userSettings)
                                },
                                modifier = Modifier
                                    .padding(top = 6.dp)
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Test Error Haptic")
                            }
                        }
                    }
                }

                // Section 4: ABOUT
                Text(
                    text = "ABOUT",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Authenticator v2.4.0",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Zero-Trust Offline Vault",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "• Hardware-Backed Android KeyStore AES-256-GCM Encryption at rest\n" +
                                    "• Zero Network Permission declared in AndroidManifest\n" +
                                    "• Screen Capture / Recents Blur protected via FLAG_SECURE\n" +
                                    "• PBKDF2 SHA-256 key derivation with 50,000 salt iterations",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 20.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Setup PIN Dialog
        if (showPinDialog) {
            var newPin by remember { mutableStateOf("") }
            var confirmPin by remember { mutableStateOf("") }
            var error by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showPinDialog = false },
                title = { Text("Create 6-Digit App Lock PIN") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newPin,
                            onValueChange = { if (it.length <= 6) newPin = it },
                            label = { Text("Enter 6-Digit PIN") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = confirmPin,
                            onValueChange = { if (it.length <= 6) confirmPin = it },
                            label = { Text("Confirm PIN") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (error.isNotEmpty()) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newPin.length < 4) {
                                error = "PIN must be at least 4 digits"
                            } else if (newPin != confirmPin) {
                                error = "PINs do not match"
                            } else {
                                onSetPin(newPin)
                                showPinDialog = false
                            }
                        }
                    ) {
                        Text("Save PIN")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showPinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Export Dialog
        if (showExportDialog) {
            var exportPassword by remember { mutableStateOf("") }
            var exportError by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Export Encrypted Backup") },
                text = {
                    Column {
                        Text(
                            text = "Set a master password to encrypt your backup file. You will need this password when restoring.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = exportPassword,
                            onValueChange = { exportPassword = it },
                            label = { Text("Encryption Master Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (exportError.isNotEmpty()) {
                            Text(
                                text = exportError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (exportPassword.length < 6) {
                                exportError = "Password must be at least 6 characters"
                            } else {
                                onExportBackup(
                                    exportPassword,
                                    { payload ->
                                        showExportDialog = false
                                        exportedPayloadDialog = payload
                                    },
                                    { err -> exportError = err }
                                )
                            }
                        }
                    ) {
                        Text("Generate Backup")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showExportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Display Generated Backup Payload / JSON File Export Dialog
        exportedPayloadDialog?.let { payload ->
            AlertDialog(
                onDismissRequest = { exportedPayloadDialog = null },
                title = { Text("Encrypted JSON Backup Ready") },
                text = {
                    Column {
                        Text(
                            text = "Save your AES-256 encrypted JSON backup file or copy the backup content below:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    pendingJsonSaveContent = payload
                                    createDocumentLauncher.launch("authenticator_backup.json")
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("save_json_file_button")
                            ) {
                                Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save JSON File", fontSize = 12.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    try {
                                        val sendIntent = Intent().apply {
                                            action = Intent.ACTION_SEND
                                            putExtra(Intent.EXTRA_TEXT, payload)
                                            type = "application/json"
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share Encrypted Backup"))
                                    } catch (e: Exception) {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Share failed: ${e.message}")
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Share", fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = payload,
                                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                                maxLines = 6,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("LS Auth Backup JSON", payload))
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Encrypted JSON copied to clipboard!")
                            }
                            exportedPayloadDialog = null
                        }
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy JSON Text")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { exportedPayloadDialog = null }) {
                        Text("Close")
                    }
                }
            )
        }

        // Restore Backup Dialog
        if (showImportDialog) {
            var importPayload by remember { mutableStateOf("") }
            var importPassword by remember { mutableStateOf("") }
            var importError by remember { mutableStateOf("") }

            val openDocumentLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                uri?.let { sourceUri ->
                    try {
                        val content = context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.use { it.readText() }
                        if (!content.isNullOrBlank()) {
                            importPayload = content
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Loaded backup file contents!")
                            }
                        }
                    } catch (e: Exception) {
                        importError = "Failed to read backup file: ${e.message}"
                    }
                }
            }

            AlertDialog(
                onDismissRequest = { showImportDialog = false },
                title = { Text("Restore Encrypted Backup") },
                text = {
                    Column {
                        Text(
                            text = "Select an encrypted JSON backup file or paste the JSON text below:",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = { openDocumentLauncher.launch(arrayOf("application/json", "text/plain", "*/*")) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("select_backup_file_button")
                        ) {
                            Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pick JSON Backup File")
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = importPayload,
                            onValueChange = { importPayload = it },
                            label = { Text("JSON Backup Text") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 4
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedTextField(
                            value = importPassword,
                            onValueChange = { importPassword = it },
                            label = { Text("Master Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (importError.isNotEmpty()) {
                            Text(
                                text = importError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (importPayload.isBlank()) {
                                importError = "Please select a backup file or paste JSON text"
                            } else if (importPassword.isBlank()) {
                                importError = "Please enter password"
                            } else {
                                onImportBackup(
                                    importPayload.trim(),
                                    importPassword,
                                    { count ->
                                        showImportDialog = false
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Successfully restored $count 2FA accounts into vault!")
                                        }
                                    },
                                    { err -> importError = err }
                                )
                            }
                        }
                    ) {
                        Text("Decrypt & Restore")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showImportDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
