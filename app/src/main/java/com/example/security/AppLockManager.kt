package com.example.security

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.view.WindowManager
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.data.UserSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AppLockManager {

    private val _isUnlocked = MutableStateFlow(false)
    val isUnlocked: StateFlow<Boolean> = _isUnlocked.asStateFlow()

    private var lastBackgroundTimestamp: Long = 0
    private var lastUserInteractionTimestamp: Long = SystemClock.elapsedRealtime()

    fun recordUserInteraction() {
        lastUserInteractionTimestamp = SystemClock.elapsedRealtime()
    }

    fun checkInactivityTimeout(settings: UserSettings) {
        if (!settings.isAppLockEnabled || !_isUnlocked.value) return
        if (settings.autoLockTimeoutSeconds > 0) {
            val now = SystemClock.elapsedRealtime()
            val elapsedInteractionSeconds = (now - lastUserInteractionTimestamp) / 1000
            if (elapsedInteractionSeconds >= settings.autoLockTimeoutSeconds) {
                lock()
            }
        }
    }

    fun onAppForegrounded(settings: UserSettings) {
        if (!settings.isAppLockEnabled) {
            _isUnlocked.value = true
            return
        }

        if (lastBackgroundTimestamp == 0L) {
            _isUnlocked.value = false
            return
        }

        val elapsedSeconds = (SystemClock.elapsedRealtime() - lastBackgroundTimestamp) / 1000
        if (settings.autoLockTimeoutSeconds == 0 || elapsedSeconds >= settings.autoLockTimeoutSeconds) {
            _isUnlocked.value = false
        } else {
            recordUserInteraction()
        }
    }

    fun onAppBackgrounded(settings: UserSettings) {
        if (settings.isAppLockEnabled) {
            lastBackgroundTimestamp = SystemClock.elapsedRealtime()
            if (settings.autoLockTimeoutSeconds == 0) {
                _isUnlocked.value = false
            }
        }
    }

    fun unlock() {
        lastUserInteractionTimestamp = SystemClock.elapsedRealtime()
        lastBackgroundTimestamp = 0L
        _isUnlocked.value = true
    }

    fun lock() {
        _isUnlocked.value = false
    }

    fun applySecureFlag(activity: Activity, secure: Boolean = false) {
        // Keep FLAG_SECURE cleared to allow streaming emulator preview rendering
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    fun promptBiometric(
        activity: FragmentActivity,
        title: String = "LS Auth Unlock",
        subtitle: String = "Authenticate to access 2FA vault",
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val biometricManager = BiometricManager.from(activity)
        val canAuthenticate = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
        )

        if (canAuthenticate != BiometricManager.BIOMETRIC_SUCCESS) {
            onError("Biometric authentication not available on this device")
            return
        }

        val executor = ContextCompat.getMainExecutor(activity)
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText("Use PIN")
            .build()

        val biometricPrompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    unlock()
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }
            }
        )

        biometricPrompt.authenticate(promptInfo)
    }
}
