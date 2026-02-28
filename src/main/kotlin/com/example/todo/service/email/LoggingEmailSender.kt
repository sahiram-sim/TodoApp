package com.example.todo.service.email

import com.example.todo.logging.logger
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
@Primary
class LoggingEmailSender : EmailSender {
    private val logger = logger()

    override fun send(to: String, subject: String, body: String) {
        // Safe for dev. Replace with JavaMail/Ses/SendGrid implementation later.
        logger.info("EMAIL to={} subject={} body={}", to, subject, body)
    }
}