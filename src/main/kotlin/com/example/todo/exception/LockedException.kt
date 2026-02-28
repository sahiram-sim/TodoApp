package com.example.todo.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

@ResponseStatus(HttpStatus.LOCKED)
class LockedException(message: String) : RuntimeException(message)