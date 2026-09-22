/**
 * Adapted from the Metrolist Project's StreamUrlCache (C) 2026, GPL-3.0.
 * https://github.com/MetrolistGroup/Metrolist
 *
 * Adaptation notes (LOQ):
 * Echo's existing call sites in MusicService.kt treat this cache as a plain
 * Map<String, Pair<String, Long>> (url to absolute-expiry-millis), keyed by
 * "${mediaId}_${audioQuality.name}", and use get/set, remove, containsKey,
 * keys, filter, clear, and putAll directly. Rather than rewrite every call
 * site to a narrower cache API (Metrolist's own call sites are simpler and
 * don't need this surface), this class preserves that exact Map-like
 * surface so MusicService.kt requires no change beyond the type of the
 * songUrlCache declaration, while replacing the previous unbounded
 * HashMap with a bounded, thread-safe LRU structure underneath.
 *
 * Bounded size, LRU eviction, and synchronized access are wired into every
 * operation below. Client-name tracking and generation-based invalidation
 * are included as additive capability (mirroring Metrolist's design) for
 * future use if Echo's stream-resolution logic adopts multi-client
 * fallback later; they are not required by any current call site.
 */

package echo.music.iad1tya.playback

internal class StreamUrlCache(
    private val maxEntries: Int = 500,
) {
    private data class Entry(
        val value: Pair<String, Long>,
        val clientName: String? = null,
    )

    private val entries =
        object : LinkedHashMap<String, Entry>(0, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Entry>): Boolean {
                val shouldRemove = size > maxEntries
                if (shouldRemove) {
                    generations.remove(eldest.key)
                }
                return shouldRemove
            }
        }
    private val generations = HashMap<String, Long>()

    init {
        require(maxEntries > 0) { "maxEntries must be greater than zero" }
    }

    /** Same semantics as the HashMap this replaces: returns null if absent or expired-by-caller-check. */
    operator fun get(key: String): Pair<String, Long>? =
        synchronized(entries) { entries[key]?.value }

    /** Same semantics as HashMap's []= : unconditional write, always succeeds. */
    operator fun set(key: String, value: Pair<String, Long>) {
        synchronized(entries) {
            entries[key] = Entry(value = value)
        }
    }

    fun remove(key: String) {
        synchronized(entries) {
            entries.remove(key)
            advanceGeneration(key)
        }
    }

    fun containsKey(key: String): Boolean =
        synchronized(entries) { entries.containsKey(key) }

    /** Snapshot of current keys -- safe to iterate/filter without holding the lock. */
    val keys: Set<String>
        get() = synchronized(entries) { entries.keys.toSet() }

    fun filter(predicate: (Map.Entry<String, Pair<String, Long>>) -> Boolean): Map<String, Pair<String, Long>> =
        synchronized(entries) {
            entries.entries
                .filter { predicate(java.util.AbstractMap.SimpleEntry(it.key, it.value.value)) }
                .associate { it.key to it.value.value }
        }

    fun clear() {
        synchronized(entries) {
            entries.keys.toList().forEach { advanceGeneration(it) }
            entries.clear()
        }
    }

    fun putAll(map: Map<String, Pair<String, Long>>) {
        synchronized(entries) {
            map.forEach { (key, value) -> entries[key] = Entry(value = value) }
        }
    }

    // --- Additive capability (not yet used by any Echo call site) ---

    fun clientName(key: String): String? =
        synchronized(entries) { entries[key]?.clientName }

    fun generation(key: String): Long =
        synchronized(entries) { generations[key] ?: 0L }

    /** Like set(), but records a client name and can be guarded against a stale generation. */
    fun put(
        key: String,
        value: Pair<String, Long>,
        clientName: String,
        expectedGeneration: Long = generation(key),
    ): Boolean =
        synchronized(entries) {
            if ((generations[key] ?: 0L) != expectedGeneration) return false
            entries[key] = Entry(value = value, clientName = clientName)
            true
        }

    fun invalidate(key: String) {
        synchronized(entries) {
            entries.remove(key)
            advanceGeneration(key)
        }
    }

    private fun advanceGeneration(key: String) {
        generations[key] = (generations[key] ?: 0L) + 1L
    }
}
