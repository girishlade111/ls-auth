package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.crypto.GoogleAuthMigrationParser
import com.example.crypto.MigratedAccount
import com.example.crypto.OtpUriParser
import com.example.data.UserSettings
import com.example.model.Account
import com.example.ui.components.QrReticleOverlay
import com.example.util.HapticAction
import com.example.util.HapticManager
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.zxing.BinaryBitmap
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun QrScannerScreen(
    onScanSuccess: (Account) -> Unit,
    onMigrationSuccess: (List<MigratedAccount>) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    userSettings: UserSettings = UserSettings()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    var isTorchOn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var scannedRawText by remember { mutableStateOf<String?>(null) }
    var cameraControl by remember { mutableStateOf<androidx.camera.core.CameraControl?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            decodeQrFromUri(context, uri,
                onSuccess = { qrText ->
                    processQrText(
                        context = context,
                        userSettings = userSettings,
                        rawText = qrText,
                        onScanSuccess = onScanSuccess,
                        onMigrationSuccess = onMigrationSuccess,
                        onError = { errorMessage = it }
                    )
                },
                onError = { errorMessage = it }
            )
        }
    }

    LaunchedEffect(Unit) {
        if (!cameraPermissionState.status.isGranted) {
            cameraPermissionState.launchPermissionRequest()
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color.Black
    ) {
        if (cameraPermissionState.status.isGranted) {
            Box(modifier = Modifier.fillMaxSize()) {
                // CameraX Preview View
                AndroidView(
                    factory = { ctx ->
                        val previewView = PreviewView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }

                        val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            val executor = Executors.newSingleThreadExecutor()
                            imageAnalysis.setAnalyzer(executor) { imageProxy ->
                                processImageProxy(
                                    imageProxy = imageProxy,
                                    onQrFound = { raw ->
                                        if (scannedRawText == null) {
                                            scannedRawText = raw
                                            processQrText(
                                                context = context,
                                                userSettings = userSettings,
                                                rawText = raw,
                                                onScanSuccess = onScanSuccess,
                                                onMigrationSuccess = onMigrationSuccess,
                                                onError = {
                                                    errorMessage = it
                                                    scannedRawText = null
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            try {
                                cameraProvider.unbindAll()
                                val camera = cameraProvider.bindToLifecycle(
                                    lifecycleOwner,
                                    cameraSelector,
                                    preview,
                                    imageAnalysis
                                )
                                cameraControl = camera.cameraControl
                            } catch (e: Exception) {
                                Log.e("QrScanner", "Camera binding failed", e)
                            }
                        }, ContextCompat.getMainExecutor(ctx))

                        previewView
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Reticle & Darkened Cutout
                QrReticleOverlay()

                // Top Controls Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp, start = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                            .testTag("scanner_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = "Scan 2FA QR Code",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            isTorchOn = !isTorchOn
                            cameraControl?.enableTorch(isTorchOn)
                        },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isTorchOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                            contentDescription = "Toggle Torch",
                            tint = if (isTorchOn) Color.Yellow else Color.White
                        )
                    }
                }

                // Bottom Bar with Gallery Import Option
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier.testTag("scan_from_image_button")
                    ) {
                        Icon(imageVector = Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick QR Image from Gallery")
                    }
                }
            }
        } else {
            // Camera Permission Denied View
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Camera Permission Required",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "LS Auth requires local camera access to scan 2FA setup QR codes. Images are processed purely in real-time memory and never stored or uploaded.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { cameraPermissionState.launchPermissionRequest() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Grant Camera Permission")
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = onBack) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }

        // Error Dialog
        errorMessage?.let { errorText ->
            AlertDialog(
                onDismissRequest = { errorMessage = null },
                title = { Text("Unrecognized QR Code") },
                text = { Text(errorText) },
                confirmButton = {
                    Button(onClick = { errorMessage = null }) {
                        Text("Try Again")
                    }
                }
            )
        }
    }
}

private fun processQrText(
    context: Context,
    userSettings: UserSettings,
    rawText: String,
    onScanSuccess: (Account) -> Unit,
    onMigrationSuccess: (List<MigratedAccount>) -> Unit,
    onError: (String) -> Unit
) {
    val trimmed = rawText.trim()
    try {
        if (trimmed.startsWith("otpauth-migration://", ignoreCase = true)) {
            val migratedAccounts = GoogleAuthMigrationParser.parseMigrationUrl(trimmed)
            if (migratedAccounts.isNotEmpty()) {
                HapticManager.performHaptic(context, HapticAction.SCAN_SUCCESS, userSettings)
                onMigrationSuccess(migratedAccounts)
            } else {
                HapticManager.performHaptic(context, HapticAction.ERROR_STATE, userSettings)
                onError("No accounts found in Google Authenticator export QR payload")
            }
        } else if (trimmed.startsWith("otpauth://", ignoreCase = true)) {
            val parsed = OtpUriParser.parse(trimmed)
            val account = Account(
                accountName = parsed.accountName,
                issuer = parsed.issuer,
                secretBase32 = parsed.secretBase32,
                type = parsed.type,
                algorithm = parsed.algorithm,
                digits = parsed.digits,
                period = parsed.period,
                counter = parsed.counter
            )
            HapticManager.performHaptic(context, HapticAction.SCAN_SUCCESS, userSettings)
            onScanSuccess(account)
        } else {
            HapticManager.performHaptic(context, HapticAction.ERROR_STATE, userSettings)
            onError("QR code is not a valid 2FA setup link (must start with otpauth:// or otpauth-migration://)")
        }
    } catch (e: Exception) {
        HapticManager.performHaptic(context, HapticAction.ERROR_STATE, userSettings)
        onError("Failed to parse QR code: ${e.message}")
    }
}

private fun processImageProxy(
    imageProxy: ImageProxy,
    onQrFound: (String) -> Unit
) {
    try {
        val buffer = imageProxy.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val width = imageProxy.width
        val height = imageProxy.height

        val luminanceSource = com.google.zxing.PlanarYUVLuminanceSource(
            bytes, width, height, 0, 0, width, height, false
        )
        val binaryBitmap = BinaryBitmap(HybridBinarizer(luminanceSource))
        val reader = MultiFormatReader()
        val result = reader.decode(binaryBitmap)

        result?.text?.let { rawText ->
            onQrFound(rawText)
        }
    } catch (e: Exception) {
        // No QR detected in frame
    } finally {
        imageProxy.close()
    }
}

private fun decodeQrFromUri(
    context: Context,
    uri: Uri,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        if (bitmap == null) {
            onError("Failed to load image from gallery")
            return
        }

        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        val reader = MultiFormatReader()
        val result = reader.decode(binaryBitmap)

        if (result != null && result.text.isNotBlank()) {
            onSuccess(result.text)
        } else {
            onError("No valid 2FA QR code found in selected image")
        }
    } catch (e: Exception) {
        onError("Could not decode QR code from image: ${e.message}")
    }
}
