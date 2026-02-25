package com.example.todo.api.profile

import com.example.todo.api.profile.dto.ProfileResponse
import com.example.todo.api.profile.dto.ProfileUpdateRequest
import com.example.todo.logging.logger
import com.example.todo.security.AuthUser
import com.example.todo.service.profile.ProfileService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile")
class ProfileController(
    private val profileService: ProfileService
) {
    private val logger = logger()

    @GetMapping
    fun me(@AuthenticationPrincipal user: AuthUser): ProfileResponse {
        logger.info("Profile get request userId={}", user.id)
        val res = profileService.getMyProfile(user.id)
        logger.info("Profile get success userId={}", user.id)
        return res
    }

    @PutMapping
    fun update(
        @AuthenticationPrincipal user: AuthUser,
        @Valid @RequestBody req: ProfileUpdateRequest
    ): ProfileResponse {
        // safe: username only (no password/token)
        logger.info("Profile update request userId={} newUsername={}", user.id, req.username.trim())
        val res = profileService.updateMyProfile(user.id, req)
        logger.info("Profile update success userId={}", user.id)
        return res
    }
}