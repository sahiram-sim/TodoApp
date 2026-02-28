package com.example.todo.service.role

import com.example.todo.domain.role.RoleRepository
import com.example.todo.domain.user.User
import com.example.todo.domain.user.UserRepository
import com.example.todo.exception.NotFoundException
import java.util.UUID
import com.example.todo.logging.logger
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserRoleService(
        private val userRepository: UserRepository,
        private val roleRepository: RoleRepository
) {
    private val logger = logger()

    @Transactional
    fun assignRoles(usernameOrEmailRaw: String, roleNamesRaw: Set<String>) {
        val key = usernameOrEmailRaw.trim()
        val user =
                userRepository.findByEmail(key.lowercase()).orElseGet {
                    userRepository.findByUsername(key).orElseThrow {
                        NotFoundException("User not found")
                    }
                }

        val roles =
                roleNamesRaw.map { it.trim().uppercase().replace(" ", "_") }.toSet().map { roleName
                    ->
                    roleRepository.findByName(roleName).orElseThrow {
                        NotFoundException("Role not found: $roleName")
                    }
                }

        user.roles.addAll(roles)
        userRepository.save(user)
    }

    @Transactional
    fun revokeRoles(usernameOrEmailRaw: String, roleNamesRaw: Set<String>) {
        val key = usernameOrEmailRaw.trim()
        val user =
                userRepository.findByEmail(key.lowercase()).orElseGet {
                    userRepository.findByUsername(key).orElseThrow {
                        NotFoundException("User not found")
                    }
                }

        val targetNames = roleNamesRaw.map { it.trim().uppercase().replace(" ", "_") }.toSet()

        user.roles.removeIf { it.name in targetNames }
        userRepository.save(user)
    }

    @Transactional
    fun setRoles(userId: UUID, roleNamesRaw: Set<String>): User {
        val user =
                userRepository.findById(userId).orElseThrow { NotFoundException("User not found") }

        val roles =
                roleNamesRaw
                        .map { it.trim().uppercase().replace(" ", "_") }
                        .toSet()
                        .map { name ->
                            roleRepository.findByName(name).orElseThrow {
                                NotFoundException("Role not found: $name")
                            }
                        }
                        .toSet()

        user.roles.clear()
        user.roles.addAll(roles)
        return userRepository.save(user)
    }
}
