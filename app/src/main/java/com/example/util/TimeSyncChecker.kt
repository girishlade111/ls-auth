package com.example.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.abs

sealed class TimeAccuracyStatus {
    object Idle : TimeAccuracyStatus()
    object Checking : TimeAccuracyStatus()
    data class Success(
        val driftSeconds: Long,
        val isWarning: Boolean,
        val networkTimeMs: Long,
        val deviceTimeMs: Long,
        val message: String
    ) : TimeAccuracyStatus()
    data class Error(val message: String) : TimeAccuracyStatus()
}

object TimeSyncChecker {

    private val TIME_SERVERS = listOf(
        "https://www.google.com",
        "https://cloudflare.com",
        "https://www.apple.com"
    )

    suspend fun checkClockAccuracy(): TimeAccuracyStatus = withContext(Dispatchers.IO) {
        val deviceTimeBefore = System.currentTimeMillis()
        var lastErrorMsg = "Unable to connect to network time servers."

        for (serverUrl in TIME_SERVERS) {
            try {
                val url = URL(serverUrl)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "HEAD"
                    connectTimeout = 4000
                    readTimeout = 4000
                    useCaches = false
                }

                val responseCode = connection.responseCode
                val networkDateHeader = connection.date
                val deviceTimeAfter = System.currentTimeMillis()
                connection.disconnect()

                if (networkDateHeader > 0) {
                    val latencyMs = (deviceTimeAfter - deviceTimeBefore) / 2
                    val estimatedNetworkTime = networkDateHeader + latencyMs
                    val deviceCurrentTime = System.currentTimeMillis()

                    val driftMs = deviceCurrentTime - estimatedNetworkTime
                    val driftSeconds = driftMs / 1000
                    val absDrift = abs(driftSeconds)

                    val isWarning = absDrift >= 15
                    val msg = if (isWarning) {
                        "Warning: Device clock is off by ~${absDrift}s from network time. TOTP codes may fail. Please set device clock to automatic."
                    } else {
                        "Device clock is synchronized! Drift is ~${absDrift}s (well within TOTP tolerance)."
                    }

                    return@withContext TimeAccuracyStatus.Success(
                        driftSeconds = driftSeconds,
                        isWarning = isWarning,
                        networkTimeMs = estimatedNetworkTime,
                        deviceTimeMs = deviceCurrentTime,
                        message = msg
                    )
                }
            } catch (e: Exception) {
                lastErrorMsg = e.localizedMessage ?: "Network error checking time"
            }
        }

        return@withContext TimeAccuracyStatus.Error(
            message = "Time check failed: $lastErrorMsg. Check internet connection."
        )
    }
}
