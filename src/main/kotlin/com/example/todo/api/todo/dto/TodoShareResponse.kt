package com.example.todo.api.todo.dto

import com.example.todo.domain.todo.TodoShareRole
import java.time.Instant
import java.util.UUID

data class TodoShareResponse(
    val id: UUID,
    val todoId: UUID,
    val sharedWithUserId: UUID,
    val shareRole: TodoShareRole,
    val createdAt: Instant
)