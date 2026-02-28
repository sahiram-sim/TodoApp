package com.example.todo.api.admin.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class RoleResponse(
    val id: UUID,
    val name: String
)

data class CreateRoleRequest(
    @field:NotBlank
    @field:Size(max = 50)
    val name: String
)

data class AssignRolesRequest(
    @field:NotBlank
    val usernameOrEmail: String,
    val roleNames: Set<@NotBlank String>
)

data class AdminUserResponse(
    val id: UUID,
    val email: String,
    val username: String,
    val roles: Set<String>
)

data class AdminUpdateUserRequest(
    @field:Size(max = 255)
    val email: String? = null,

    @field:Size(max = 255)
    val username: String? = null
)

data class SetRolesRequest(
    val roleNames: Set<@NotBlank String>
)