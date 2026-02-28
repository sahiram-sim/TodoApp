package com.example.todo.api.auth

import com.example.todo.api.auth.dto.*
import com.example.todo.logging.logger
import com.example.todo.security.AuthUser
import com.example.todo.service.auth.AuthService
import com.example.todo.service.auth.EmailVerificationService
import com.example.todo.service.auth.PasswordResetService
import com.example.todo.service.auth.RefreshTokenService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/auth")
class AuthController(
        private val authService: AuthService,
        private val emailVerificationService: EmailVerificationService,
        private val passwordResetService: PasswordResetService,
        private val refreshTokenService: RefreshTokenService
) {
    private val logger = logger()

    @PostMapping("/register")
    fun register(@Valid @RequestBody req: RegisterRequest): ResponseEntity<AuthResponse> {
        logger.info(
                "Auth register attempt emailDomain={}",
                req.email.substringAfter("@", "unknown")
        )
        val result = authService.register(req)
        logger.info(
                "Auth register success emailDomain={}",
                req.email.substringAfter("@", "unknown")
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(result)
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody req: LoginRequest): ResponseEntity<AuthResponse> {
        logger.info("Auth login attempt emailDomain={}", req.email.substringAfter("@", "unknown"))
        val result = authService.login(req)
        logger.info("Auth login success emailDomain={}", req.email.substringAfter("@", "unknown"))
        return ResponseEntity.ok(result)
    }

    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody req: RefreshRequest): ResponseEntity<AuthResponse> {
        logger.info("Auth refresh attempt")
        val result = authService.refresh(req.refreshToken)
        logger.info("Auth refresh success")
        return ResponseEntity.ok(result)
    }

    @PostMapping("/verify-email")
    fun verifyEmail(@Valid @RequestBody req: VerifyEmailRequest): MessageResponse {
        logger.info("Auth verify-email attempt")
        emailVerificationService.verifyEmail(req.token)
        logger.info("Auth verify-email success")
        return MessageResponse("Email verified")
    }

    @PostMapping("/resend-verification")
    fun resendVerification(@Valid @RequestBody req: ResendVerificationRequest): MessageResponse {
        logger.info(
                "Auth resend-verification attempt emailDomain={}",
                req.email.substringAfter("@", "unknown")
        )
        emailVerificationService.sendVerificationEmail(req.email)
        logger.info(
                "Auth resend-verification success emailDomain={}",
                req.email.substringAfter("@", "unknown")
        )
        return MessageResponse(
                "If the account exists and is unverified, a verification email has been sent."
        )
    }

    @PostMapping("/forgot-password")
    fun forgotPassword(@Valid @RequestBody req: ForgotPasswordRequest): MessageResponse {
        logger.info(
                "Auth forgot-password attempt emailDomain={}",
                req.email.substringAfter("@", "unknown")
        )
        passwordResetService.requestReset(req.email)
        logger.info(
                "Auth forgot-password done emailDomain={}",
                req.email.substringAfter("@", "unknown")
        )
        return MessageResponse("If the account exists, a password reset email has been sent.")
    }

    @PostMapping("/reset-password")
    fun resetPassword(@Valid @RequestBody req: ResetPasswordRequest): MessageResponse {
        logger.info("Auth reset-password attempt")
        passwordResetService.resetPassword(req.token, req.newPassword)
        logger.info("Auth reset-password success")
        return MessageResponse("Password reset successful")
    }

    @PostMapping("/change-password")
    fun changePassword(@AuthenticationPrincipal user: AuthUser, @Valid @RequestBody req: ChangePasswordRequest): MessageResponse {
        logger.info("Auth change-password attempt userId={}", user.id)
        authService.changePassword(user.id, req.oldPassword, req.newPassword)
        logger.info("Auth change-password success userId={}", user.id)
        return MessageResponse("Password changed successfully")
    }

    @PostMapping("/logout-all")
    fun logoutAll(@AuthenticationPrincipal user: AuthUser): MessageResponse {
        logger.info("Auth logout-all attempt userId={}", user.id)
        authService.logoutAll(user.id)
        logger.info("Auth logout-all success userId={}", user.id)
        return MessageResponse("Logged out from all sessions")
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun logout(@Valid @RequestBody req: RefreshRequest) {
        logger.info("Auth logout attempt")
        authService.logout(req.refreshToken)
        logger.info("Auth logout success")
    }

    @GetMapping("/sessions")
    fun listSessions(@AuthenticationPrincipal user: AuthUser): List<SessionResponse> {
        logger.info("Auth sessions list userId={}", user.id)
        return refreshTokenService.listSessions(user.id)
    }

    @DeleteMapping("/sessions/{sessionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeSession(@AuthenticationPrincipal user: AuthUser, @PathVariable sessionId: UUID) {
        logger.info("Auth session revoke attempt userId={} sessionId={}", user.id, sessionId)
        refreshTokenService.revokeSession(user.id, sessionId)
        logger.info("Auth session revoke success userId={} sessionId={}", user.id, sessionId)
    }
}
