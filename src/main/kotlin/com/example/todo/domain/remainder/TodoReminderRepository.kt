package com.example.todo.domain.reminder

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TodoReminderRepository : JpaRepository<TodoReminder, UUID> {
    fun existsByTodoIdAndReminderType(todoId: UUID, reminderType: String): Boolean
}