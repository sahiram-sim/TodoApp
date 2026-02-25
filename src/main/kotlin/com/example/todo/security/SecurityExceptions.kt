package com.example.todo.security

import org.springframework.security.core.AuthenticationException

class JwtAuthenticationFailedException(message: String) : AuthenticationException(message)