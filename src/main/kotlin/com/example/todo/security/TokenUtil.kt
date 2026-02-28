package com.example.todo.security

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

object TokenUtil {
    private val rng = SecureRandom()

    fun newRawToken(bytes: Int = 32): String {
        val b = ByteArray(bytes)
        rng.nextBytes(b)
        // URL-safe token
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b)
    }

    fun sha256Hex(input: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(input.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }
}