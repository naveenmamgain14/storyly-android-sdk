package com.storyly.sdk.internal.cache

import android.content.Context
import com.storyly.sdk.StorylyConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Persists the last good stories response so a cold start can paint immediately
 * instead of showing a skeleton while the network round-trips.
 *
 * The raw response body is stored rather than parsed models: parsing is cheap,
 * and it keeps the cache format independent of the model classes.
 */
internal class StoryCache(context: Context, config: StorylyConfig) {

    private val dir = File(context.applicationContext.cacheDir, DIR)

    // Keyed per backend+key so two configs in one app never read each other's data.
    private val file = File(dir, "${(config.baseUrl + config.apiKey).hashCode()}.json")

    suspend fun read(): String? = withContext(Dispatchers.IO) {
        runCatching { file.takeIf { it.isFile }?.readText()?.takeIf { it.isNotBlank() } }.getOrNull()
    }

    suspend fun write(body: String) {
        withContext(Dispatchers.IO) {
            runCatching {
                dir.mkdirs()
                // Write then rename so a kill mid-write cannot leave a truncated cache.
                val tmp = File(dir, file.name + ".tmp")
                tmp.writeText(body)
                if (!tmp.renameTo(file)) {
                    file.writeText(body)
                    tmp.delete()
                }
            }
        }
    }

    private companion object {
        const val DIR = "storyly_stories"
    }
}
