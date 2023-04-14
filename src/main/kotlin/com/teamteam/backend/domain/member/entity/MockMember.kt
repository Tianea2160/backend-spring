package com.teamteam.backend.domain.member.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name="mock_member")
class MockMember(
    @Id
    var id : String,
    var username : String
)