package com.example.todo.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secret: String,
    val issuer: String = "todoapp",
    val accessTokenExpirationSeconds: Long = 900
)