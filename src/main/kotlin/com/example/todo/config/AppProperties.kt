package com.example.todo.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
        val pagination: Pagination = Pagination(),
        val cache: Cache = Cache(),
        val links: Links = Links(),
        val security: Security = Security()
) {
    data class Pagination(val defaultSize: Int = 20, val maxSize: Int = 100)

    data class Cache(
            val todoListTtlMinutes: Long = 5,
            val todoSingleTtlMinutes: Long = 30,
            val profileTtlMinutes: Long = 30,
            val versionTtlMinutes: Long = 60,
            val maxEntries: Long = 10_000
    )

    data class Links(val frontendBaseUrl: String = "http://localhost:3000")

    data class Security(
            val emailVerifyTtlMinutes: Long = 60,
            val passwordResetTtlMinutes: Long = 30,
            val loginMaxAttempts: Int = 5,
            val loginLockMinutes: Long = 15,
    )

    data class Reminders(
            val enabled: Boolean = true,
            val dueSoonLeadDays: Long = 1,
            val schedulerFixedDelay: java.time.Duration = java.time.Duration.ofMinutes(10)
    )
    val reminders: Reminders = Reminders()
}
