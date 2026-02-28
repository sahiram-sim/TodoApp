package com.example.todo.audit

data class AuditRequestContext(
    val requestId: String?,
    val ip: String?,
    val userAgent: String?,
    val method: String?,
    val path: String?
)

object AuditContextHolder {
    private val local = ThreadLocal<AuditRequestContext?>()

    fun set(ctx: AuditRequestContext) = local.set(ctx)
    fun get(): AuditRequestContext? = local.get()
    fun clear() = local.remove()
}