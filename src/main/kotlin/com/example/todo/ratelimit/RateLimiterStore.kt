package com.example.todo.ratelimit

import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

class RateLimiterStore {

    private data class BucketState(
        var windowStart: Long,
        var remaining: Long,
        @Volatile var lastSeen: Long
    )

    data class Result(
        val allowed: Boolean,
        val limit: Long,
        val remaining: Long,
        val resetEpochSeconds: Long,
        val retryAfterSeconds: Long
    )

    private val buckets = ConcurrentHashMap<String, BucketState>()

    fun consume(key: String, limit: Long, windowSeconds: Long): Result {
        val now = Instant.now().epochSecond
        val windowStart = now - (now % windowSeconds)
        val reset = windowStart + windowSeconds

        val state = buckets.compute(key) { _, existing ->
            if (existing == null || existing.windowStart != windowStart) {
                BucketState(windowStart = windowStart, remaining = limit, lastSeen = now)
            } else {
                existing.lastSeen = now
                existing
            }
        }!!

        return synchronized(state) {
            state.lastSeen = now

            if (state.windowStart != windowStart) {
                state.windowStart = windowStart
                state.remaining = limit
            }

            if (state.remaining > 0) {
                state.remaining -= 1
                Result(true, limit, state.remaining, reset, 0)
            } else {
                val retryAfter = (reset - now).coerceAtLeast(0)
                Result(false, limit, 0, reset, retryAfter)
            }
        }
    }

    /**
     * Remove buckets not used for ttlSeconds.
     */
    fun cleanup(ttlSeconds: Long): Int {
        val now = Instant.now().epochSecond
        var removed = 0
        val it = buckets.entries.iterator()
        while (it.hasNext()) {
            val e = it.next()
            val lastSeen = e.value.lastSeen
            if (now - lastSeen > ttlSeconds) {
                it.remove()
                removed++
            }
        }
        return removed
    }
}