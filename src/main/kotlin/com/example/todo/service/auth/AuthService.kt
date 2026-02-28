package com.example.todo.service.auth

import com.example.todo.api.auth.dto.AuthResponse
import com.example.todo.api.auth.dto.LoginRequest
import com.example.todo.api.auth.dto.RegisterRequest
import com.example.todo.config.AppProperties
import com.example.todo.config.JwtProperties
import com.example.todo.domain.role.RoleRepository
import com.example.todo.domain.user.User
import com.example.todo.domain.user.UserRepository
import com.example.todo.exception.ConflictException
import com.example.todo.exception.UnauthorizedException
import com.example.todo.logging.logger
import com.example.todo.service.audit.AuditService
import java.time.Instant
import java.time.temporal.ChronoUnit
import org.springframework.security.authentication.LockedException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
        private val userRepository: UserRepository,
        private val passwordEncoder: PasswordEncoder,
        private val tokenService: TokenService,
        private val roleRepository: RoleRepository,
        private val refreshTokenService: RefreshTokenService,
        private val jwtProperties: JwtProperties,
        private val emailVerificationService: EmailVerificationService,
        private val appProperties: AppProperties,
        private val auditService: AuditService,
) {
    private val logger = logger()

    @Transactional
    fun register(req: RegisterRequest): AuthResponse {
        val email = req.email.trim().lowercase()
        val username = req.username.trim()

        logger.info(
                "Register attempt emailDomain={} username={}",
                email.substringAfter("@", "unknown"),
                username
        )

        if (userRepository.existsByEmail(email)) {
            logger.warn(
                    "Register rejected: email exists emailDomain={}",
                    email.substringAfter("@", "unknown")
            )
            throw ConflictException("Email already exists")
        }
        if (userRepository.existsByUsername(username)) {
            logger.warn("Register rejected: username exists username={}", username)
            throw ConflictException("Username already exists")
        }

        val userRole =
                roleRepository.findByName("USER").orElseThrow {
                    IllegalStateException("Default role USER not seeded")
                }

        val user =
                User(
                        email = email,
                        username = username,
                        passwordHash = passwordEncoder.encode(req.password),
                        roles = mutableSetOf(userRole)
                )

        val saved = userRepository.save(user)

        emailVerificationService.sendVerificationEmail(saved.email)

        val access = tokenService.createAccessToken(saved)
        val refreshValue = tokenService.createRefreshTokenValue()
        refreshTokenService.issue(saved, refreshValue)

        logger.info("Register success userId={}", saved.id)

        return AuthResponse(
                accessToken = access,
                refreshToken = refreshValue,
                accessTokenExpiresInSeconds = jwtProperties.accessTokenExpirationSeconds
        )
    }

//     @Transactional(noRollbackFor = [UnauthorizedException::class.java, LockedException::class.java])
    fun login(req: LoginRequest): AuthResponse {
        val email = req.email.trim().lowercase()
        logger.info("Login attempt emailDomain={}", email.substringAfter("@", "unknown"))

        val user =
                userRepository.findByEmail(email).orElseThrow {
                    logger.warn(
                            "Login failed: user not found emailDomain={}",
                            email.substringAfter("@", "unknown")
                    )

                    auditService.log(
                            action = "AUTH_LOGIN",
                            outcome = "FAIL",
                            actorEmailOverride = email,
                            message = "User not found"
                    )

                    UnauthorizedException("Invalid credentials")
                }

        val now = Instant.now()
        val lockedUntil = user.lockedUntil
        if (lockedUntil != null && now.isBefore(lockedUntil)) {
            logger.warn(
                    "Login blocked: account locked userId={} lockedUntil={}",
                    user.id,
                    lockedUntil
            )

            auditService.log(
                    action = "AUTH_LOGIN",
                    outcome = "FAIL",
                    actorEmailOverride = email,
                    targetUserId = user.id,
                    message = "Account locked until $lockedUntil"
            )

            throw LockedException("Account locked. Try again later.") // or UnauthorizedException
        }

        if (!user.emailVerified) {
            logger.warn("Login blocked: email not verified userId={}", user.id)

            auditService.log(
                    action = "AUTH_LOGIN",
                    outcome = "FAIL",
                    actorEmailOverride = email,
                    targetUserId = user.id,
                    message = "Email not verified"
            )

            throw UnauthorizedException("Email not verified")
        }

        val hash =
                user.passwordHash
                        ?: run {
                            auditService.log(
                                    action = "AUTH_LOGIN",
                                    outcome = "FAIL",
                                    actorEmailOverride = email,
                                    targetUserId = user.id,
                                    message = "Missing password hash"
                            )
                            throw UnauthorizedException("Invalid credentials")
                        }

        if (!passwordEncoder.matches(req.password, hash)) {
            user.failedLoginCount = user.failedLoginCount + 1
            user.lastFailedLoginAt = now

            val maxAttempts = appProperties.security.loginMaxAttempts
            val lockMinutes = appProperties.security.loginLockMinutes

            if (user.failedLoginCount >= maxAttempts) {
                user.lockedUntil = now.plus(lockMinutes, ChronoUnit.MINUTES)
                logger.warn(
                        "Login failed: locking account userId={} failedCount={} lockedUntil={}",
                        user.id,
                        user.failedLoginCount,
                        user.lockedUntil
                )

                auditService.log(
                        action = "AUTH_LOGIN",
                        outcome = "FAIL",
                        actorEmailOverride = email,
                        targetUserId = user.id,
                        message = "Bad password; account locked until ${user.lockedUntil}"
                )
            } else {
                logger.warn(
                        "Login failed: bad password userId={} failedCount={}",
                        user.id,
                        user.failedLoginCount
                )

                auditService.log(
                        action = "AUTH_LOGIN",
                        outcome = "FAIL",
                        actorEmailOverride = email,
                        targetUserId = user.id,
                        message = "Bad password; failedCount=${user.failedLoginCount}"
                )
            }

            userRepository.save(user)
            throw UnauthorizedException("Invalid credentials")
        }

        // success: reset counters
        if (user.failedLoginCount != 0 || user.lockedUntil != null || user.lastFailedLoginAt != null
        ) {
            user.failedLoginCount = 0
            user.lockedUntil = null
            user.lastFailedLoginAt = null
            userRepository.save(user)
        }

        val userId = user.id ?: throw IllegalStateException("User id missing")

        refreshTokenService.revokeAllForUser(userId)

        val access = tokenService.createAccessToken(user)
        val refreshValue = tokenService.createRefreshTokenValue()
        refreshTokenService.issue(user, refreshValue)

        auditService.log(
                action = "AUTH_LOGIN",
                outcome = "SUCCESS",
                actorEmailOverride = email,
                targetUserId = userId,
                message = "Login success"
        )

        logger.info("Login success userId={}", userId)

        return AuthResponse(
                accessToken = access,
                refreshToken = refreshValue,
                accessTokenExpiresInSeconds = jwtProperties.accessTokenExpirationSeconds
        )
    }

    @Transactional
    fun refresh(refreshToken: String): AuthResponse {
        // Do NOT log token
        logger.info("Refresh attempt")

        val raw = refreshToken.trim()
        if (raw.isBlank()) {
            logger.warn("Refresh failed: blank token")
            throw UnauthorizedException("Invalid refresh token")
        }

        val rotated = refreshTokenService.rotate(raw)
        val access = tokenService.createAccessToken(rotated.user)

        logger.info("Refresh success userId={}", rotated.user.id)

        return AuthResponse(
                accessToken = access,
                refreshToken = rotated.newRefreshTokenValue,
                accessTokenExpiresInSeconds = jwtProperties.accessTokenExpirationSeconds
        )
    }

    @Transactional
    fun logout(refreshToken: String) {
        // Do NOT log token
        logger.info("Logout attempt")

        val raw = refreshToken.trim()
        if (raw.isBlank()) {
            logger.warn("Logout failed: blank token")
            throw UnauthorizedException("Invalid refresh token")
        }

        refreshTokenService.revoke(raw)
        logger.info("Logout success")
    }

    @Transactional
    fun changePassword(userId: java.util.UUID, oldPassword: String, newPassword: String) {
        val user =
                userRepository.findById(userId).orElseThrow {
                    UnauthorizedException("Unauthorized")
                }

        val currentHash = user.passwordHash ?: throw UnauthorizedException("Unauthorized")

        if (!passwordEncoder.matches(oldPassword, currentHash)) {
            throw UnauthorizedException("Invalid credentials")
        }

        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)

        // revoke all sessions
        refreshTokenService.revokeAllForUser(userId)
    }

    @Transactional
    fun logoutAll(userId: java.util.UUID) {
        refreshTokenService.revokeAllForUser(userId)
    }
}
