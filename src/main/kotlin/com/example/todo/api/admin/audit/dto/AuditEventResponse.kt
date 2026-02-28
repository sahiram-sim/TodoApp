package com.example.todo.api.admin.audit.dto

import java.time.Instant
import java.util.UUID

data class AuditEventResponse(
    val id: UUID,
    val createdAt: Instant,
    val actorUserId: UUID?,
    val actorEmail: String?,
    val targetUserId: UUID?,
    val action: String,
    val outcome: String,
    val message: String?,
    val ip: String?,
    val userAgent: String?,
    val requestId: String?,
    val httpMethod: String?,
    val httpPath: String?
)