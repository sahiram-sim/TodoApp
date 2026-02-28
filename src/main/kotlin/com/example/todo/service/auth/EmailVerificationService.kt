package com.example.todo.service.auth

import com.example.todo.config.AppProperties
import com.example.todo.domain.auth.EmailVerificationToken
import com.example.todo.domain.auth.EmailVerificationTokenRepository
import com.example.todo.domain.user.UserRepository
import com.example.todo.exception.NotFoundException
import com.example.todo.security.TokenUtil
import com.example.todo.service.email.EmailSender
import java.time.Instant
import org.springframework.transaction.annotation.Transactional
import java.time.temporal.ChronoUnit
import org.springframework.stereotype.Service

@Service
class EmailVerificationService(
        private val userRepository: UserRepository,
        private val tokenRepo: EmailVerificationTokenRepository,
        private val emailSender: EmailSender,
        private val appProperties: AppProperties
) {

    @Transactional
    fun sendVerificationEmail(email: String) {
        val user =
                userRepository.findByEmailIgnoreCase(email.trim())
                        ?: throw NotFoundException("User not found")

        if (user.emailVerified) return // no-op

        // invalidate prior tokens (optional)
        tokenRepo.markAllUnusedAsUsed(user.id!!, Instant.now())

        val rawToken = TokenUtil.newRawToken()
        val tokenHash = TokenUtil.sha256Hex(rawToken)

        val expiresAt =
                Instant.now().plus(appProperties.security.emailVerifyTtlMinutes, ChronoUnit.MINUTES)
        tokenRepo.save(
                EmailVerificationToken(user = user, tokenHash = tokenHash, expiresAt = expiresAt)
        )

        val link = "${appProperties.links.frontendBaseUrl}/verify-email?token=$rawToken"
        val subject = "Verify your email"
        val body =
                "Click to verify your email: $link\nThis link expires in ${appProperties.security.emailVerifyTtlMinutes} minutes."

        emailSender.send(user.email, subject, body)
    }

    @Transactional
    fun verifyEmail(rawToken: String) {
        val tokenHash = TokenUtil.sha256Hex(rawToken.trim())
        val token = tokenRepo.findByTokenHash(tokenHash) ?: throw NotFoundException("Invalid token")

        if (token.isUsed()) throw IllegalStateException("Token already used")
        if (token.isExpired()) throw IllegalStateException("Token expired")

        val user = token.user
        if (!user.emailVerified) {
            user.emailVerified = true
            user.emailVerifiedAt = Instant.now()
            userRepository.save(user)
        }

        token.usedAt = Instant.now()
        tokenRepo.save(token)
    }
}
