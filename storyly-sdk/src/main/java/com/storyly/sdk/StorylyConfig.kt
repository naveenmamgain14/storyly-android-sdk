package com.storyly.sdk

/**
 * Connection and caching settings for the SDK.
 *
 * [apiKey] and [backendUrl] are required: the SDK never assumes a default backend,
 * so each integrating app points at its own Storyly deployment.
 */
public class StorylyConfig @JvmOverloads constructor(
    public val apiKey: String,
    public val backendUrl: String,
    public val diskCacheBytes: Long = DEFAULT_DISK_CACHE_BYTES,
    /** Set false to stop the SDK reporting any usage events. */
    public val analyticsEnabled: Boolean = true,
    /** Your own identifier for the signed-in user, attached to events. */
    public val userId: String? = null,
) {
    init {
        require(apiKey.isNotBlank()) { "apiKey must not be blank" }
        require(backendUrl.isNotBlank()) { "backendUrl must not be blank" }
        require(diskCacheBytes > 0) { "diskCacheBytes must be positive" }
    }

    internal val baseUrl: String = backendUrl.trimEnd('/')

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StorylyConfig) return false
        return apiKey == other.apiKey &&
            backendUrl == other.backendUrl &&
            diskCacheBytes == other.diskCacheBytes &&
            analyticsEnabled == other.analyticsEnabled &&
            userId == other.userId
    }

    override fun hashCode(): Int {
        var result = apiKey.hashCode()
        result = 31 * result + backendUrl.hashCode()
        result = 31 * result + diskCacheBytes.hashCode()
        result = 31 * result + analyticsEnabled.hashCode()
        result = 31 * result + (userId?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String = "StorylyConfig(backendUrl=$backendUrl)"

    public companion object {
        /** 64 MB of on-disk media cache. */
        public const val DEFAULT_DISK_CACHE_BYTES: Long = 64L * 1024 * 1024
    }
}
