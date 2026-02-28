package com.example.todo.domain.audit

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import java.util.UUID

interface AuditEventRepository : JpaRepository<AuditEvent, UUID>, JpaSpecificationExecutor<AuditEvent> {
    fun findAllByOrderByCreatedAtDesc(pageable: Pageable): Page<AuditEvent>
}