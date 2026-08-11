package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.Base32
import com.example.crypto.HashAlgorithm
import com.example.crypto.OtpType
import com.example.data.FolderEntity
import com.example.model.Account
import com.example.model.AppIconPack
import com.example.ui.components.IconPackPickerDialog
import com.example.ui.components.ServiceIconView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualEntryScreen(
    folders: List<FolderEntity>,
    timeSeconds: Long,
    onSaveAccount: (Account) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var issuer by remember { mutableStateOf("") }
    var accountName by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var isSecretVisible by remember { mutableStateOf(false) }

    var selectedIconKey by remember { mutableStateOf<String?>(null) }
    var showIconPackPicker by remember { mutableStateOf(false) }

    var selectedType by remember { mutableStateOf(OtpType.TOTP) }
    var selectedAlgorithm by remember { mutableStateOf(HashAlgorithm.SHA1) }
    var selectedDigits by remember { mutableIntStateOf(6) }
    var periodSeconds by remember { mutableIntStateOf(30) }
    var hotpCounter by remember { mutableLongStateOf(0L) }
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }

    var isAdvancedExpanded by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val cleanSecret = remember(secretKey) {
        secretKey.replace("\\s+".toRegex(), "").replace("-", "").uppercase()
    }

    val isValidBase32 = remember(cleanSecret) {
        if (cleanSecret.isEmpty()) false
        else Base32.isValidBase32(cleanSecret)
    }

    // Preview Account
    val previewAccount = remember(cleanSecret, issuer, accountName, selectedType, selectedAlgorithm, selectedDigits, periodSeconds, hotpCounter, selectedIconKey) {
        if (isValidBase32) {
            Account(
                accountName = accountName.ifBlank { "Account" },
                issuer = issuer.ifBlank { "Service" },
                secretBase32 = cleanSecret,
                type = selectedType,
                algorithm = selectedAlgorithm,
                digits = selectedDigits,
                period = periodSeconds,
                counter = hotpCounter,
                iconKey = selectedIconKey
            )
        } else null
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Manual Secret Entry", fontWeight = FontWeight.Bold) },
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                // Service / Issuer
                OutlinedTextField(
                    value = issuer,
                    onValueChange = { issuer = it },
                    label = { Text("Service or Issuer (e.g. Google, GitHub)") },
                    placeholder = { Text("Google") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_issuer"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                )

                // Account Name / Username
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text("Account Name / Email") },
                    placeholder = { Text("user@example.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_account_name"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                )

                // App Icon Pack Selector Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            ServiceIconView(
                                issuer = issuer.ifBlank { "Service" },
                                accountName = accountName.ifBlank { "Account" },
                                iconKey = selectedIconKey,
                                size = 42.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                val matched = AppIconPack.resolveIcon(issuer = issuer, accountName = accountName, iconKey = selectedIconKey)
                                Text(
                                    text = if (selectedIconKey != null && matched != null) "Icon: ${matched.name}"
                                           else if (matched != null) "Auto-matched: ${matched.name}"
                                           else "Default Monogram",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Choose from 60+ popular app icons",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        OutlinedButton(
                            onClick = { showIconPackPicker = true },
                            modifier = Modifier.testTag("choose_icon_pack_button"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.GridView, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Icon Pack")
                        }
                    }
                }

                // Secret Key
                OutlinedTextField(
                    value = secretKey,
                    onValueChange = {
                        secretKey = it
                        errorMessage = null
                    },
                    label = { Text("Secret Key (Base32)") },
                    placeholder = { Text("JBSWY3DPEHPK3PXP") },
                    singleLine = true,
                    isError = secretKey.isNotEmpty() && !isValidBase32,
                    trailingIcon = {
                        IconButton(onClick = { isSecretVisible = !isSecretVisible }) {
                            Icon(
                                imageVector = if (isSecretVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Toggle Secret Visibility"
                            )
                        }
                    },
                    visualTransformation = if (isSecretVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_secret_key"),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii,
                        capitalization = KeyboardCapitalization.Characters
                    )
                )

                if (secretKey.isNotEmpty() && !isValidBase32) {
                    Text(
                        text = "Invalid Base32 string (must contain A-Z, 2-7)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Live Preview Card
                previewAccount?.let { acc ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Live Code Preview",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = acc.getFormattedCode(timeSeconds),
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Advanced Settings Toggle Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { isAdvancedExpanded = !isAdvancedExpanded }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Advanced Settings (Algorithm, Digits, Period)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Icon(
                        imageVector = if (isAdvancedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Expand Advanced",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                // Advanced Settings Collapsible Body
                AnimatedVisibility(visible = isAdvancedExpanded) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        // Type Selector (TOTP vs HOTP)
                        Text("Type", style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = selectedType == OtpType.TOTP,
                                onClick = { selectedType = OtpType.TOTP },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("TOTP (Time)") }
                            SegmentedButton(
                                selected = selectedType == OtpType.HOTP,
                                onClick = { selectedType = OtpType.HOTP },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("HOTP (Counter)") }
                        }

                        // Hash Algorithm
                        Text("Algorithm", style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            HashAlgorithm.entries.forEachIndexed { index, algo ->
                                SegmentedButton(
                                    selected = selectedAlgorithm == algo,
                                    onClick = { selectedAlgorithm = algo },
                                    shape = SegmentedButtonDefaults.itemShape(index = index, count = HashAlgorithm.entries.size)
                                ) { Text(algo.name) }
                            }
                        }

                        // Digits (6 vs 8)
                        Text("Digits", style = MaterialTheme.typography.labelLarge)
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = selectedDigits == 6,
                                onClick = { selectedDigits = 6 },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("6 Digits") }
                            SegmentedButton(
                                selected = selectedDigits == 8,
                                onClick = { selectedDigits = 8 },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("8 Digits") }
                        }

                        // Period or Counter
                        if (selectedType == OtpType.TOTP) {
                            OutlinedTextField(
                                value = periodSeconds.toString(),
                                onValueChange = { periodSeconds = it.toIntOrNull() ?: 30 },
                                label = { Text("Period (Seconds)") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            OutlinedTextField(
                                value = hotpCounter.toString(),
                                onValueChange = { hotpCounter = it.toLongOrNull() ?: 0L },
                                label = { Text("Initial Counter Value") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                errorMessage?.let { errorText ->
                    Text(
                        text = errorText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Save Button
                Button(
                    onClick = {
                        if (issuer.isBlank() && accountName.isBlank()) {
                            errorMessage = "Please enter either an Issuer or Account Name"
                            return@Button
                        }
                        if (!isValidBase32) {
                            errorMessage = "Please enter a valid Base32 secret key"
                            return@Button
                        }

                        val accountToSave = Account(
                            accountName = accountName.ifBlank { "Account" },
                            issuer = issuer.ifBlank { "" },
                            secretBase32 = cleanSecret,
                            type = selectedType,
                            algorithm = selectedAlgorithm,
                            digits = selectedDigits,
                            period = periodSeconds,
                            counter = hotpCounter,
                            folderId = selectedFolderId,
                            iconKey = selectedIconKey
                        )
                        onSaveAccount(accountToSave)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("save_account_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = isValidBase32
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Account", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        if (showIconPackPicker) {
            IconPackPickerDialog(
                selectedIconKey = selectedIconKey ?: AppIconPack.resolveIcon(issuer = issuer, accountName = accountName)?.id,
                onSelectIcon = { key ->
                    selectedIconKey = key
                },
                onDismiss = { showIconPackPicker = false }
            )
        }
    }
}
