package com.teamteam.backend.domain.member.entity

import jakarta.persistence.Id
import org.springframework.data.mongodb.core.mapping.Document


@Document(collection = "service_user")
class Member(
    @Id
    val id : String,
    val PID: String,
    val role: String,
    val username: String,
)