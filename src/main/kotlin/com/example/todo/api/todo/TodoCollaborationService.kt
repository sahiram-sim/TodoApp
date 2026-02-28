package com.example.todo.service.todo

import com.example.todo.api.todo.dto.ShareTodoRequest
import com.example.todo.api.todo.dto.TodoShareResponse
import com.example.todo.domain.todo.TodoRepository
import com.example.todo.domain.todo.TodoShare
import com.example.todo.domain.todo.TodoShareRepository
import com.example.todo.domain.todo.TodoShareRole
import com.example.todo.domain.user.UserRepository
import com.example.todo.exception.NotFoundException
import com.example.todo.exception.UnauthorizedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TodoCollaborationService(
    private val todoRepository: TodoRepository,
    private val shareRepo: TodoShareRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun shareTodo(ownerId: UUID, todoId: UUID, req: ShareTodoRequest): TodoShareResponse {
        val todo = todoRepository.findByIdAndDeletedAtIsNull(todoId)
            ?: throw NotFoundException("Todo not found")

        if (todo.userId != ownerId) throw UnauthorizedException("Only owner can share")

        val identifier = req.usernameOrEmail.trim()
        val targetUser = userRepository.findByEmail(identifier.lowercase()).orElseGet {
            userRepository.findByUsername(identifier).orElseThrow { NotFoundException("Target user not found") }
        }

        val targetId = targetUser.id ?: throw IllegalStateException("Target user id missing")
        if (targetId == ownerId) throw UnauthorizedException("Cannot share with yourself")

        val existing = shareRepo.findByTodoIdAndSharedWithUserId(todoId, targetId)
        val saved = if (existing != null) {
            existing.shareRole = req.role
            shareRepo.save(existing)
        } else {
            shareRepo.save(
                TodoShare(
                    todoId = todoId,
                    ownerUserId = ownerId,
                    sharedWithUserId = targetId,
                    shareRole = req.role
                )
            )
        }

        return TodoShareResponse(
            id = saved.id,
            todoId = saved.todoId,
            sharedWithUserId = saved.sharedWithUserId,
            shareRole = saved.shareRole,
            createdAt = saved.createdAt
        )
    }

    @Transactional(readOnly = true)
    fun listShares(ownerId: UUID, todoId: UUID): List<TodoShareResponse> {
        val todo = todoRepository.findByIdAndDeletedAtIsNull(todoId)
            ?: throw NotFoundException("Todo not found")
        if (todo.userId != ownerId) throw UnauthorizedException("Only owner can view shares")

        return shareRepo.findAllByTodoId(todoId).map {
            TodoShareResponse(it.id, it.todoId, it.sharedWithUserId, it.shareRole, it.createdAt)
        }
    }

    @Transactional
    fun revokeShare(ownerId: UUID, todoId: UUID, sharedWithUserId: UUID) {
        val todo = todoRepository.findByIdAndDeletedAtIsNull(todoId)
            ?: throw NotFoundException("Todo not found")
        if (todo.userId != ownerId) throw UnauthorizedException("Only owner can revoke")

        val deleted = shareRepo.deleteByTodoIdAndSharedWithUserId(todoId, sharedWithUserId)
        if (deleted == 0) throw NotFoundException("Share not found")
    }

    @Transactional(readOnly = true)
    fun roleFor(userId: UUID, todoId: UUID): TodoShareRole? {
        return shareRepo.findByTodoIdAndSharedWithUserId(todoId, userId)?.shareRole
    }
}