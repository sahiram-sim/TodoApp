package com.example.todo.api.profile.dto

import java.util.UUID

data class ProfileResponse(
    val id: UUID,
    val email: String,
    val username: String
)