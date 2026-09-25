package com.storyly.sdk.model

import androidx.compose.runtime.Immutable

/** A single story: a titled group of [StoryItem] slides. */
@Immutable
public class Story internal constructor(
    public val id: String,
    public val title: String,
    public val description: String?,
    public val thumbnailUrl: String?,
    public val items: List<StoryItem>,
) {
    override fun toString(): String = "Story(id=$id, title=$title, items=${items.size})"
}

/** One slide within a [Story]. */
@Immutable
public class StoryItem internal constructor(
    public val id: String,
    public val type: StoryMediaType,
    public val url: String,
    public val durationSeconds: Int,
    public val actionUrl: String?,
    public val actionText: String?,
) {
    override fun toString(): String = "StoryItem(id=$id, type=$type)"
}

/** Media kind of a [StoryItem]. */
public enum class StoryMediaType {
    IMAGE,
    GIF,
    VIDEO,
}
