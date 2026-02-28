package com.example.todo.service.email

interface EmailSender {
    fun send(to: String, subject: String, body: String)
}