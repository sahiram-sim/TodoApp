package com.example.todo.service.reminder

import com.example.todo.config.AppProperties
import com.example.todo.domain.reminder.TodoReminder
import com.example.todo.domain.reminder.TodoReminderRepository
import com.example.todo.domain.todo.TodoRepository
import com.example.todo.domain.todo.TodoStatus
import com.example.todo.domain.user.UserRepository
import com.example.todo.logging.logger
import com.example.todo.service.email.EmailSender
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.ZoneId

@Component
class TodoReminderJob(
    private val props: AppProperties,
    private val todoRepository: TodoRepository,
    private val reminderRepo: TodoReminderRepository,
    private val userRepository: UserRepository,
    private val emailSender: EmailSender
) {
    private val logger = logger()
    private val zone: ZoneId = ZoneId.of("Asia/Kolkata")

    @Scheduled(fixedDelayString = "\${app.reminders.schedulerFixedDelay:PT10M}")
    @Transactional
    fun run() {
        if (!props.reminders.enabled) return

        val today = LocalDate.now(zone)
        val dueSoonDate = today.plusDays(props.reminders.dueSoonLeadDays)

        val dueSoonTodos = todoRepository.findAllByStatusAndDueDateBetween(
            TodoStatus.PENDING, dueSoonDate, dueSoonDate
        )

        val overdueTodos = todoRepository.findAllByStatusAndDueDateBefore(
            TodoStatus.PENDING, today
        )

        var sent = 0

        for (t in dueSoonTodos) {
            val todoId = t.id ?: continue
            val userId = t.userId ?: continue  // must be UUID? or UUID

            if (reminderRepo.existsByTodoIdAndReminderType(todoId, "DUE_SOON")) continue

            val user = userRepository.findById(userId).orElse(null) ?: continue

            val subject = "Todo due soon"
            val body = "Your todo \"${t.title}\" is due on ${t.dueDate}."

            emailSender.send(user.email, subject, body)

            reminderRepo.save(TodoReminder(todo = t, user = user, reminderType = "DUE_SOON"))
            sent++
        }

        for (t in overdueTodos) {
            val todoId = t.id ?: continue
            val userId = t.userId ?: continue

            if (reminderRepo.existsByTodoIdAndReminderType(todoId, "OVERDUE")) continue

            val user = userRepository.findById(userId).orElse(null) ?: continue

            val subject = "Todo overdue"
            val body = "Your todo \"${t.title}\" was due on ${t.dueDate}."

            emailSender.send(user.email, subject, body)

            reminderRepo.save(TodoReminder(todo = t, user = user, reminderType = "OVERDUE"))
            sent++
        }

        if (sent > 0) logger.info("TodoReminderJob sent={}", sent)
    }
}