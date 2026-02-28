package com.example.todo.api.auth.dto

import java.time.Instant
import java.util.UUID

data class SessionResponse(
    val id: UUID,
    val createdAt: Instant,
    val expiresAt: Instant,
    val revoked: Boolean,
    val revokedAt: Instant?,
    val replacedByTokenId: UUID?
)