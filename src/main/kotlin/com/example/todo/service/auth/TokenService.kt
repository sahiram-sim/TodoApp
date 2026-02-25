package com.example.todo.service.auth

import com.example.todo.domain.user.User
import com.example.todo.security.JwtTokenProvider
import org.springframework.stereotype.Service

@Service
class TokenService(
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun createAccessToken(user: User): String {
        // implement based on your JwtTokenProvider API
        // common pattern: subject = user.id or user.email
        return jwtTokenProvider.generateAccessToken(user)
    }

    fun createRefreshTokenValue(): String {
        return jwtTokenProvider.generateRefreshTokenValue() // or UUID.randomUUID().toString()
    }
}