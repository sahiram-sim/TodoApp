package com.example.todo.service.role

import com.example.todo.domain.role.Role
import com.example.todo.domain.role.RoleRepository
import com.example.todo.exception.ConflictException
import com.example.todo.exception.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class RoleService(
    private val roleRepository: RoleRepository
) {

    @Transactional(readOnly = true)
    fun list(): List<Role> = roleRepository.findAll().sortedBy { it.name }

    @Transactional
    fun create(nameRaw: String): Role {
        val name = normalizeRoleName(nameRaw)

        if (roleRepository.existsByName(name)) {
            throw ConflictException("Role already exists")
        }

        return roleRepository.save(Role(name = name))
    }

    @Transactional
    fun delete(roleId: UUID) {
        if (!roleRepository.existsById(roleId)) {
            throw NotFoundException("Role not found")
        }
        roleRepository.deleteById(roleId)
    }

    @Transactional(readOnly = true)
    fun requireByName(nameRaw: String): Role {
        val name = normalizeRoleName(nameRaw)
        return roleRepository.findByName(name).orElseThrow {
            NotFoundException("Role not found: $name")
        }
    }

    fun normalizeRoleName(name: String): String =
        name.trim().uppercase()
            .replace(" ", "_")
}