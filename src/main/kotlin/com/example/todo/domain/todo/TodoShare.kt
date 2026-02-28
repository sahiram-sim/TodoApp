package com.example.todo.domain.todo

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "todo_shares",
    uniqueConstraints = [UniqueConstraint(name = "uq_todo_shares_todo_user", columnNames = ["todo_id", "shared_with_user_id"])]
)
class TodoShare(
    @Id
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID = UUID.randomUUID(),

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "todo_id", nullable = false, updatable = false)
    var todoId: UUID,

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "owner_user_id", nullable = false, updatable = false)
    var ownerUserId: UUID,

    @JdbcTypeCode(SqlTypes.UUID)
    @Column(name = "shared_with_user_id", nullable = false, updatable = false)
    var sharedWithUserId: UUID,

    @Enumerated(EnumType.STRING)
    @Column(name = "share_role", nullable = false, length = 20)
    var shareRole: TodoShareRole,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant = Instant.now()
)