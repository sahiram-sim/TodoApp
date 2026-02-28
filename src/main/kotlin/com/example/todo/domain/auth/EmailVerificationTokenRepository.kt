package com.example.todo.domain.auth

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface EmailVerificationTokenRepository : JpaRepository<EmailVerificationToken, UUID> {
    fun findByTokenHash(tokenHash: String): EmailVerificationToken?

    @Modifying
    @Query("delete from EmailVerificationToken t where t.expiresAt < :cutoff")
    fun deleteExpired(@Param("cutoff") cutoff: Instant): Int

    @Modifying
    @Query("update EmailVerificationToken t set t.usedAt = :now where t.user.id = :userId and t.usedAt is null")
    fun markAllUnusedAsUsed(@Param("userId") userId: UUID, @Param("now") now: Instant): Int
}