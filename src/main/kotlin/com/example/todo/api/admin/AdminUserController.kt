package com.example.todo.api.admin

import com.example.todo.api.admin.dto.AdminUpdateUserRequest
import com.example.todo.api.admin.dto.AdminUserResponse
import com.example.todo.api.admin.dto.SetRolesRequest
import com.example.todo.domain.user.UserRepository
import com.example.todo.exception.ConflictException
import com.example.todo.exception.NotFoundException
import com.example.todo.service.role.UserRoleService
import jakarta.validation.Valid
import java.util.UUID
import com.example.todo.logging.logger
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
class AdminUserController(
        private val userRepository: UserRepository,
        private val userRoleService: UserRoleService
) {
    private val logger = logger()
    @GetMapping
    fun listUsers(): List<AdminUserResponse> =
            userRepository.findAll().sortedBy { it.email }.map { u ->
                AdminUserResponse(
                        id = u.id!!,
                        email = u.email,
                        username = u.username,
                        roles = u.roles.map { it.name }.toSet()
                )
            }

    @GetMapping("/{userId}")
    fun getUser(@PathVariable userId: UUID): AdminUserResponse {
        val u = userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }
        return AdminUserResponse(
                id = u.id!!,
                email = u.email,
                username = u.username,
                roles = u.roles.map { it.name }.toSet()
        )
    }

    @PutMapping("/{userId}")
    fun updateUser(
            @PathVariable userId: UUID,
            @Valid @RequestBody req: AdminUpdateUserRequest
    ): AdminUserResponse {
        val user =
                userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }

        req.email?.trim()?.lowercase()?.let { newEmail ->
            if (newEmail.isNotBlank() && !newEmail.equals(user.email, ignoreCase = true)) {
                if (userRepository.existsByEmail(newEmail))
                        throw ConflictException("Email already exists")
                user.email = newEmail
            }
        }

        req.username?.trim()?.let { newUsername ->
            if (newUsername.isNotBlank() && !newUsername.equals(user.username, ignoreCase = true)) {
                if (userRepository.existsByUsername(newUsername))
                        throw ConflictException("Username already exists")
                user.username = newUsername
            }
        }

        val saved = userRepository.save(user)

        return AdminUserResponse(
                id = saved.id!!,
                email = saved.email,
                username = saved.username,
                roles = saved.roles.map { it.name }.toSet()
        )
    }

    @PutMapping("/{userId}/roles")
    fun setRoles(
            @PathVariable userId: UUID,
            @Valid @RequestBody req: SetRolesRequest
    ): AdminUserResponse {
        val updated = userRoleService.setRoles(userId, req.roleNames)
        return AdminUserResponse(
                id = updated.id!!,
                email = updated.email,
                username = updated.username,
                roles = updated.roles.map { it.name }.toSet()
        )
    }
}
