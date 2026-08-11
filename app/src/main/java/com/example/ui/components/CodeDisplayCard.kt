package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.crypto.OtpType
import com.example.data.UserSettings
import com.example.model.Account
import com.example.util.HapticAction
import com.example.util.HapticManager
import kotlinx.coroutines.delay

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CodeDisplayCard(
    account: Account,
    timeSeconds: Long,
    isBlurred: Boolean,
    onCopyCode: (String) -> Unit,
    onHotpIncrement: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    dragHandleModifier: Modifier = Modifier,
    isDragging: Boolean = false,
    cardElevation: androidx.compose.ui.unit.Dp = 1.dp,
    userSettings: UserSettings = UserSettings()
) {
    val context = LocalContext.current
    var isJustCopied by remember { mutableStateOf(false) }
    var localBlur by remember { mutableStateOf(isBlurred) }

    val formattedCode = remember(timeSeconds, account, account.counter) {
        account.getFormattedCode(timeSeconds)
    }

    val remainingSeconds = account.getRemainingSeconds(timeSeconds)
    val periodProgress = account.getPeriodProgress(timeSeconds)

    LaunchedEffect(isBlurred) {
        localBlur = isBlurred
    }

    LaunchedEffect(isJustCopied) {
        if (isJustCopied) {
            delay(1500)
            isJustCopied = false
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("account_card_${account.id}")
            .combinedClickable(
                onClick = {
                    if (localBlur) {
                        localBlur = false
                    } else if (account.type == OtpType.TOTP) {
                        HapticManager.performHaptic(context, HapticAction.COPY_CODE, userSettings)
                        onCopyCode(account.getCurrentCode(timeSeconds))
                        isJustCopied = true
                    }
                },
                onLongClick = {
                    HapticManager.performHaptic(context, HapticAction.KEYPRESS, userSettings)
                    onLongClick()
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDragging) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Drag Handle Icon
            Icon(
                imageVector = Icons.Default.DragHandle,
                contentDescription = "Drag to reorder account",
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = if (isDragging) 0.9f else 0.4f),
                modifier = dragHandleModifier
                    .padding(end = 8.dp)
                    .testTag("drag_handle_${account.id}")
            )

            // Service Icon
            ServiceIconView(
                issuer = account.issuer,
                accountName = account.accountName,
                iconKey = account.iconKey,
                size = 46.dp
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Account Info + Code
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Issuer / Label
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = account.issuer.ifBlank { account.accountName },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (account.issuer.isNotBlank() && account.accountName.isNotBlank()) {
                    Text(
                        text = account.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Large Monospaced Code
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (localBlur) {
                        Text(
                            text = "••••••  Tap to reveal",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    } else {
                        AnimatedContent(
                            targetState = formattedCode,
                            transitionSpec = {
                                (fadeIn() + scaleIn(initialScale = 0.95f)).togetherWith(
                                    fadeOut() + scaleOut(targetScale = 1.05f)
                                )
                            },
                            label = "codeTransition"
                        ) { codeText ->
                            Text(
                                text = codeText,
                                style = MaterialTheme.typography.headlineLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (isJustCopied) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Copied",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Right side: Countdown ring (for TOTP) or Refresh button (for HOTP) or Blur toggle
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                if (account.type == OtpType.TOTP) {
                    CountdownRing(
                        remainingSeconds = remainingSeconds,
                        periodProgress = periodProgress,
                        size = 38.dp,
                        strokeWidth = 3.dp
                    )
                } else {
                    // HOTP generate next code button
                    IconButton(
                        onClick = {
                            triggerHapticFeedback(context)
                            onHotpIncrement()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Generate HOTP code",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                IconButton(
                    onClick = { localBlur = !localBlur },
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Icon(
                        imageVector = if (localBlur) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = if (localBlur) "Show code" else "Hide code",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}

private fun triggerHapticFeedback(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(50)
        }
    } catch (e: Exception) {
        // Ignore if vibration not available
    }
}
