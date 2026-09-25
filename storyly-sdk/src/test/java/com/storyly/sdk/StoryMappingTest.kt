package com.storyly.sdk

import com.storyly.sdk.internal.net.StoryDto
import com.storyly.sdk.internal.net.StoryItemDto
import com.storyly.sdk.internal.net.resolveMediaType
import com.storyly.sdk.internal.net.toHttpMessage
import com.storyly.sdk.internal.net.toStory
import com.storyly.sdk.internal.net.toStoryItem
import com.storyly.sdk.model.StoryMediaType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StoryMappingTest {

    @Test
    fun `video type from the backend wins`() {
        assertEquals(StoryMediaType.VIDEO, resolveMediaType("video", "https://x/clip.mp4"))
        assertEquals(StoryMediaType.VIDEO, resolveMediaType("VIDEO", "https://x/clip.mp4"))
    }

    @Test
    fun `gif is detected from the url because the backend reports it as image`() {
        assertEquals(StoryMediaType.GIF, resolveMediaType("image", "https://x/loop.gif"))
        assertEquals(StoryMediaType.GIF, resolveMediaType("image", "https://x/loop.GIF"))
    }

    @Test
    fun `gif detection ignores query strings and fragments`() {
        assertEquals(StoryMediaType.GIF, resolveMediaType("image", "https://x/loop.gif?v=2"))
        assertEquals(StoryMediaType.GIF, resolveMediaType("image", "https://x/loop.gif#a"))
    }

    @Test
    fun `a gif elsewhere in the url does not make it a gif`() {
        assertEquals(StoryMediaType.IMAGE, resolveMediaType("image", "https://gif.example.com/p.jpg"))
    }

    @Test
    fun `items without media are dropped rather than rendered blank`() {
        assertNull(StoryItemDto(id = "1", mediaUrl = null).toStoryItem())
        assertNull(StoryItemDto(id = "1", mediaUrl = "   ").toStoryItem())
    }

    @Test
    fun `duration is clamped so one bad record cannot freeze a story`() {
        assertEquals(1, StoryItemDto(mediaUrl = "https://x/a.jpg", duration = 0).toStoryItem()!!.durationSeconds)
        assertEquals(1, StoryItemDto(mediaUrl = "https://x/a.jpg", duration = -5).toStoryItem()!!.durationSeconds)
        assertEquals(60, StoryItemDto(mediaUrl = "https://x/a.jpg", duration = 9999).toStoryItem()!!.durationSeconds)
        assertEquals(7, StoryItemDto(mediaUrl = "https://x/a.jpg", duration = 7).toStoryItem()!!.durationSeconds)
    }

    @Test
    fun `blank action text becomes null so no empty button renders`() {
        val item = StoryItemDto(mediaUrl = "https://x/a.jpg", actionText = "  ", actionUrl = "").toStoryItem()!!
        assertNull(item.actionText)
        assertNull(item.actionUrl)
    }

    @Test
    fun `story falls back to its first item for a thumbnail`() {
        val story = StoryDto(
            id = "s1",
            title = "T",
            thumbnailUrl = null,
            items = listOf(StoryItemDto(mediaUrl = "https://x/first.jpg")),
        ).toStory()
        assertEquals("https://x/first.jpg", story.thumbnailUrl)
    }

    @Test
    fun `http codes map to messages a user can act on`() {
        assertEquals("Invalid API key", 401.toHttpMessage())
        assertEquals("Invalid API key", 403.toHttpMessage())
        assertEquals("Stories endpoint not found", 404.toHttpMessage())
        assertEquals("Server error (503)", 503.toHttpMessage())
        assertEquals("Request failed (418)", 418.toHttpMessage())
    }
}
