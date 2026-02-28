package com.example.todo.domain.todo

import java.time.LocalDate
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface TodoRepository : JpaRepository<Todo, UUID> {

    @Query(
            """
        select t from Todo t
        where t.userId = :userId
          and t.deletedAt is null
          and (:status is null or t.status = :status)
          and (:priority is null or t.priority = :priority)
          and (:dueDate is null or t.dueDate = :dueDate)
        """
    )
    fun findVisibleByUser(
            @Param("userId") userId: UUID,
            @Param("status") status: TodoStatus?,
            @Param("priority") priority: TodoPriority?,
            @Param("dueDate") dueDate: LocalDate?,
            pageable: Pageable
    ): Page<Todo>

    fun findByIdAndDeletedAtIsNull(id: UUID): Todo?

    fun findAllByDueDateBetween(d1: LocalDate, d2: LocalDate): List<Todo>
    fun findAllByDueDateBefore(d: LocalDate): List<Todo>

    @Query(
            """
    select t from Todo t
    where t.deletedAt is null
      and t.status <> com.example.todo.domain.todo.TodoStatus.DONE
      and t.dueDate = :dueDate
            """
    )
    fun findDueOn(@Param("dueDate") dueDate: LocalDate): List<Todo>

    @Query(
            """
    select t from Todo t
    where t.deletedAt is null
      and t.status <> com.example.todo.domain.todo.TodoStatus.DONE
      and t.dueDate < :today
            """
    )
    fun findOverdue(@Param("today") today: LocalDate): List<Todo>

    @Query(
            """
    select t from Todo t
    where t.deletedAt is null
      and t.id in (
        select s.todoId from TodoShare s
        where s.sharedWithUserId = :userId
      )
            """
    )
    fun findSharedVisible(@Param("userId") userId: UUID, pageable: Pageable): Page<Todo>

    @Query(
        """
        select t from Todo t
        where t.deletedAt is null
        and (
            t.userId = :userId
            or t.id in (
            select s.todoId from TodoShare s
            where s.sharedWithUserId = :userId
            )
        )
        and (:status is null or t.status = :status)
        and (:priority is null or t.priority = :priority)
        and (:dueDate is null or t.dueDate = :dueDate)
        """
    )
    fun findAccessible(
        @Param("userId") userId: UUID,
        @Param("status") status: TodoStatus?,
        @Param("priority") priority: TodoPriority?,
        @Param("dueDate") dueDate: LocalDate?,
        pageable: Pageable
    ): Page<Todo>
}
