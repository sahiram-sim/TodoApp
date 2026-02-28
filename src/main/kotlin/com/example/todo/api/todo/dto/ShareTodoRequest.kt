package com.example.todo.api.todo.dto

import com.example.todo.domain.todo.TodoShareRole
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ShareTodoRequest(
    @field:NotBlank
    val usernameOrEmail: String,

    @field:NotNull
    val role: TodoShareRole
)