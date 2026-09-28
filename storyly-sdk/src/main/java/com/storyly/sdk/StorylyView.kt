package com.storyly.sdk

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.DisposableEffect
import com.storyly.sdk.internal.analytics.StoryEvent
import com.storyly.sdk.internal.cache.StoryCache
import com.storyly.sdk.internal.analytics.StorylyAnalytics
import com.storyly.sdk.internal.media.StorylyImageLoaders
import com.storyly.sdk.internal.net.StorylyClient
import com.storyly.sdk.internal.ui.StoryRail
import com.storyly.sdk.internal.ui.StoryRailError
import com.storyly.sdk.internal.ui.StoryRailSkeleton
import com.storyly.sdk.internal.ui.StoryViewer
import com.storyly.sdk.internal.ui.StorylyTokens
import com.storyly.sdk.model.Story
import com.storyly.sdk.model.StoryMediaType
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * A horizontal rail of stories that opens a full-screen viewer when tapped.
 *
 * ```
 * StorylyView(
 *     config = StorylyConfig(apiKey = "…", backendUrl = "https://example.com"),
 * )
 * ```
 */
@Composable
public fun StorylyView(
    config: StorylyConfig,
    modifier: Modifier = Modifier,
    listener: StorylyListener? = null,
) {
    val context = LocalContext.current
    val currentListener by rememberUpdatedState(listener)

    val imageLoader = remember(config.diskCacheBytes) {
        StorylyImageLoaders.get(context, config.diskCacheBytes)
    }
    val client = remember(config) { StorylyClient(config) }
    val cache = remember(config) { StoryCache(context, config) }
    val analytics = remember(config) {
        StorylyAnalytics(context, client, config.userId, config.analyticsEnabled)
    }
    DisposableEffect(analytics) { onDispose { analytics.shutdown() } }

    var state by remember { mutableStateOf<RailState>(RailState.Loading) }
    var retryToken by remember { mutableIntStateOf(0) }
    var openedIndex by remember { mutableStateOf<Int?>(null) }
    var seen by remember { mutableStateOf(emptySet<String>()) }

    LaunchedEffect(config, retryToken) {
        state = RailState.Loading

        // Paint the last good response first so a cold start shows stories
        // rather than a skeleton, then revalidate against the network.
        val cached = cache.read()?.let { runCatching { client.parse(it) }.getOrNull() }
        if (!cached.isNullOrEmpty()) state = RailState.Ready(cached)

        try {
            val body = client.fetchRaw()
            val fresh = client.parse(body)
            cache.write(body)
            state = RailState.Ready(fresh)
        } catch (e: Exception) {
            currentListener?.onLoadFailed(e)
            // A failed refresh must not wipe content that is already on screen;
            // only surface the error when there is nothing to show.
            if (state !is RailState.Ready) state = RailState.Failed(e.toDisplayMessage())
        }
    }

    val stories = (state as? RailState.Ready)?.stories.orEmpty()

    // Warm the thumbnails the user is most likely to tap next.
    LaunchedEffect(stories) {
        stories.forEach { analytics.track(StoryEvent.IMPRESSION, it.id) }
        if (stories.isNotEmpty()) {
            StorylyImageLoaders.prefetch(
                context = context,
                loader = imageLoader,
                urls = stories.mapNotNull { it.thumbnailUrl },
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(StorylyTokens.surface)
    ) {
        when (val current = state) {
            RailState.Loading -> StoryRailSkeleton()

            is RailState.Failed -> StoryRailError(
                message = current.message,
                onRetry = { retryToken++ },
            )

            is RailState.Ready -> if (current.stories.isNotEmpty()) {
                StoryRail(
                    stories = current.stories,
                    seen = seen,
                    imageLoader = imageLoader,
                    onStoryClick = { index ->
                        current.stories.getOrNull(index)?.let { story ->
                            seen = seen + story.id
                            openedIndex = index
                            // VIEW is emitted by onStoryShown, which also covers
                            // swiping between stories. Tracking it here too would
                            // double-count every open.
                            currentListener?.onStoryOpened(story)
                        }
                    },
                )
            }
        }
    }

    val index = openedIndex
    if (index != null && index in stories.indices) {
        // Pull in the first frames of the opened story before they are swiped to.
        LaunchedEffect(index) {
            StorylyImageLoaders.prefetch(
                context = context,
                loader = imageLoader,
                urls = stories[index].items
                    .filter { it.type != StoryMediaType.VIDEO }
                    .map { it.url },
            )
        }

        StoryViewer(
            stories = stories,
            initialStoryIndex = index,
            imageLoader = imageLoader,
            onStoryShown = { story ->
                seen = seen + story.id
                analytics.track(StoryEvent.VIEW, story.id)
            },
            onStoryCompleted = { story -> analytics.track(StoryEvent.COMPLETE, story.id) },
            onActionClick = { story, item ->
                analytics.track(StoryEvent.CLICK, story.id, item.id)
                currentListener?.onActionClicked(story, item)
            },
            onDismiss = {
                stories.getOrNull(index)?.let {
                    analytics.track(StoryEvent.DISMISS, it.id)
                    currentListener?.onStoryClosed(it)
                }
                openedIndex = null
            },
            videoCacheBytes = config.diskCacheBytes,
        )
    }
}

private sealed interface RailState {
    data object Loading : RailState
    data class Ready(val stories: List<Story>) : RailState
    data class Failed(val message: String) : RailState
}

private fun Throwable.toDisplayMessage(): String = when (this) {
    is UnknownHostException -> "No internet connection"
    is SocketTimeoutException -> "Connection timed out"
    is ConnectException -> "Couldn't reach the server"
    else -> message ?: "Couldn't load stories"
}
