package com.example.todo.ratelimit

import com.example.todo.logging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class RateLimiterCleanupJob(
    private val store: RateLimiterStore
) {
    private val logger = logger()

    @Scheduled(fixedDelayString = "PT10M")
    fun cleanup() {
        val removed = store.cleanup(ttlSeconds = 3600) // 1 hour
        if (removed > 0) logger.debug("RateLimiterStore cleanup removed={}", removed)
    }
}