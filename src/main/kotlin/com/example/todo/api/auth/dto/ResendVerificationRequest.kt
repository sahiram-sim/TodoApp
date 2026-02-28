package com.example.todo.api.auth.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class ResendVerificationRequest(
    @field:Email
    @field:NotBlank
    val email: String
)