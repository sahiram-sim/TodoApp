package com.example.todo.domain.token

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.UUID
import java.time.Instant

interface RefreshTokenRepository : JpaRepository<RefreshToken, UUID> {
    fun findByTokenHash(tokenHash: String): RefreshToken?

    @Modifying
    @Query("delete from RefreshToken rt where rt.user.id = :userId")
    fun deleteByUserId(@Param("userId") userId: UUID)

    fun findAllByUserIdOrderByCreatedAtDesc(userId: UUID): List<RefreshToken>

    @Modifying
    @Query("""
        update RefreshToken rt
           set rt.revoked = true,
               rt.revokedAt = :now
         where rt.id = :id
           and rt.user.id = :userId
           and rt.revoked = false
    """)
    fun revokeByIdForUser(
        @Param("id") id: UUID,
        @Param("userId") userId: UUID,
        @Param("now") now: Instant
    ): Int
}