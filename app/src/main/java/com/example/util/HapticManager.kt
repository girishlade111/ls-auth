package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.data.UserSettings

enum class HapticAction {
    COPY_CODE,
    SCAN_SUCCESS,
    ERROR_STATE,
    KEYPRESS,
    DRAG_START,
    DRAG_DROP
}

object HapticManager {

    fun performHaptic(
        context: Context,
        action: HapticAction,
        userSettings: UserSettings = UserSettings()
    ) {
        if (!userSettings.hapticsEnabled) return

        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            } ?: return

            if (!vibrator.hasVibrator()) return

            val durationFactor = when (userSettings.hapticDuration) {
                "SHORT" -> 0.5f
                "LONG" -> 1.8f
                else -> 1.0f // "MEDIUM"
            }

            val amplitude = when (userSettings.hapticIntensity) {
                "LIGHT" -> 70
                "STRONG" -> 255
                else -> 160 // "MEDIUM"
            }

            when (action) {
                HapticAction.COPY_CODE -> {
                    if (!userSettings.copyCodeHapticEnabled) return
                    val durationMs = (35L * durationFactor).toLong().coerceAtLeast(10L)
                    vibrateOneShot(vibrator, durationMs, amplitude)
                }

                HapticAction.SCAN_SUCCESS -> {
                    if (!userSettings.scanSuccessHapticEnabled) return
                    val pulse1 = (30L * durationFactor).toLong().coerceAtLeast(10L)
                    val pause = (40L * durationFactor).toLong().coerceAtLeast(15L)
                    val pulse2 = (50L * durationFactor).toLong().coerceAtLeast(15L)
                    vibrateWaveform(
                        vibrator,
                        longArrayOf(0, pulse1, pause, pulse2),
                        intArrayOf(0, (amplitude * 0.7f).toInt().coerceIn(1, 255), 0, amplitude)
                    )
                }

                HapticAction.ERROR_STATE -> {
                    if (!userSettings.errorHapticEnabled) return
                    val pulse = (50L * durationFactor).toLong().coerceAtLeast(15L)
                    val pause = (35L * durationFactor).toLong().coerceAtLeast(10L)
                    vibrateWaveform(
                        vibrator,
                        longArrayOf(0, pulse, pause, pulse, pause, pulse),
                        intArrayOf(0, amplitude, 0, amplitude, 0, amplitude)
                    )
                }

                HapticAction.KEYPRESS -> {
                    val durationMs = (15L * durationFactor).toLong().coerceAtLeast(8L)
                    vibrateOneShot(vibrator, durationMs, (amplitude * 0.6f).toInt().coerceIn(1, 255))
                }

                HapticAction.DRAG_START -> {
                    val durationMs = (25L * durationFactor).toLong().coerceAtLeast(10L)
                    vibrateOneShot(vibrator, durationMs, (amplitude * 0.85f).toInt().coerceIn(1, 255))
                }

                HapticAction.DRAG_DROP -> {
                    val pulse1 = (18L * durationFactor).toLong().coerceAtLeast(8L)
                    val pause = (18L * durationFactor).toLong().coerceAtLeast(8L)
                    val pulse2 = (28L * durationFactor).toLong().coerceAtLeast(10L)
                    vibrateWaveform(
                        vibrator,
                        longArrayOf(0, pulse1, pause, pulse2),
                        intArrayOf(0, (amplitude * 0.5f).toInt().coerceIn(1, 255), 0, (amplitude * 0.9f).toInt().coerceIn(1, 255))
                    )
                }
            }
        } catch (_: Exception) {
            // Ignore if vibration hardware is not present or supported
        }
    }

    private fun vibrateOneShot(vibrator: Vibrator, milliseconds: Long, amplitude: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val safeAmp = amplitude.coerceIn(1, 255)
            vibrator.vibrate(VibrationEffect.createOneShot(milliseconds, safeAmp))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(milliseconds)
        }
    }

    private fun vibrateWaveform(vibrator: Vibrator, timings: LongArray, amplitudes: IntArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
        } else {
            @Suppress("DEPRECATION")
            val totalMs = timings.sum()
            vibrator.vibrate(totalMs)
        }
    }
}
