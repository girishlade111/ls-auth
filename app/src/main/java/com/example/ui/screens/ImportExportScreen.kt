package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.GoogleDriveState
import com.example.model.Account
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportScreen(
    accounts: List<Account>,
    foldersCount: Int = 0,
    googleDriveState: GoogleDriveState = GoogleDriveState(),
    onConnectGoogleDrive: (email: String) -> Unit = {},
    onDisconnectGoogleDrive: () -> Unit = {},
    onBackupToGoogleDrive: (password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onRestoreFromGoogleDrive: (password: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onParseUniversalImport: (rawContent: String, onSuccess: (Int) -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onGenerateExportBitwardenCsv: () -> String = { "" },
    onGenerateExportStandardUris: () -> String = { "" },
    onGenerateExportStandardCsv: () -> String = { "" },
    onNavigateToScanQr: () -> Unit = {},
    onDecryptAndStageBackup: (payload: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _, _ -> },
    onExportBackup: (password: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) -> Unit = { _, _, _ -> },
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedTabIndex by remember { mutableStateOf(0) } // 0: Google Drive, 1: Import, 2: Export

    // Google Drive dialog state
    var showConnectDriveDialog by remember { mutableStateOf(false) }
    var driveEmailInput by remember { mutableStateOf("") }
    var drivePasswordInput by remember { mutableStateOf("") }
    var driveActionError by remember { mutableStateOf("") }

    // Universal Import State
    var importContentText by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf("") }
    var importMasterPassword by remember { mutableStateOf("") }

    // Export Dialog State
    var pendingExportTitle by remember { mutableStateOf("") }
    var pendingExportPayload by remember { mutableStateOf<String?>(null) }
    var exportPasswordInput by remember { mutableStateOf("") }
    var exportError by remember { mutableStateOf("") }

    // Open file launcher for import
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { sourceUri ->
            try {
                val content = context.contentResolver.openInputStream(sourceUri)?.bufferedReader()?.use { it.readText() }
                if (!content.isNullOrBlank()) {
                    importContentText = content
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Loaded file contents!")
                    }
                }
            } catch (e: Exception) {
                importError = "Failed to read file: ${e.localizedMessage}"
            }
        }
    }

    // Save document launcher for export
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri: Uri? ->
        uri?.let { targetUri ->
            pendingExportPayload?.let { payload ->
                try {
                    context.contentResolver.openOutputStream(targetUri)?.use { outStream ->
                        outStream.write(payload.toByteArray(Charsets.UTF_8))
                    }
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("File saved successfully!")
                    }
                } catch (e: Exception) {
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("Failed to save file: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Backup, Import & Export", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTabIndex) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Google Drive (Optional)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Import Codes", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = selectedTabIndex == 2,
                    onClick = { selectedTabIndex = 2 },
                    text = { Text("Export Codes", fontWeight = FontWeight.SemiBold, fontSize = 12.sp) },
                    icon = { Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        // TAB 0: GOOGLE DRIVE BACKUP (OPTIONAL)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "Google Drive Backup",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = MaterialTheme.colorScheme.tertiaryContainer
                                            ) {
                                                Text(
                                                    text = "OPTIONAL",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                                )
                                            }
                                        }
                                        Text(
                                            text = "App is 100% offline and client-side by default. Connecting Google Drive is completely optional.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (!googleDriveState.isConnected) {
                            // Disconnected State
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudOff,
                                        contentDescription = null,
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.outline
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Text(
                                        text = "No Google Account Linked",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "Link your Google account to enable optional cloud backups. Your vault data will be encrypted with AES-256 before upload.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = { showConnectDriveDialog = true },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("link_google_drive_button"),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.CloudQueue, contentDescription = null)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Connect Google Drive (Optional)")
                                    }
                                }
                            }
                        } else {
                            // Connected State
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = Color(0xFF2E7D32),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "Connected Account",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                            Text(
                                                text = googleDriveState.accountEmail,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        OutlinedButton(
                                            onClick = onDisconnectGoogleDrive,
                                            shape = RoundedCornerShape(20.dp)
                                        ) {
                                            Text("Disconnect", fontSize = 12.sp)
                                        }
                                    }

                                    Divider(modifier = Modifier.padding(vertical = 12.dp))

                                    val lastBackupFormatted = if (googleDriveState.lastBackupTimestamp > 0) {
                                        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                                        sdf.format(Date(googleDriveState.lastBackupTimestamp))
                                    } else {
                                        "Never"
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Last Drive Backup:", style = MaterialTheme.typography.bodyMedium)
                                        Text(lastBackupFormatted, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Backed Up Items:", style = MaterialTheme.typography.bodyMedium)
                                        Text("${googleDriveState.lastBackupItemCount} accounts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = "Master Password for Drive Encryption:",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    OutlinedTextField(
                                        value = drivePasswordInput,
                                        onValueChange = { drivePasswordInput = it },
                                        label = { Text("Master Encryption Password") },
                                        singleLine = true,
                                        visualTransformation = PasswordVisualTransformation(),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    if (driveActionError.isNotEmpty()) {
                                        Text(
                                            text = driveActionError,
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.padding(top = 6.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                if (drivePasswordInput.isBlank()) {
                                                    driveActionError = "Enter master password to encrypt backup"
                                                } else {
                                                    onBackupToGoogleDrive(
                                                        drivePasswordInput,
                                                        { msg ->
                                                            driveActionError = ""
                                                            coroutineScope.launch { snackbarHostState.showSnackbar(msg) }
                                                        },
                                                        { err -> driveActionError = err }
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("backup_to_drive_button"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Backup Now")
                                        }

                                        OutlinedButton(
                                            onClick = {
                                                if (drivePasswordInput.isBlank()) {
                                                    driveActionError = "Enter master password to decrypt backup"
                                                } else {
                                                    onRestoreFromGoogleDrive(
                                                        drivePasswordInput,
                                                        {
                                                            driveActionError = ""
                                                            coroutineScope.launch { snackbarHostState.showSnackbar("Google Drive backup restored!") }
                                                        },
                                                        { err -> driveActionError = err }
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("restore_from_drive_button"),
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Restore")
                                        }
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: IMPORT CODES (Bitwarden, Google Auth, Microsoft Auth, Zoho OneAuth, CSV, URIs)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Supported Apps & Sources",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Import codes seamlessly from popular authenticators:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF175DDC).copy(alpha = 0.15f)) {
                                        Text("Bitwarden (CSV/JSON)", Modifier.padding(8.dp, 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF175DDC))
                                    }
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF4285F4).copy(alpha = 0.15f)) {
                                        Text("Google Auth (QR)", Modifier.padding(8.dp, 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1967D2))
                                    }
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFF0078D4).copy(alpha = 0.15f)) {
                                        Text("Microsoft Auth", Modifier.padding(8.dp, 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0078D4))
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFD32F2F).copy(alpha = 0.15f)) {
                                        Text("Zoho OneAuth", Modifier.padding(8.dp, 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD32F2F))
                                    }
                                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                        Text("Standard OTP URIs", Modifier.padding(8.dp, 4.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 14.dp))

                                Button(
                                    onClick = onNavigateToScanQr,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Scan Google Auth Migration QR Code")
                                }

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedButton(
                                    onClick = { openDocumentLauncher.launch(arrayOf("text/plain", "text/csv", "application/json", "*/*")) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileUpload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Select Export File (CSV / JSON)")
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = importContentText,
                                    onValueChange = { importContentText = it },
                                    label = { Text("Paste CSV, JSON, or otpauth:// URIs") },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 5
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                OutlinedTextField(
                                    value = importMasterPassword,
                                    onValueChange = { importMasterPassword = it },
                                    label = { Text("Password (If file is encrypted JSON)") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (importContentText.isBlank()) {
                                            importError = "Please select a file or paste import data"
                                        } else {
                                            // Check if it's encrypted JSON first
                                            if (importContentText.contains("AES-256") || importMasterPassword.isNotBlank()) {
                                                onDecryptAndStageBackup(
                                                    importContentText.trim(),
                                                    importMasterPassword,
                                                    { importError = "" },
                                                    { err -> importError = err }
                                                )
                                            } else {
                                                onParseUniversalImport(
                                                    importContentText.trim(),
                                                    { count ->
                                                        importError = ""
                                                        coroutineScope.launch {
                                                            snackbarHostState.showSnackbar("Parsed $count accounts! Ready for review.")
                                                        }
                                                    },
                                                    { err -> importError = err }
                                                )
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("parse_and_stage_import_button"),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Parse & Review Accounts")
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: EXPORT CODES (To Bitwarden, Google Auth, Microsoft Auth, Zoho OneAuth, CSV, Encrypted JSON)
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Export Destination Formats",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Export your ${accounts.size} 2FA accounts into formats compatible with other popular authenticators:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                // Option A: Bitwarden CSV
                                OutlinedButton(
                                    onClick = {
                                        val payload = onGenerateExportBitwardenCsv()
                                        pendingExportTitle = "Bitwarden Compatible CSV Export"
                                        pendingExportPayload = payload
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null, tint = Color(0xFF175DDC))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export for Bitwarden (CSV)")
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Option B: Standard OTP URIs
                                OutlinedButton(
                                    onClick = {
                                        val payload = onGenerateExportStandardUris()
                                        pendingExportTitle = "Standard OTP URIs Export"
                                        pendingExportPayload = payload
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export URIs (Microsoft / Zoho / Google Auth)")
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Option C: Standard CSV
                                OutlinedButton(
                                    onClick = {
                                        val payload = onGenerateExportStandardCsv()
                                        pendingExportTitle = "Standard 2FA CSV Export"
                                        pendingExportPayload = payload
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.FileDownload, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Export Standard CSV")
                                }

                                Divider(modifier = Modifier.padding(vertical = 14.dp))

                                Text(
                                    text = "Encrypted Database Backup (AES-256)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = exportPasswordInput,
                                    onValueChange = { exportPasswordInput = it },
                                    label = { Text("Set Encryption Password") },
                                    singleLine = true,
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                if (exportError.isNotEmpty()) {
                                    Text(
                                        text = exportError,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(top = 6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Button(
                                    onClick = {
                                        if (exportPasswordInput.isBlank()) {
                                            exportError = "Please enter password to encrypt backup file"
                                        } else {
                                            onExportBackup(
                                                exportPasswordInput,
                                                { json ->
                                                    exportError = ""
                                                    pendingExportTitle = "Encrypted Vault Database Backup"
                                                    pendingExportPayload = json
                                                },
                                                { err -> exportError = err }
                                            )
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Icon(Icons.Default.Lock, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Generate Encrypted Backup")
                                }
                            }
                        }
                    }
                }
            }
        }

        // CONNECT GOOGLE DRIVE DIALOG
        if (showConnectDriveDialog) {
            AlertDialog(
                onDismissRequest = { showConnectDriveDialog = false },
                title = { Text("Connect Google Drive (Optional)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = "Enter your Google account email address to enable optional Google Drive cloud sync:",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        OutlinedTextField(
                            value = driveEmailInput,
                            onValueChange = { driveEmailInput = it },
                            label = { Text("Google Account Email") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("user@gmail.com") }
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val email = driveEmailInput.ifBlank { "user@gmail.com" }
                            onConnectGoogleDrive(email)
                            showConnectDriveDialog = false
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar("Connected Google account ($email)!")
                            }
                        }
                    ) {
                        Text("Connect Account")
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { showConnectDriveDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // EXPORT REVIEW & ACTION DIALOG
        pendingExportPayload?.let { payload ->
            AlertDialog(
                onDismissRequest = { pendingExportPayload = null },
                title = { Text(pendingExportTitle) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Export payload generated successfully (${accounts.size} accounts):",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = payload,
                                modifier = Modifier.padding(10.dp),
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 8
                            )
                        }
                    }
                },
                confirmButton = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Export Payload", payload))
                                coroutineScope.launch { snackbarHostState.showSnackbar("Copied export payload to clipboard!") }
                                pendingExportPayload = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Copy to Clipboard")
                        }

                        OutlinedButton(
                            onClick = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(Intent.EXTRA_TEXT, payload)
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Export Data"))
                                pendingExportPayload = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share via App")
                        }

                        OutlinedButton(
                            onClick = {
                                createDocumentLauncher.launch("2fa_export.txt")
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save as File")
                        }
                    }
                },
                dismissButton = {
                    OutlinedButton(onClick = { pendingExportPayload = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}
