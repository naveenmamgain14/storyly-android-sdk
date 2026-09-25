package com.storyly.sdk.internal.analytics

import android.content.Context
import com.storyly.sdk.internal.net.AnalyticsEventDto
import com.storyly.sdk.internal.net.StorylyClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.util.UUID

internal enum class StoryEvent {
    IMPRESSION,
    VIEW,
    CLICK,
    COMPLETE,
    DISMISS,
}

/**
 * Buffers events and posts them in batches.
 *
 * Analytics is strictly best-effort: every failure is swallowed, and nothing
 * here runs on the caller's thread, so a flaky network can never stall or
 * crash the host app's UI.
 */
internal class StorylyAnalytics(
    context: Context,
    private val client: StorylyClient,
    private val userId: String?,
    private val enabled: Boolean,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val queue = Channel<AnalyticsEventDto>(capacity = Channel.BUFFERED)
    private val deviceId = deviceId(context)
    private val sessionId = UUID.randomUUID().toString()

    init {
        if (enabled) scope.launch {
            drain()
            scope.cancel()
        }
    }

    fun track(event: StoryEvent, storyId: String, storyItemId: String? = null) {
        if (!enabled) return
        queue.trySend(
            AnalyticsEventDto(
                eventType = event.name,
                storyId = storyId,
                storyItemId = storyItemId,
                userId = userId,
                deviceId = deviceId,
                sessionId = sessionId,
            )
        )
    }

    /** Closing the queue lets the drain loop flush what is buffered, then exit. */
    fun shutdown() {
        queue.close()
    }

    /**
     * Sends whenever the batch fills or the window elapses, so a single tap is
     * not one request but a burst of taps is not one request per tap either.
     */
    private suspend fun drain() {
        val batch = mutableListOf<AnalyticsEventDto>()
        var closed = false

        while (!closed) {
            val event = withTimeoutOrNull(FLUSH_WINDOW_MS) {
                try {
                    queue.receive()
                } catch (e: ClosedReceiveChannelException) {
                    closed = true
                    null
                }
            }

            if (event != null) {
                batch += event
                if (batch.size < MAX_BATCH) continue
            }

            if (batch.isNotEmpty()) {
                client.sendEvents(batch.toList())
                batch.clear()
            }
        }
    }

    private fun deviceId(context: Context): String {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.getString(KEY_DEVICE_ID, null)?.let { return it }
        // Random per install: no hardware identifiers, nothing tied to the person.
        return UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }
    }

    private companion object {
        const val FLUSH_WINDOW_MS = 4_000L
        const val MAX_BATCH = 20
        const val PREFS = "storyly_sdk"
        const val KEY_DEVICE_ID = "device_id"
    }
}
