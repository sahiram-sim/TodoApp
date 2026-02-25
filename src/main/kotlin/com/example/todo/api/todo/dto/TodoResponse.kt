package com.example.todo.api.todo.dto

import com.example.todo.domain.todo.Todo
import com.example.todo.domain.todo.TodoPriority
import com.example.todo.domain.todo.TodoStatus
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

data class TodoResponse(
    val id: UUID,
    val title: String,
    val description: String?,
    val status: TodoStatus,
    val priority: TodoPriority,
    val dueDate: LocalDate?,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(todo: Todo) = TodoResponse(
            id = todo.id,
            title = todo.title,
            description = todo.description,
            status = todo.status,
            priority = todo.priority,
            dueDate = todo.dueDate,
            createdAt = todo.createdAt,
            updatedAt = todo.updatedAt
        )
    }
}