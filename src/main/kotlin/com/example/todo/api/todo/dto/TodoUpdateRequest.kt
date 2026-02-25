package com.example.todo.api.todo.dto

import com.example.todo.domain.todo.TodoPriority
import com.example.todo.domain.todo.TodoStatus
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class TodoUpdateRequest(
    @field:Size(max = 200)
    val title: String? = null,

    val description: String? = null,
    val status: TodoStatus? = null,
    val priority: TodoPriority? = null,
    val dueDate: LocalDate? = null
)