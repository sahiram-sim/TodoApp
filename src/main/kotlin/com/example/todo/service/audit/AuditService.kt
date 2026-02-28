package com.example.todo.service.audit

import com.example.todo.audit.AuditContextHolder
import com.example.todo.domain.audit.AuditEvent
import com.example.todo.domain.audit.AuditEventRepository
import com.example.todo.security.AuthUser
import java.time.Instant
import java.util.UUID
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.domain.Specification
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuditService(private val repo: AuditEventRepository) {

    @Transactional
    fun log(
            action: String,
            outcome: String, // "SUCCESS" / "FAIL"
            actor: com.example.todo.security.AuthUser? = null,
            actorEmailOverride: String? = null,
            targetUserId: UUID? = null,
            message: String? = null
    ) {
        val ctx = AuditContextHolder.get()

        repo.save(
                AuditEvent(
                        actorUserId = actor?.id,
                        actorEmail = actorEmailOverride ?: actor?.username, // <-- key change
                        targetUserId = targetUserId,
                        action = action,
                        outcome = outcome,
                        message = message,
                        ip = ctx?.ip,
                        userAgent = ctx?.userAgent,
                        requestId = ctx?.requestId,
                        httpMethod = ctx?.method,
                        httpPath = ctx?.path,
                        createdAt = Instant.now()
                )
        )
    }

    @Transactional(readOnly = true)
    fun search(
            from: Instant?,
            to: Instant?,
            actorUserId: UUID?,
            targetUserId: UUID?,
            action: String?,
            outcome: String?,
            pageable: Pageable
    ): Page<AuditEvent> {

        var spec: Specification<AuditEvent> = Specification { _, _, cb -> cb.conjunction() }

        if (from != null)
                spec =
                        spec.and { root, _, cb ->
                            cb.greaterThanOrEqualTo(root.get("createdAt"), from)
                        }

        if (to != null)
                spec = spec.and { root, _, cb -> cb.lessThanOrEqualTo(root.get("createdAt"), to) }

        if (actorUserId != null)
                spec =
                        spec.and { root, _, cb ->
                            cb.equal(root.get<UUID>("actorUserId"), actorUserId)
                        }

        if (targetUserId != null)
                spec =
                        spec.and { root, _, cb ->
                            cb.equal(root.get<UUID>("targetUserId"), targetUserId)
                        }

        if (!action.isNullOrBlank())
                spec = spec.and { root, _, cb -> cb.equal(root.get<String>("action"), action) }

        if (!outcome.isNullOrBlank())
                spec = spec.and { root, _, cb -> cb.equal(root.get<String>("outcome"), outcome) }

        return repo.findAll(spec, pageable)
    }
}
