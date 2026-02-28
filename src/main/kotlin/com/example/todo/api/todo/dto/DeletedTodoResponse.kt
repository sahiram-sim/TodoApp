package com.example.todo.api.todo.dto

import java.time.Instant
import java.util.UUID

data class DeletedTodoResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val status: String,
    val priority: String,
    val dueDate: String?,
    val deletedAt: Instant
)