package com.example.todo.domain.audit

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "audit_events")
class AuditEvent(
    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID = UUID.randomUUID(),

    @Column(nullable = false)
    var createdAt: Instant = Instant.now(),

    @Column(columnDefinition = "uuid")
    var actorUserId: UUID? = null,

    var actorEmail: String? = null,

    @Column(columnDefinition = "uuid")
    var targetUserId: UUID? = null,

    @Column(nullable = false)
    var action: String,

    @Column(nullable = false)
    var outcome: String, // SUCCESS / FAIL

    @Column(columnDefinition = "text")
    var message: String? = null,

    var ip: String? = null,
    var userAgent: String? = null,
    var requestId: String? = null,

    var httpMethod: String? = null,
    var httpPath: String? = null
)