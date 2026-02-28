package com.example.todo.domain.user

import com.example.todo.domain.role.Role
import jakarta.persistence.*
import org.hibernate.annotations.UuidGenerator
import java.util.UUID
import java.time.Instant

@Entity
@Table(
    name = "users",
    uniqueConstraints = [
        UniqueConstraint(name = "uq_users_email", columnNames = ["email"]),
        UniqueConstraint(name = "uq_users_username", columnNames = ["username"])
    ]
)
class User(
    @Id
    @UuidGenerator
    @Column(columnDefinition = "uuid")
    var id: UUID? = null,

    @Column(nullable = false)
    var email: String,

    @Column(nullable = false)
    var username: String,

    @Column(nullable = false)
    var passwordHash: String?,

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = [JoinColumn(name = "user_id")],
        inverseJoinColumns = [JoinColumn(name = "role_id")]
    )
    var roles: MutableSet<Role> = mutableSetOf(),

    @Column(nullable = false)
    var emailVerified: Boolean = false,

    var emailVerifiedAt: Instant? = null,

    @Column(nullable = false)
    var failedLoginCount: Int = 0,

    var lockedUntil: Instant? = null,

    var lastFailedLoginAt: Instant? = null,
    
    @Column(name = "reminders_enabled", nullable = false)
    var remindersEnabled: Boolean = true,

    @Column(name = "timezone", nullable = false)
    var timezone: String = "Asia/Kolkata",

    @Column(name = "reminder_hour")
    var reminderHour: Int? = null,

    @Column(name = "reminder_minute")
    var reminderMinute: Int? = null,
)