package com.example.todo.ratelimit

import com.example.todo.security.AuthUser
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.AnonymousAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import java.time.Duration

class RateLimitFilter(
    private val store: RateLimiterStore
) : OncePerRequestFilter() {

    private val authUserLimit = 200L
    private val authUserWindow = Duration.ofMinutes(1).seconds

    private val anonLimit = 30L
    private val anonWindow = Duration.ofMinutes(1).seconds

    // IP-based auth endpoints limit (baseline)
    private val authEndpointsLimit = 10L
    private val authEndpointsWindow = Duration.ofMinutes(15).seconds

    // Email-based limit for targeted attacks (login/forgot-password)
    private val authEmailLimit = 5L
    private val authEmailWindow = Duration.ofMinutes(15).seconds

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val path = request.requestURI ?: "/"
        val isAuthEndpoint = path.startsWith("/api/auth")

        // Wrap only for endpoints where we need request body (login/forgot-password)
        val effectiveRequest =
            if (path == "/api/auth/login" || path == "/api/auth/forgot-password") {
                CachedBodyHttpServletRequest(request)
            } else {
                request
            }

        val auth = SecurityContextHolder.getContext().authentication
        val isAuthenticated =
            auth != null && auth.isAuthenticated && auth !is AnonymousAuthenticationToken

        // 1) Apply IP limit for auth endpoints (all /api/auth/**)
        if (isAuthEndpoint) {
            val ip = ClientIpResolver.resolve(effectiveRequest)
            val ipResult = store.consume("auth-ip:$ip", authEndpointsLimit, authEndpointsWindow)
            writeHeaders(response, ipResult)

            if (!ipResult.allowed) {
                write429(response, path, ipResult.retryAfterSeconds)
                return
            }

            // 2) Additionally apply email-based limit for login/forgot-password
            if (effectiveRequest is CachedBodyHttpServletRequest &&
                (path == "/api/auth/login" || path == "/api/auth/forgot-password")
            ) {
                val email = extractEmailFromJson(effectiveRequest.bodyAsString())
                if (!email.isNullOrBlank()) {
                    val emailKey = "auth-email:${email.trim().lowercase()}"
                    val emailResult = store.consume(emailKey, authEmailLimit, authEmailWindow)
                    // (optional) overwrite headers with stricter bucket; or set separate headers
                    writeHeaders(response, emailResult)

                    if (!emailResult.allowed) {
                        write429(response, path, emailResult.retryAfterSeconds)
                        return
                    }
                }
            }

            filterChain.doFilter(effectiveRequest, response)
            return
        }

        // Non-auth endpoints: authenticated user bucket by userId if possible
        val (limit, windowSeconds, identityKey) = when {
            isAuthenticated -> {
                val userId =
                    (auth.principal as? AuthUser)?.id?.toString()
                        ?: auth.name
                        ?: "unknown"
                Triple(authUserLimit, authUserWindow, "user:$userId")
            }
            else -> {
                val ip = ClientIpResolver.resolve(effectiveRequest)
                Triple(anonLimit, anonWindow, "anon-ip:$ip")
            }
        }

        val result = store.consume(identityKey, limit, windowSeconds)
        writeHeaders(response, result)

        if (!result.allowed) {
            write429(response, path, result.retryAfterSeconds)
            return
        }

        filterChain.doFilter(effectiveRequest, response)
    }

    private fun writeHeaders(response: HttpServletResponse, result: RateLimiterStore.Result) {
        response.setHeader("X-RateLimit-Limit", result.limit.toString())
        response.setHeader("X-RateLimit-Remaining", result.remaining.toString())
        response.setHeader("X-RateLimit-Reset", result.resetEpochSeconds.toString())
    }

    private fun write429(response: HttpServletResponse, path: String, retryAfterSeconds: Long) {
        response.status = 429
        response.setHeader("Retry-After", retryAfterSeconds.toString())
        response.contentType = "application/json"
        response.writer.write(
            """{"status":429,"error":"Too Many Requests","message":"Rate limit exceeded","path":"$path"}"""
        )
    }

    // Minimal JSON extraction without adding new deps.
    // Works for {"email":"x"} and ignores casing/spacing.
    private fun extractEmailFromJson(body: String): String? {
        val re = Regex(""""email"\s*:\s*"([^"]+)"""", RegexOption.IGNORE_CASE)
        return re.find(body)?.groupValues?.getOrNull(1)
    }
}