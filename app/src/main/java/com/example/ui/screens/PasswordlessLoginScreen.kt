package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ServiceIconView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PushLoginRequest(
    val id: String,
    val serviceName: String,
    val userEmail: String,
    val location: String,
    val ipAddress: String,
    val device: String,
    val targetNumber: Int,
    val optionNumbers: List<Int>,
    val timestamp: Long = System.currentTimeMillis(),
    var status: RequestStatus = RequestStatus.PENDING
)

enum class RequestStatus {
    PENDING,
    APPROVED,
    DENIED,
    EXPIRED
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordlessLoginScreen(
    onBack: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeRequest by remember { mutableStateOf<PushLoginRequest?>(null) }
    var timeRemainingSeconds by remember { mutableIntStateOf(30) }

    val requestHistory = remember {
        mutableStateListOf(
            PushLoginRequest(
                id = "req_1",
                serviceName = "Microsoft Entra ID",
                userEmail = "alex.smith@contoso.com",
                location = "San Francisco, CA, USA",
                ipAddress = "192.0.2.45",
                device = "Chrome on macOS Sonoma",
                targetNumber = 42,
                optionNumbers = listOf(18, 42, 89),
                timestamp = System.currentTimeMillis() - 3600000,
                status = RequestStatus.APPROVED
            ),
            PushLoginRequest(
                id = "req_2",
                serviceName = "Corporate VPN Gateway",
                userEmail = "alex.smith@contoso.com",
                location = "London, UK",
                ipAddress = "198.51.100.12",
                device = "Safari on iOS",
                targetNumber = 77,
                optionNumbers = listOf(34, 77, 91),
                timestamp = System.currentTimeMillis() - 86400000,
                status = RequestStatus.APPROVED
            )
        )
    }

    // Timer loop for pending active push request
    LaunchedEffect(activeRequest) {
        if (activeRequest != null && activeRequest?.status == RequestStatus.PENDING) {
            timeRemainingSeconds = 30
            while (timeRemainingSeconds > 0 && activeRequest?.status == RequestStatus.PENDING) {
                delay(1000)
                timeRemainingSeconds--
            }
            if (timeRemainingSeconds <= 0 && activeRequest?.status == RequestStatus.PENDING) {
                activeRequest?.let { req ->
                    req.status = RequestStatus.EXPIRED
                    requestHistory.add(0, req)
                    snackbarHostState.showSnackbar("Push login request expired")
                }
                activeRequest = null
            }
        }
    }

    fun triggerNewSimulatedPush() {
        val services = listOf("Microsoft Entra ID", "Google Workspace", "Okta SSO", "GitHub Enterprise", "AWS Identity Center")
        val locations = listOf("Seattle, WA, USA", "New York, NY, USA", "Berlin, Germany", "Tokyo, Japan", "Austin, TX, USA")
        val devices = listOf("Chrome on macOS", "Firefox on Windows 11", "Safari on macOS", "Edge on Windows")
        
        val correctNum = Random.nextInt(10, 99)
        val wrong1 = (correctNum + Random.nextInt(5, 20)) % 90 + 10
        val wrong2 = (correctNum + Random.nextInt(21, 40)) % 90 + 10
        val options = listOf(correctNum, wrong1, wrong2).shuffled()

        val req = PushLoginRequest(
            id = "req_${System.currentTimeMillis()}",
            serviceName = services.random(),
            userEmail = "employee@enterprise.com",
            location = locations.random(),
            ipAddress = "203.0.113.${Random.nextInt(1, 254)}",
            device = devices.random(),
            targetNumber = correctNum,
            optionNumbers = options,
            timestamp = System.currentTimeMillis(),
            status = RequestStatus.PENDING
        )
        activeRequest = req
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Passwordless Push Auth", fontWeight = FontWeight.Bold)
                        Text(
                            text = "Enterprise biometric SSO verification",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("passwordless_back_button")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Banner
            item {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Passwordless Security",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Approve login attempts instantly without typing passwords. Match the number on screen and verify with biometrics.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
            }

            // Simulate Request Action Button
            item {
                Button(
                    onClick = { triggerNewSimulatedPush() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("simulate_push_request_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simulate Incoming Login Request", fontWeight = FontWeight.Bold)
                }
            }

            // Active Pending Request Prompt Card
            if (activeRequest != null && activeRequest?.status == RequestStatus.PENDING) {
                val req = activeRequest!!
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(2.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    ServiceIconView(
                                        issuer = req.serviceName,
                                        accountName = req.userEmail,
                                        size = 36.dp
                                    )
                                    Text(
                                        text = req.serviceName,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.errorContainer
                                ) {
                                    Text(
                                        text = "${timeRemainingSeconds}s",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                            }

                            LinearProgressIndicator(
                                progress = { timeRemainingSeconds / 30f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Request Metadata Box
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = "${req.location} (${req.ipAddress})", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Computer, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = req.device, style = MaterialTheme.typography.bodySmall)
                                }
                            }

                            // Number Matching Prompt
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Tap the matching number shown on your browser screen:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    modifier = Modifier.padding(vertical = 8.dp)
                                ) {
                                    req.optionNumbers.forEach { number ->
                                        Surface(
                                            onClick = {
                                                if (number == req.targetNumber) {
                                                    req.status = RequestStatus.APPROVED
                                                    requestHistory.add(0, req)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Login Approved via Biometric Token!")
                                                    }
                                                } else {
                                                    req.status = RequestStatus.DENIED
                                                    requestHistory.add(0, req)
                                                    coroutineScope.launch {
                                                        snackbarHostState.showSnackbar("Incorrect number selected! Login Denied.")
                                                    }
                                                }
                                                activeRequest = null
                                            },
                                            shape = RoundedCornerShape(16.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shadowElevation = 4.dp,
                                            modifier = Modifier.size(72.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    text = number.toString(),
                                                    style = MaterialTheme.typography.headlineMedium,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        req.status = RequestStatus.DENIED
                                        requestHistory.add(0, req)
                                        activeRequest = null
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("Request Denied.")
                                        }
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = MaterialTheme.colorScheme.error
                                    )
                                ) {
                                    Text("Deny Login")
                                }
                            }
                        }
                    }
                }
            }

            // Push History List Section
            item {
                Text(
                    text = "Push Auth Activity History",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(requestHistory, key = { it.id }) { req ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val (statusIcon, statusColor, statusText) = when (req.status) {
                                RequestStatus.APPROVED -> Triple(Icons.Default.CheckCircle, Color(0xFF43A047), "Approved")
                                RequestStatus.DENIED -> Triple(Icons.Default.Warning, Color(0xFFE53935), "Denied")
                                RequestStatus.EXPIRED -> Triple(Icons.Default.Warning, Color(0xFFFB8C00), "Expired")
                                RequestStatus.PENDING -> Triple(Icons.Default.Shield, MaterialTheme.colorScheme.primary, "Pending")
                            }

                            Icon(
                                imageVector = statusIcon,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(24.dp)
                            )

                            Column {
                                Text(
                                    text = req.serviceName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${req.device} • ${req.location}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                            text = when (req.status) {
                                RequestStatus.APPROVED -> "Approved"
                                RequestStatus.DENIED -> "Denied"
                                RequestStatus.EXPIRED -> "Expired"
                                RequestStatus.PENDING -> "Pending"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (req.status) {
                                RequestStatus.APPROVED -> Color(0xFF43A047)
                                RequestStatus.DENIED -> Color(0xFFE53935)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }
        }
    }
}
