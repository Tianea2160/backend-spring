package com.teamteam.backend.shared.entity

import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
open class BaseTimeEntity(
    @CreatedDate
    @Column(updatable = false, name = "created_at")
    var createdAt: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    @Column(name = "modified_at")
    var modifiedAt: LocalDateTime = LocalDateTime.now(),
)