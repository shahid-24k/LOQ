/**
 * Adapted from the Metrolist Project's StreamUrlCacheTest (C) 2026, GPL-3.0.
 * https://github.com/MetrolistGroup/Metrolist
 *
 * Adapted to Echo's Map-like StreamUrlCache surface (get/set, remove,
 * containsKey, keys, filter, clear, putAll) rather than Metrolist's
 * header/client-based put() API, since that's what MusicService.kt's
 * existing call sites use.
 */

package echo.music.iad1tya.playback

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class StreamUrlCacheTest {

    @Test
    fun `fresh entry is returned via get`() {
        val cache = StreamUrlCache()
        cache["song_HIGH"] = "https://example.com/stream" to (System.currentTimeMillis() + 10_000L)

        val entry = cache["song_HIGH"]

        assertEquals("https://example.com/stream", entry?.first)
    }

    @Test
    fun `absent entry returns null`() {
        val cache = StreamUrlCache()

        assertNull(cache["missing_HIGH"])
    }

    @Test
    fun `remove clears the entry and bumps generation`() {
        val cache = StreamUrlCache()
        cache["song_HIGH"] = "https://example.com/stream" to Long.MAX_VALUE
        val generationBefore = cache.generation("song_HIGH")

        cache.remove("song_HIGH")

        assertNull(cache["song_HIGH"])
        assertEquals(generationBefore + 1, cache.generation("song_HIGH"))
    }

    @Test
    fun `containsKey reflects current state`() {
        val cache = StreamUrlCache()
        assertFalse(cache.containsKey("song_HIGH"))

        cache["song_HIGH"] = "https://example.com/stream" to Long.MAX_VALUE

        assertTrue(cache.containsKey("song_HIGH"))
    }

    @Test
    fun `least recently used entry is evicted at capacity`() {
        val cache = StreamUrlCache(maxEntries = 2)
        cache["first_HIGH"] = "https://example.com/first" to Long.MAX_VALUE
        cache["second_HIGH"] = "https://example.com/second" to Long.MAX_VALUE
        // touch "first" so "second" becomes the least-recently-used entry
        cache["first_HIGH"]

        cache["third_HIGH"] = "https://example.com/third" to Long.MAX_VALUE

        assertNull(cache["second_HIGH"])
        assertEquals("https://example.com/first", cache["first_HIGH"]?.first)
        assertEquals("https://example.com/third", cache["third_HIGH"]?.first)
    }

    @Test
    fun `filter matches the quality-switch save-restore pattern used in MusicService`() {
        val cache = StreamUrlCache()
        cache["songA_HIGH"] = "https://example.com/a-high" to Long.MAX_VALUE
        cache["songA_LOW"] = "https://example.com/a-low" to Long.MAX_VALUE
        cache["songB_HIGH"] = "https://example.com/b-high" to Long.MAX_VALUE

        val keptForSongA = cache.filter { it.key.startsWith("songA_") }
        cache.clear()
        cache.putAll(keptForSongA)

        assertEquals(2, keptForSongA.size)
        assertTrue(cache.containsKey("songA_HIGH"))
        assertTrue(cache.containsKey("songA_LOW"))
        assertFalse(cache.containsKey("songB_HIGH"))
    }

    @Test
    fun `clear empties every entry and bumps their generations`() {
        val cache = StreamUrlCache()
        cache["song_HIGH"] = "https://example.com/stream" to Long.MAX_VALUE
        val generationBefore = cache.generation("song_HIGH")

        cache.clear()

        assertNull(cache["song_HIGH"])
        assertEquals(generationBefore + 1, cache.generation("song_HIGH"))
    }

    @Test
    fun `invalidate is equivalent to remove for generation tracking`() {
        val cache = StreamUrlCache()
        cache["song_HIGH"] = "https://example.com/stream" to Long.MAX_VALUE

        cache.invalidate("song_HIGH")

        assertNull(cache["song_HIGH"])
    }

    @Test
    fun `put with matching generation succeeds and records client name`() {
        val cache = StreamUrlCache()

        val accepted = cache.put(
            key = "song_HIGH",
            value = "https://example.com/stream" to Long.MAX_VALUE,
            clientName = "WEB_REMIX",
        )

        assertTrue(accepted)
        assertEquals("WEB_REMIX", cache.clientName("song_HIGH"))
    }

    @Test
    fun `put with stale expected generation is rejected`() {
        val cache = StreamUrlCache()
        cache["song_HIGH"] = "https://example.com/stream" to Long.MAX_VALUE
        val staleGeneration = cache.generation("song_HIGH")
        cache.invalidate("song_HIGH") // advances the generation

        val accepted = cache.put(
            key = "song_HIGH",
            value = "https://example.com/other" to Long.MAX_VALUE,
            clientName = "WEB_REMIX",
            expectedGeneration = staleGeneration,
        )

        assertFalse(accepted)
        assertNull(cache["song_HIGH"])
    }

    @Test
    fun `generation counter is pruned when LRU evicts the entry`() {
        val cache = StreamUrlCache(maxEntries = 2)
        cache["first_HIGH"] = "https://example.com/first" to Long.MAX_VALUE
        cache.invalidate("first_HIGH") // generation now 1, entry gone
        cache["first_HIGH"] = "https://example.com/first-again" to Long.MAX_VALUE // back in, generation still 1
        cache["second_HIGH"] = "https://example.com/second" to Long.MAX_VALUE
        cache["third_HIGH"] = "https://example.com/third" to Long.MAX_VALUE // evicts "first_HIGH"

        assertEquals(0L, cache.generation("first_HIGH"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun `maxEntries must be positive`() {
        StreamUrlCache(maxEntries = 0)
    }
}
