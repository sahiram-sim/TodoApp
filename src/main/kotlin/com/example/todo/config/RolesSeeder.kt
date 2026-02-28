package com.example.todo.config

import com.example.todo.domain.role.Role
import com.example.todo.domain.role.RoleRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RolesSeeder {

    @Bean
    fun seedRoles(roleRepository: RoleRepository) = ApplicationRunner {
        ensure(roleRepository, "USER")
        ensure(roleRepository, "ADMIN")
    }

    private fun ensure(repo: RoleRepository, roleName: String) {
        val name = roleName.trim().uppercase()
        // repo.save(Role(name = name))
        if (!repo.existsByName(name)) repo.save(Role(name = name))
    }
}
