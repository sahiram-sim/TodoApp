package com.example.todo.api.admin

import com.example.todo.service.role.RoleService
import com.example.todo.service.role.UserRoleService
import com.example.todo.api.admin.dto.RoleResponse
import com.example.todo.api.admin.dto.CreateRoleRequest
import com.example.todo.api.admin.dto.AssignRolesRequest
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/admin/roles")
@PreAuthorize("hasRole('ADMIN')")
class AdminRoleController(
    private val roleService: RoleService,
    private val userRoleService: UserRoleService
) {

    @GetMapping
    fun listRoles(): List<RoleResponse> =
        roleService.list().map { RoleResponse(id = it.id!!, name = it.name) }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createRole(@Valid @RequestBody req: CreateRoleRequest): RoleResponse {
        val role = roleService.create(req.name)
        return RoleResponse(id = role.id!!, name = role.name)
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteRole(@PathVariable roleId: UUID) {
        roleService.delete(roleId)
    }

    @PostMapping("/assign")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun assignRoles(@Valid @RequestBody req: AssignRolesRequest) {
        userRoleService.assignRoles(req.usernameOrEmail, req.roleNames)
    }

    @PostMapping("/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun revokeRoles(@Valid @RequestBody req: AssignRolesRequest) {
        userRoleService.revokeRoles(req.usernameOrEmail, req.roleNames)
    }
}