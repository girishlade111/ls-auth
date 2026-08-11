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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.PasswordEntity
import com.example.model.Account
import com.example.ui.components.ServiceIconView
import com.example.util.PasswordGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordVaultScreen(
    passwords: List<PasswordEntity>,
    accounts: List<Account>,
    onAddPassword: (PasswordEntity) -> Unit,
    onUpdatePassword: (PasswordEntity) -> Unit,
    onDeletePassword: (Long) -> Unit,
    onCopyText: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var searchQuery by remember { mutableStateOf("") }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var editingPassword by remember { mutableStateOf<PasswordEntity?>(null) }
    var showGeneratorDialog by remember { mutableStateOf(false) }

    val filteredPasswords = remember(passwords, searchQuery) {
        if (searchQuery.isBlank()) passwords
        else passwords.filter {
            it.title.contains(searchQuery, ignoreCase = true) ||
                    it.username.contains(searchQuery, ignoreCase = true) ||
                    it.website.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Password Vault", fontWeight = FontWeight.Bold)
                        Text(
                            text = "${passwords.size} credentials stored",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("password_vault_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { showGeneratorDialog = true },
                        modifier = Modifier.testTag("open_password_generator_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Password Generator",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingPassword = null
                    showAddEditDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_password_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Credential")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("password_search_input"),
                placeholder = { Text("Search logins, domains, usernames...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Text("✕", fontWeight = FontWeight.Bold)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (filteredPasswords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Key,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                        )
                        Text(
                            text = if (searchQuery.isBlank()) "No passwords stored yet" else "No matching credentials",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Tap + to save login credentials or generate strong passwords.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredPasswords, key = { it.id }) { item ->
                        PasswordCardItem(
                            item = item,
                            linkedAccount = accounts.find { it.id == item.linkedAccountId },
                            onCopyUsername = {
                                onCopyText(item.username, "Username")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Username copied")
                                }
                            },
                            onCopyPassword = {
                                onCopyText(item.password, "Password")
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Password copied to clipboard")
                                }
                            },
                            onEdit = {
                                editingPassword = item
                                showAddEditDialog = true
                            },
                            onDelete = {
                                onDeletePassword(item.id)
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Credential deleted")
                                }
                            }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (showAddEditDialog) {
        AddEditPasswordDialog(
            initialItem = editingPassword,
            accounts = accounts,
            onDismiss = { showAddEditDialog = false },
            onSave = { entity ->
                if (editingPassword == null) {
                    onAddPassword(entity)
                } else {
                    onUpdatePassword(entity)
                }
                showAddEditDialog = false
            }
        )
    }

    if (showGeneratorDialog) {
        PasswordGeneratorDialog(
            onDismiss = { showGeneratorDialog = false },
            onUsePassword = { generated ->
                onCopyText(generated, "Generated Password")
                coroutineScope.launch {
                    snackbarHostState.showSnackbar("Generated password copied to clipboard!")
                }
                showGeneratorDialog = false
            }
        )
    }
}

@Composable
fun PasswordCardItem(
    item: PasswordEntity,
    linkedAccount: Account?,
    onCopyUsername: () -> Unit,
    onCopyPassword: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var isPasswordVisible by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("password_item_card_${item.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ServiceIconView(
                        issuer = item.title,
                        accountName = item.username,
                        iconKey = item.iconKey,
                        size = 40.dp
                    )
                    Column {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (item.website.isNotBlank()) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = item.website,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Username Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = item.username.ifBlank { "No username" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
                IconButton(onClick = onCopyUsername, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Username",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Password Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (isPasswordVisible) item.password else "••••••••••••",
                        style = MaterialTheme.typography.bodyMedium,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle password visibility",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(onClick = onCopyPassword, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Password",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            // Linked 2FA Indicator if present
            if (linkedAccount != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Linked 2FA: ${linkedAccount.issuer.ifBlank { linkedAccount.accountName }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditPasswordDialog(
    initialItem: PasswordEntity?,
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (PasswordEntity) -> Unit
) {
    var title by remember { mutableStateOf(initialItem?.title ?: "") }
    var username by remember { mutableStateOf(initialItem?.username ?: "") }
    var password by remember { mutableStateOf(initialItem?.password ?: "") }
    var website by remember { mutableStateOf(initialItem?.website ?: "") }
    var notes by remember { mutableStateOf(initialItem?.notes ?: "") }
    var selectedAccountId by remember { mutableStateOf<Long?>(initialItem?.linkedAccountId) }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialItem == null) "New Credential" else "Edit Credential") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title / Service Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_dialog_title_input")
                )

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username or Email") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("password_dialog_username_input")
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        singleLine = true,
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("password_dialog_password_input")
                    )

                    Button(
                        onClick = {
                            password = PasswordGenerator.generate(PasswordGenerator.GeneratorOptions(length = 18))
                            isPasswordVisible = true
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "Generate")
                    }
                }

                OutlinedTextField(
                    value = website,
                    onValueChange = { website = it },
                    label = { Text("Website Domain (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Link to 2FA dropdown
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = !dropdownExpanded }
                ) {
                    val selectedAcc = accounts.find { it.id == selectedAccountId }
                    OutlinedTextField(
                        value = selectedAcc?.let { "${it.issuer} (${it.accountName})" } ?: "None (Standalone)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Link 2FA Token") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("None (Standalone)") },
                            onClick = {
                                selectedAccountId = null
                                dropdownExpanded = false
                            }
                        )
                        accounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.issuer.ifBlank { "Service" }} (${acc.accountName})") },
                                onClick = {
                                    selectedAccountId = acc.id
                                    dropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = PasswordEntity(
                        id = initialItem?.id ?: 0L,
                        title = title.ifBlank { "Account" },
                        username = username,
                        password = password,
                        website = website,
                        notes = notes,
                        linkedAccountId = selectedAccountId,
                        createdAt = initialItem?.createdAt ?: System.currentTimeMillis(),
                        updatedAt = System.currentTimeMillis()
                    )
                    onSave(updated)
                },
                enabled = title.isNotBlank() && password.isNotBlank(),
                modifier = Modifier.testTag("password_dialog_save_button")
            ) {
                Text("Save Credential")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun PasswordGeneratorDialog(
    onDismiss: () -> Unit,
    onUsePassword: (String) -> Unit
) {
    var length by remember { mutableFloatStateOf(16f) }
    var useUpper by remember { mutableStateOf(true) }
    var useLower by remember { mutableStateOf(true) }
    var useNumbers by remember { mutableStateOf(true) }
    var useSymbols by remember { mutableStateOf(true) }
    var avoidAmbiguous by remember { mutableStateOf(true) }

    var generatedPassword by remember {
        mutableStateOf(
            PasswordGenerator.generate(
                PasswordGenerator.GeneratorOptions(
                    length = 16,
                    includeUppercase = true,
                    includeLowercase = true,
                    includeNumbers = true,
                    includeSymbols = true,
                    avoidAmbiguous = true
                )
            )
        )
    }

    val strength = remember(generatedPassword) {
        PasswordGenerator.calculateStrength(generatedPassword)
    }

    fun regenerate() {
        generatedPassword = PasswordGenerator.generate(
            PasswordGenerator.GeneratorOptions(
                length = length.toInt(),
                includeUppercase = useUpper,
                includeLowercase = useLower,
                includeNumbers = useNumbers,
                includeSymbols = useSymbols,
                avoidAmbiguous = avoidAmbiguous
            )
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text("Password Generator")
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Generated Password Box
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = generatedPassword,
                                style = MaterialTheme.typography.titleMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { regenerate() }) {
                                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Regenerate")
                            }
                        }

                        // Strength Meter
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val color = when (strength) {
                                PasswordGenerator.PasswordStrength.WEAK -> Color(0xFFE53935)
                                PasswordGenerator.PasswordStrength.FAIR -> Color(0xFFFB8C00)
                                PasswordGenerator.PasswordStrength.STRONG -> Color(0xFF43A047)
                                PasswordGenerator.PasswordStrength.ULTRA -> Color(0xFF00ACC1)
                            }
                            LinearProgressIndicator(
                                progress = { strength.score / 4f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(CircleShape),
                                color = color
                            )
                            Text(
                                text = strength.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                }

                // Length Slider
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Length")
                        Text("${length.toInt()} characters", fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = length,
                        onValueChange = {
                            length = it
                            regenerate()
                        },
                        valueRange = 8f..64f,
                        steps = 55
                    )
                }

                // Options List
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Uppercase (A-Z)")
                        Switch(checked = useUpper, onCheckedChange = { useUpper = it; regenerate() })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Lowercase (a-z)")
                        Switch(checked = useLower, onCheckedChange = { useLower = it; regenerate() })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Numbers (0-9)")
                        Switch(checked = useNumbers, onCheckedChange = { useNumbers = it; regenerate() })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Symbols (!@#$)")
                        Switch(checked = useSymbols, onCheckedChange = { useSymbols = it; regenerate() })
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Avoid Confusing Characters (0, O, I, l)")
                        Switch(checked = avoidAmbiguous, onCheckedChange = { avoidAmbiguous = it; regenerate() })
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onUsePassword(generatedPassword) },
                modifier = Modifier.testTag("use_generated_password_button")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Copy & Use Password")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
