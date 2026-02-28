package com.example.todo.config

import com.example.todo.ratelimit.RateLimitFilter
import com.example.todo.ratelimit.RateLimiterStore
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.scheduling.annotation.Scheduled

@Configuration
class RateLimitConfig {

    @Bean
    fun rateLimiterStore(): RateLimiterStore = RateLimiterStore()

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE + 10)
    fun rateLimitFilter(store: RateLimiterStore): RateLimitFilter {
        return RateLimitFilter(store)
    }

    @Scheduled(fixedDelayString = "PT10M")
    fun cleanupRateLimitBuckets() {
    }
}