package com.example.todo.security

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(private val jwtTokenProvider: JwtTokenProvider) :
        OncePerRequestFilter() {

    override fun doFilterInternal(
            request: HttpServletRequest,
            response: HttpServletResponse,
            filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")
        val token = header?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")?.trim()

        println("JWT-FILTER path=${request.requestURI} hasToken=${!token.isNullOrBlank()}")

        if (!token.isNullOrBlank() && jwtTokenProvider.validateToken(token)) {
            val auth = jwtTokenProvider.getAuthentication(token)
            println(
                    "JWT-FILTER setting auth principalClass=${auth.principal?.javaClass?.name} principal=${auth.principal}"
            )
            SecurityContextHolder.getContext().authentication = auth
        } else {
            println("JWT-FILTER NOT setting auth (token blank or invalid)")
        }

        filterChain.doFilter(request, response)
    }
}
