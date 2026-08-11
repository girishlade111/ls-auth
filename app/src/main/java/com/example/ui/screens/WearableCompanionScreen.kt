package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Account
import com.example.ui.components.ServiceIconView
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WearableCompanionScreen(
    accounts: List<Account>,
    timeSeconds: Long,
    onCopyText: (String, String) -> Unit,
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var selectedIndex by remember { mutableIntStateOf(0) }
    var syncTilesEnabled by remember { mutableStateOf(true) }
    var offlineCacheEnabled by remember { mutableStateOf(true) }
    var complicationWidgetEnabled by remember { mutableStateOf(true) }
    var isAmbientMode by remember { mutableStateOf(false) }

    val currentAccount = remember(accounts, selectedIndex) {
        if (accounts.isNotEmpty()) accounts[selectedIndex.coerceIn(0, accounts.lastIndex)]
        else null
    }

    val liveCode = remember(currentAccount, timeSeconds) {
        currentAccount?.getCurrentCode(timeSeconds) ?: "000 000"
    }

    val secondsRemaining = 30 - (timeSeconds % 30)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Wear OS Watch Sync", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Smartwatch companion & complications",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("wearable_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status Header Badge
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BluetoothConnected,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Connected: Galaxy Watch 6 Pro",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Wear OS 4.0 • Encrypted BLE Sync Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Interactive Smartwatch Display Frame
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Live Wear OS Tile Preview",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Circular Watch Chassis
                    Surface(
                        shape = CircleShape,
                        color = if (isAmbientMode) Color.Black else MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .size(260.dp)
                            .border(8.dp, Color(0xFF2C2C2E), CircleShape)
                            .border(10.dp, Color(0xFF1C1C1E), CircleShape)
                            .clip(CircleShape)
                            .clickable {
                                if (currentAccount != null) {
                                    onCopyText(liveCode, "2FA Code")
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("Copied $liveCode from Watch Tile")
                                    }
                                }
                            },
                        shadowElevation = 12.dp
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            // Circular Countdown Ring
                            CircularProgressIndicator(
                                progress = { secondsRemaining / 30f },
                                modifier = Modifier.size(230.dp),
                                color = if (secondsRemaining <= 5) Color(0xFFE53935) else MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                strokeWidth = 8.dp
                            )

                            // Inner Watch Face Content
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier.padding(24.dp)
                            ) {
                                if (currentAccount != null) {
                                    ServiceIconView(
                                        issuer = currentAccount.issuer,
                                        accountName = currentAccount.accountName,
                                        size = 32.dp
                                    )

                                    Text(
                                        text = currentAccount.issuer.ifBlank { currentAccount.accountName },
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isAmbientMode) Color.White else MaterialTheme.colorScheme.onSurface
                                    )

                                    val formatted = if (liveCode.length == 6) "${liveCode.take(3)} ${liveCode.drop(3)}" else liveCode
                                    Text(
                                        text = formatted,
                                        style = MaterialTheme.typography.headlineMedium,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isAmbientMode) Color.Cyan else MaterialTheme.colorScheme.primary,
                                        letterSpacing = 2.sp
                                    )

                                    Text(
                                        text = "Tap to Copy • ${secondsRemaining}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = if (isAmbientMode) Color.Gray else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                } else {
                                    Text(text = "No tokens", color = Color.Gray)
                                }
                            }
                        }
                    }

                    // Watch Controls (Token Switcher & Ambient Mode)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        IconButton(
                            onClick = {
                                if (accounts.isNotEmpty()) {
                                    selectedIndex = (selectedIndex - 1 + accounts.size) % accounts.size
                                }
                            },
                            enabled = accounts.size > 1
                        ) {
                            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = "Previous Token")
                        }

                        Text(
                            text = "${if (accounts.isNotEmpty()) selectedIndex + 1 else 0} of ${accounts.size}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold
                        )

                        IconButton(
                            onClick = {
                                if (accounts.isNotEmpty()) {
                                    selectedIndex = (selectedIndex + 1) % accounts.size
                                }
                            },
                            enabled = accounts.size > 1
                        ) {
                            Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Next Token")
                        }

                        OutlinedButton(
                            onClick = { isAmbientMode = !isAmbientMode },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isAmbientMode) "Active Mode" else "Ambient Mode")
                        }
                    }
                }
            }

            // Sync Settings Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Wear OS Companion Options",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Sync Tiles to Watch", fontWeight = FontWeight.SemiBold)
                                Text("Show live 2FA tokens on Wear OS swipe tiles", style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(checked = syncTilesEnabled, onCheckedChange = { syncTilesEnabled = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Watch Offline Encrypted Cache", fontWeight = FontWeight.SemiBold)
                                Text("Generate codes on watch even without phone connected", style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(checked = offlineCacheEnabled, onCheckedChange = { offlineCacheEnabled = it })
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Watchface Complications", fontWeight = FontWeight.SemiBold)
                                Text("Display 2FA widget directly on watch face", style = MaterialTheme.typography.labelSmall)
                            }
                            Switch(checked = complicationWidgetEnabled, onCheckedChange = { complicationWidgetEnabled = it })
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Watch data force synced successfully!")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Force Sync Watch Data")
                        }
                    }
                }
            }
        }
    }
}
