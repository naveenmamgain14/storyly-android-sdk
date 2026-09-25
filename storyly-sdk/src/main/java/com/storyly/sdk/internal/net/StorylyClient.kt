package com.storyly.sdk.internal.net

import com.storyly.sdk.StorylyConfig
import com.storyly.sdk.model.Story
import com.storyly.sdk.model.StoryItem
import com.storyly.sdk.model.StoryMediaType
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal class StorylyClient(
    private val config: StorylyConfig,
    private val callFactory: Call.Factory = StorylyHttp.client,
) {

    private val json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
    }

    suspend fun fetchStories(): List<Story> {
        val request = Request.Builder()
            .url("${config.baseUrl}/api/v1/sdk/stories")
            .header("X-API-Key", config.apiKey)
            .header("Accept", "application/json")
            .get()
            .build()

        val body = callFactory.newCall(request).awaitBody()
        val envelope = json.decodeFromString(StoriesEnvelope.serializer(), body)
        if (!envelope.success) {
            throw IOException(envelope.error ?: "Request rejected by server")
        }
        return envelope.data.map { it.toStory() }
    }

    private suspend fun Call.awaitBody(): String = suspendCancellableCoroutine { cont ->
        // Cancelling the coroutine must cancel the in-flight request, otherwise a
        // scrolled-away rail keeps a socket busy.
        cont.invokeOnCancellation { runCatching { cancel() } }

        enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (!cont.isCancelled) cont.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!it.isSuccessful) {
                        cont.resumeWithException(IOException(it.code.toHttpMessage()))
                        return
                    }
                    val text = it.body?.string()
                    if (text == null) {
                        cont.resumeWithException(IOException("Empty response"))
                    } else {
                        cont.resume(text)
                    }
                }
            }
        })
    }
}

private fun Int.toHttpMessage(): String = when {
    this == 401 || this == 403 -> "Invalid API key"
    this == 404 -> "Stories endpoint not found"
    this >= 500 -> "Server error ($this)"
    else -> "Request failed ($this)"
}

private fun StoryDto.toStory(): Story {
    val mapped = items.mapNotNull { it.toStoryItem() }
    return Story(
        id = id,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl ?: mapped.firstOrNull()?.url,
        items = mapped,
    )
}

private fun StoryItemDto.toStoryItem(): StoryItem? {
    val media = mediaUrl?.takeIf { it.isNotBlank() } ?: return null
    return StoryItem(
        id = id.ifBlank { media },
        type = resolveMediaType(type, media),
        url = media,
        durationSeconds = duration.coerceIn(MIN_DURATION_SECONDS, MAX_DURATION_SECONDS),
        actionUrl = actionUrl?.takeIf { it.isNotBlank() },
        actionText = actionText?.takeIf { it.isNotBlank() },
    )
}

/**
 * The backend only distinguishes image from video, so GIFs arrive typed as
 * images. They need a different decoder, so detect them from the URL path.
 */
private fun resolveMediaType(rawType: String, url: String): StoryMediaType {
    if (rawType.equals("video", ignoreCase = true)) return StoryMediaType.VIDEO
    val path = url.substringBefore('?').substringBefore('#')
    return if (path.endsWith(".gif", ignoreCase = true)) StoryMediaType.GIF else StoryMediaType.IMAGE
}

private const val MIN_DURATION_SECONDS = 1
private const val MAX_DURATION_SECONDS = 60
