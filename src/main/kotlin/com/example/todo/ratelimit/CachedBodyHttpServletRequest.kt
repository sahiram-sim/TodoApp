package com.example.todo.ratelimit

import jakarta.servlet.ReadListener
import jakarta.servlet.ServletInputStream
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import java.io.ByteArrayInputStream
import java.nio.charset.Charset

class CachedBodyHttpServletRequest(request: HttpServletRequest) : HttpServletRequestWrapper(request) {
    private val cachedBody: ByteArray = request.inputStream.readBytes()

    override fun getInputStream(): ServletInputStream {
        val bais = ByteArrayInputStream(cachedBody)
        return object : ServletInputStream() {
            override fun isFinished(): Boolean = bais.available() == 0
            override fun isReady(): Boolean = true
            override fun setReadListener(readListener: ReadListener?) {}
            override fun read(): Int = bais.read()
        }
    }

    fun bodyAsString(charset: Charset = Charsets.UTF_8): String =
        cachedBody.toString(charset)
}