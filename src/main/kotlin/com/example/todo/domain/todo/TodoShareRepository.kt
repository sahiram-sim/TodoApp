package com.example.todo.domain.todo

import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface TodoShareRepository : JpaRepository<TodoShare, UUID> {
    fun findAllByTodoId(todoId: UUID): List<TodoShare>
    fun findByTodoIdAndSharedWithUserId(todoId: UUID, sharedWithUserId: UUID): TodoShare?
    fun existsByTodoIdAndSharedWithUserId(todoId: UUID, sharedWithUserId: UUID): Boolean
    fun deleteByTodoIdAndSharedWithUserId(todoId: UUID, sharedWithUserId: UUID): Int

    fun findAllBySharedWithUserId(sharedWithUserId: UUID): List<TodoShare>
}