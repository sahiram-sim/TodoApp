package com.example.todo.api.admin.audit

import com.example.todo.api.admin.audit.dto.AuditEventResponse
import com.example.todo.service.audit.AuditService
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*
import java.time.Instant
import java.util.UUID

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
class AdminAuditController(
    private val auditService: AuditService
) {

    @GetMapping
    fun search(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) from: Instant?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) to: Instant?,
        @RequestParam(required = false) actorUserId: UUID?,
        @RequestParam(required = false) targetUserId: UUID?,
        @RequestParam(required = false) action: String?,
        @RequestParam(required = false) outcome: String?,
        pageable: Pageable
    ): Page<AuditEventResponse> {
        return auditService.search(from, to, actorUserId, targetUserId, action, outcome, pageable)
            .map { e ->
                AuditEventResponse(
                    id = e.id,
                    createdAt = e.createdAt,
                    actorUserId = e.actorUserId,
                    actorEmail = e.actorEmail,
                    targetUserId = e.targetUserId,
                    action = e.action,
                    outcome = e.outcome,
                    message = e.message,
                    ip = e.ip,
                    userAgent = e.userAgent,
                    requestId = e.requestId,
                    httpMethod = e.httpMethod,
                    httpPath = e.httpPath
                )
            }
    }
}