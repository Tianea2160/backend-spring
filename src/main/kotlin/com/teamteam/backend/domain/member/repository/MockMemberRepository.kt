package com.teamteam.backend.domain.member.repository

import com.teamteam.backend.domain.member.entity.MockMember
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MockMemberRepository : JpaRepository<MockMember, String> {
}