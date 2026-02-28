package com.example.todo.domain.auth

import com.example.todo.domain.user.User
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "email_verification_tokens",
    indexes = [
        Index(name = "idx_evt_user", columnList = "user_id"),
        Index(name = "idx_evt_expires", columnList = "expiresAt")
    ]
)
class EmailVerificationToken(
    @Id
    @GeneratedValue
    var id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    // store hashed token (not raw token)
    @Column(nullable = false, length = 64, unique = true)
    var tokenHash: String,

    @Column(nullable = false)
    var expiresAt: Instant,

    var usedAt: Instant? = null,

    @Column(nullable = false)
    var createdAt: Instant = Instant.now()
) {
    fun isExpired(now: Instant = Instant.now()) = now.isAfter(expiresAt)
    fun isUsed() = usedAt != null
}