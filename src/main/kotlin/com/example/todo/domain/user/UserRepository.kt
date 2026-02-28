package com.example.todo.domain.user

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface UserRepository : JpaRepository<User, UUID> {
    fun existsByEmail(email: String): Boolean
    fun existsByUsername(username: String): Boolean
    fun findByEmail(email: String): java.util.Optional<User>
    fun findByIdAndEmail(id: UUID, email: String): User?
    fun findByUsername(username: String): java.util.Optional<User>

    fun findAllByOrderByUsernameAsc(pageable: Pageable): Page<User>
    fun findByEmailContainingIgnoreCaseOrUsernameContainingIgnoreCase(
        email: String,
        username: String,
        pageable: Pageable
    ): Page<User>

    fun findByEmailIgnoreCase(email: String): User?
}