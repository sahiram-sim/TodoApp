package com.example.todo.api.todo

import com.example.todo.api.todo.dto.*
import com.example.todo.api.todo.dto.ShareTodoRequest
import com.example.todo.api.todo.dto.TodoShareResponse
import com.example.todo.config.AppProperties
import com.example.todo.domain.todo.TodoPriority
import com.example.todo.domain.todo.TodoStatus
import com.example.todo.security.AuthUser
import com.example.todo.service.todo.TodoCollaborationService
import com.example.todo.service.todo.TodoService
import jakarta.validation.Valid
import java.time.LocalDate
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/todos")
class TodoController(
        private val todoService: TodoService,
        private val appProperties: AppProperties,
        private val todoCollaborationService: TodoCollaborationService
) {
        @GetMapping
        fun listTodos(
                @AuthenticationPrincipal user: AuthUser,
                @RequestParam(required = false) status: TodoStatus?,
                @RequestParam(required = false) priority: TodoPriority?,
                @RequestParam(required = false)
                @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                dueDate: LocalDate?,
                pageable: Pageable
        ): TodoListResponse {
                val maxSize = appProperties.pagination.maxSize
                val safeSize = pageable.pageSize.coerceIn(1, maxSize)
                val safePageable = PageRequest.of(pageable.pageNumber, safeSize, pageable.sort)

                return todoService.listTodos(user.id, status, priority, dueDate, safePageable)
        }

        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        fun createTodo(
                @AuthenticationPrincipal user: AuthUser,
                @RequestBody req: TodoCreateRequest
        ): TodoResponse = todoService.createTodo(user.id, req)

        @GetMapping("/{id}")
        fun getTodo(@AuthenticationPrincipal user: AuthUser, @PathVariable id: UUID): TodoResponse =
                todoService.getTodo(user.id, id)

        @PutMapping("/{id}")
        fun updateTodo(
                @AuthenticationPrincipal user: AuthUser,
                @PathVariable id: UUID,
                @RequestBody req: TodoUpdateRequest
        ): TodoResponse = todoService.updateTodo(user.id, id, req)

        @DeleteMapping("/{id}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        fun deleteTodo(@AuthenticationPrincipal user: AuthUser, @PathVariable id: UUID) {
                todoService.deleteTodo(user.id, id)
        }

        @PostMapping("/{id}/share")
        @ResponseStatus(HttpStatus.CREATED)
        fun shareTodo(
                @AuthenticationPrincipal user: AuthUser,
                @PathVariable id: UUID,
                @Valid @RequestBody req: ShareTodoRequest
        ): TodoShareResponse = todoCollaborationService.shareTodo(user.id, id, req)

        @GetMapping("/{id}/shares")
        fun listShares(
                @AuthenticationPrincipal user: AuthUser,
                @PathVariable id: UUID
        ): List<TodoShareResponse> = todoCollaborationService.listShares(user.id, id)

        @DeleteMapping("/{id}/share/{sharedWithUserId}")
        @ResponseStatus(HttpStatus.NO_CONTENT)
        fun revokeShare(
                @AuthenticationPrincipal user: AuthUser,
                @PathVariable id: UUID,
                @PathVariable sharedWithUserId: UUID
        ) {
                todoCollaborationService.revokeShare(user.id, id, sharedWithUserId)
        }

        @GetMapping("/deleted")
        fun listDeleted(
                @AuthenticationPrincipal user: AuthUser,
                pageable: Pageable
        ): TodoListResponse = todoService.listDeletedTodos(user.id, pageable)

        @PostMapping("/{id}/restore")
        fun restore(@AuthenticationPrincipal user: AuthUser, @PathVariable id: UUID): TodoResponse =
                todoService.restoreTodo(user.id, id)
}
