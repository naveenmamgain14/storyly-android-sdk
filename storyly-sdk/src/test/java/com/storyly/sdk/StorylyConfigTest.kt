package com.storyly.sdk

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class StorylyConfigTest {

    @Test
    fun `blank api key is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            StorylyConfig(apiKey = "  ", backendUrl = "https://example.com")
        }
    }

    @Test
    fun `blank backend url is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            StorylyConfig(apiKey = "key", backendUrl = "")
        }
    }

    @Test
    fun `non positive cache size is rejected`() {
        assertThrows(IllegalArgumentException::class.java) {
            StorylyConfig(apiKey = "key", backendUrl = "https://example.com", diskCacheBytes = 0)
        }
    }

    @Test
    fun `trailing slashes are trimmed so paths do not double up`() {
        val config = StorylyConfig(apiKey = "key", backendUrl = "https://example.com///")
        assertEquals("https://example.com", config.baseUrl)
    }

    @Test
    fun `equality covers every field so recomposition reloads on change`() {
        val a = StorylyConfig("key", "https://example.com")
        assertEquals(a, StorylyConfig("key", "https://example.com"))
        assertEquals(a.hashCode(), StorylyConfig("key", "https://example.com").hashCode())
        assertNotEquals(a, StorylyConfig("other", "https://example.com"))
        assertNotEquals(a, StorylyConfig("key", "https://other.com"))
        assertNotEquals(a, StorylyConfig("key", "https://example.com", userId = "u1"))
        assertNotEquals(a, StorylyConfig("key", "https://example.com", analyticsEnabled = false))
    }

    @Test
    fun `api key is kept out of toString`() {
        val config = StorylyConfig("super-secret-key", "https://example.com")
        assert(!config.toString().contains("super-secret-key"))
    }
}
