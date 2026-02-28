package com.example.todo.audit

import com.example.todo.ratelimit.ClientIpResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.util.UUID

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5) // before your rate limit filter
class AuditContextFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val requestId = request.getHeader("X-Request-Id")?.takeIf { it.isNotBlank() }
            ?: UUID.randomUUID().toString()

        val ip = ClientIpResolver.resolve(request)
        val ua = request.getHeader("User-Agent")

        AuditContextHolder.set(
            AuditRequestContext(
                requestId = requestId,
                ip = ip,
                userAgent = ua,
                method = request.method,
                path = request.requestURI
            )
        )

        // Optionally echo request id for clients
        response.setHeader("X-Request-Id", requestId)

        try {
            filterChain.doFilter(request, response)
        } finally {
            AuditContextHolder.clear()
        }
    }
}