package com.example.todo.service.auth

import com.example.todo.config.AppProperties
import com.example.todo.domain.auth.PasswordResetToken
import com.example.todo.domain.auth.PasswordResetTokenRepository
import com.example.todo.domain.user.UserRepository
import com.example.todo.exception.UnauthorizedException
import com.example.todo.security.TokenUtil
import com.example.todo.service.email.EmailSender
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.transaction.annotation.Transactional
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class PasswordResetService(
    private val userRepository: UserRepository,
    private val tokenRepo: PasswordResetTokenRepository,
    private val emailSender: EmailSender,
    private val passwordEncoder: PasswordEncoder,
    private val appProperties: AppProperties,
    private val refreshTokenService: RefreshTokenService // optional: revoke sessions on reset
) {

    /**
     * Always returns successfully to prevent email enumeration.
     * If user exists, it issues a reset token and sends email (or logs it).
     */
    @Transactional
    fun requestReset(email: String) {
        val normalized = email.trim().lowercase()
        val user = userRepository.findByEmail(normalized).orElse(null) ?: return

        val userId = user.id ?: return

        // invalidate old unused reset tokens for this user
        tokenRepo.markAllUnusedAsUsed(userId, Instant.now())

        val rawToken = TokenUtil.newRawToken()
        val tokenHash = TokenUtil.sha256Hex(rawToken)

        val expiresAt = Instant.now()
            .plus(appProperties.security.passwordResetTtlMinutes, ChronoUnit.MINUTES)

        tokenRepo.save(
            PasswordResetToken(
                user = user,
                tokenHash = tokenHash,
                expiresAt = expiresAt
            )
        )

        val link = "${appProperties.links.frontendBaseUrl}/reset-password?token=$rawToken"
        val subject = "Reset your password"
        val body =
            "Click to reset your password: $link\n" +
            "This link expires in ${appProperties.security.passwordResetTtlMinutes} minutes."

        emailSender.send(user.email, subject, body)
    }

    @Transactional
    fun resetPassword(rawToken: String, newPassword: String) {
        val tokenHash = TokenUtil.sha256Hex(rawToken.trim())
        val token = tokenRepo.findByTokenHash(tokenHash) ?: throw UnauthorizedException("Invalid token")

        if (token.isUsed()) throw UnauthorizedException("Token already used")
        if (token.isExpired()) throw UnauthorizedException("Token expired")

        val user = token.user
        val userId = user.id ?: throw UnauthorizedException("Invalid token")

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        token.usedAt = Instant.now()
        tokenRepo.save(token)

        // optional but recommended: revoke refresh tokens so old sessions die
        refreshTokenService.revokeAllForUser(userId)
    }
}