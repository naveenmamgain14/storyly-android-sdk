package com.storyly.sdk.internal.media

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.storyly.sdk.internal.net.StorylyHttp

/**
 * The SDK owns its image loader rather than using Coil's global singleton, so a
 * host app's own Coil configuration is never altered by integrating Storyly.
 */
internal object StorylyImageLoaders {

    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context, diskCacheBytes: Long): ImageLoader {
        instance?.let { return it }
        return synchronized(this) {
            instance ?: build(context.applicationContext, diskCacheBytes).also { instance = it }
        }
    }

    private fun build(context: Context, diskCacheBytes: Long): ImageLoader =
        ImageLoader.Builder(context)
            .callFactory { StorylyHttp.client }
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(MEMORY_CACHE_FRACTION)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve(DISK_CACHE_DIR))
                    .maxSizeBytes(diskCacheBytes)
                    .build()
            }
            .respectCacheHeaders(false)
            .build()

    /**
     * Warm the cache for slides the viewer is about to show. Failures are
     * irrelevant here — this is an optimisation, and the real request will
     * surface any error.
     */
    fun prefetch(context: Context, loader: ImageLoader, urls: List<String>) {
        urls.forEach { url ->
            loader.enqueue(
                ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            )
        }
    }

    private const val MEMORY_CACHE_FRACTION = 0.25
    private const val DISK_CACHE_DIR = "storyly_media"
}
