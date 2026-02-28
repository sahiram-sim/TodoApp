package com.example.todo.domain.reminder

import com.example.todo.domain.todo.Todo
import com.example.todo.domain.user.User
import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(
    name = "todo_reminders",
    uniqueConstraints = [UniqueConstraint(name = "uq_tr_todo_type", columnNames = ["todo_id", "reminder_type"])]
)
class TodoReminder(
    @Id
    @Column(columnDefinition = "uuid")
    var id: UUID = UUID.randomUUID(),

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id", nullable = false)
    var todo: Todo,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(name = "reminder_type", nullable = false)
    var reminderType: String, // "DUE_SOON" | "OVERDUE"

    @Column(nullable = false)
    var sentAt: Instant = Instant.now()
)