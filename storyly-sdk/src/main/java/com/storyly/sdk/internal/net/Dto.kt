package com.storyly.sdk.internal.net

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class StoriesEnvelope(
    val success: Boolean = false,
    val data: List<StoryDto> = emptyList(),
    val error: String? = null,
)

@Serializable
internal data class StoryDto(
    val id: String,
    val title: String = "",
    val description: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val items: List<StoryItemDto> = emptyList(),
)

@Serializable
internal data class StoryItemDto(
    val id: String = "",
    val type: String = "image",
    @SerialName("media_url") val mediaUrl: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val duration: Int = 5,
    @SerialName("action_url") val actionUrl: String? = null,
    @SerialName("action_text") val actionText: String? = null,
)
