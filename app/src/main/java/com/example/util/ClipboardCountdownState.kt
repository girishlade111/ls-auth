package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * State manager for tracking copied clipboard code and executing countdown auto-clear.
 */
class ClipboardCountdownState(
    private val context: Context,
    val totalSeconds: Int = 30
) {
    var copiedCode by mutableStateOf<String?>(null)
        private set
    var remainingSeconds by mutableIntStateOf(0)
        private set
    var isVisible by mutableStateOf(false)
        private set

    private var timerJob: Job? = null

    /**
     * Copies text to system clipboard, shows system toast confirmation,
     * and starts live countdown for automatic clipboard clearing.
     */
    fun copyToClipboard(
        code: String,
        label: String = "2FA Code",
        scope: CoroutineScope
    ) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        val clip = ClipData.newPlainText(label, code)
        clipboard?.setPrimaryClip(clip)

        // Standard system Toast confirmation
        Toast.makeText(
            context,
            "Copied code $code to clipboard. Auto-clearing in ${totalSeconds}s",
            Toast.LENGTH_SHORT
        ).show()

        copiedCode = code
        remainingSeconds = totalSeconds
        isVisible = true

        timerJob?.cancel()
        timerJob = scope.launch {
            while (remainingSeconds > 0 && isActive) {
                delay(1000L)
                remainingSeconds--
            }
            if (isActive) {
                clearClipboardInternal()
            }
        }
    }

    /**
     * Clears clipboard immediately when requested by user action.
     */
    fun clearClipboardNow() {
        timerJob?.cancel()
        clearClipboardInternal()
    }

    private fun clearClipboardInternal() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                clipboard?.clearPrimaryClip()
            } else {
                clipboard?.setPrimaryClip(ClipData.newPlainText("", ""))
            }
        } catch (e: Exception) {
            clipboard?.setPrimaryClip(ClipData.newPlainText("", ""))
        }

        Toast.makeText(context, "Clipboard cleared for security", Toast.LENGTH_SHORT).show()

        copiedCode = null
        remainingSeconds = 0
        isVisible = false
    }

    fun dismissBanner() {
        isVisible = false
    }
}

@Composable
fun rememberClipboardCountdownState(
    totalSeconds: Int = 30
): ClipboardCountdownState {
    val context = LocalContext.current
    return remember(context, totalSeconds) {
        ClipboardCountdownState(context, totalSeconds)
    }
}
