package com.storyly.sdk.internal.media

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.storyly.sdk.internal.net.StorylyHttp

@OptIn(UnstableApi::class)
internal object StorylyVideo {

    // SimpleCache must be a process singleton: two instances over one directory throws.
    @Volatile
    private var cache: SimpleCache? = null

    private fun cache(context: Context, maxBytes: Long): SimpleCache {
        cache?.let { return it }
        return synchronized(this) {
            cache ?: SimpleCache(
                context.cacheDir.resolve(CACHE_DIR),
                LeastRecentlyUsedCacheEvictor(maxBytes),
                StandaloneDatabaseProvider(context),
            ).also { cache = it }
        }
    }

    fun createPlayer(context: Context, cacheBytes: Long): ExoPlayer {
        val app = context.applicationContext
        val dataSource = CacheDataSource.Factory()
            .setCache(cache(app, cacheBytes))
            // Reuses the SDK's single OkHttp client rather than opening a second stack.
            .setUpstreamDataSourceFactory(OkHttpDataSource.Factory(StorylyHttp.client))
            .setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)

        return ExoPlayer.Builder(app)
            .setMediaSourceFactory(DefaultMediaSourceFactory(dataSource))
            .build()
            .apply { repeatMode = ExoPlayer.REPEAT_MODE_OFF }
    }

    private const val CACHE_DIR = "storyly_video"
}

/**
 * One player for the whole viewer session. Creating a player per slide is the
 * usual cause of stutter when paging through video stories.
 */
@Composable
internal fun rememberStorylyPlayer(cacheBytes: Long): ExoPlayer {
    val context = LocalContext.current
    val player = remember { StorylyVideo.createPlayer(context, cacheBytes) }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    return player
}
